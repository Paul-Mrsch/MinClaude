package minclaude.tracking;

import arc.Events;
import arc.func.Cons;
import arc.util.Time;
import minclaude.ModSettings;
import minclaude.stats.*;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;

import static mindustry.Vars.*;

/**
 * Observe le noyau de l'équipe du joueur à chaque tick de jeu (hors pause) et enregistre une fois par seconde
 * le stock, les entrées et les sorties de chaque ressource dans {@link #history}.
 * Une ressource n'est suivie qu'à partir de la première fois où elle apparaît dans le noyau.
 */
public final class ResourceTracker{
    public static final String CHUNK_NAME = "minclaude-history";
    public static final String GOALS_CHUNK_NAME = "minclaude-goals";
    private static final float TICKS_PER_SECOND = 60f;
    /** Alerte d'énergie quand moins de cette part de la demande est couverte. */
    private static final float POWER_SHORTAGE_THRESHOLD = 0.75f;

    private volatile MetricHistory history = new MetricHistory();
    private final DeltaAccumulator deltas = new DeltaAccumulator(64);
    private final AlertEngine alerts = new AlertEngine();
    private final BaseScanner scanner = new BaseScanner();
    private final minclaude.logic.StockGoals goals = new minclaude.logic.StockGoals();
    private boolean[] tracked = new boolean[64];
    private float tickTimer;
    /** Stock de chaque ressource à la seconde précédente (mesure exacte des sorties). */
    private int[] lastStock = filled(64);
    private Cons<AlertEngine.Alert> alertListener = a -> {};

    public void register(){
        Events.on(WorldLoadEvent.class, e -> resetForNewWorld());
        Events.on(ResetEvent.class, e -> resetForNewWorld());
        Events.run(Trigger.update, this::update);
    }

    public MetricHistory history(){
        return history;
    }

    /** Remplace l'historique (chargement d'une sauvegarde). Appelé après WorldLoadEvent par le chunk de sauvegarde. */
    public void setHistory(MetricHistory loaded){
        history = loaded;
        for(String key : loaded.keys()){
            if(!key.startsWith("item/") || !key.endsWith("/stock")) continue;
            Item item = content.item(key.substring(5, key.length() - 6));
            if(item != null) markTracked(item.id);
        }
    }

    public void onAlert(Cons<AlertEngine.Alert> listener){
        alertListener = listener;
    }

    public boolean isTracked(Item item){
        return item.id < tracked.length && tracked[item.id];
    }

    /** Équipe suivie : celle du joueur, ou l'équipe par défaut sans joueur (tests headless). */
    public Team team(){
        return player != null ? player.team() : state.rules.defaultTeam;
    }

    public CoreBuild core(){
        return team().core();
    }

    /** Objectifs de stock du joueur (enregistrés dans la sauvegarde). */
    public minclaude.logic.StockGoals goals(){
        return goals;
    }

    /** Relevé immédiat de la base, hors cycle d'une seconde (autotest en jeu). */
    public void scanNow(){
        scanner.scan(team(), history);
    }

    /** Dernier relevé de la base (énergie, usines, défense), mis à jour chaque seconde. */
    public BaseScanner.Snapshot base(){
        return scanner.last();
    }

    public ResourceStats stats(Item item){
        CoreBuild core = core();
        float capacity = core == null ? 0 : core.storageCapacity;
        return ResourceStats.compute(history, item.name, capacity, ResourceStats.DEFAULT_WINDOW);
    }

    private void resetForNewWorld(){
        history = new MetricHistory();
        deltas.reset();
        alerts.reset();
        tracked = new boolean[tracked.length];
        tickTimer = 0f;
        java.util.Arrays.fill(lastStock, -1);
        CoreFlowHook.counter.reset();
        scanner.reset();
        goals.clear();
    }

    private static int[] filled(int n){
        int[] a = new int[n];
        java.util.Arrays.fill(a, -1);
        return a;
    }

    /** Vrai si le noyau suivi est instrumenté : entrées, sorties et pertes sont alors exactes. */
    public boolean exactFlows(){
        return core() instanceof CoreFlowHook.TrackedCoreBuild;
    }

    private void update(){
        if(!state.isPlaying()) return;
        CoreBuild core = core();
        if(core == null) return;
        CoreFlowHook.trackedTeam = core.team;

        for(Item item : content.items()){
            int amount = core.items.get(item);
            if(amount > 0) markTracked(item.id);
            if(isTracked(item)) deltas.observe(item.id, amount);
        }

        tickTimer += Time.delta;
        while(tickTimer >= TICKS_PER_SECOND){
            tickTimer -= TICKS_PER_SECOND;
            sampleSecond(core);
        }
    }

    private void sampleSecond(Building core){
        MetricHistory h = history;
        boolean exact = core instanceof CoreFlowHook.TrackedCoreBuild;
        for(Item item : content.items()){
            int id = item.id;
            if(id >= lastStock.length){
                int old = lastStock.length;
                lastStock = java.util.Arrays.copyOf(lastStock, Math.max(id + 1, old * 2));
                java.util.Arrays.fill(lastStock, old, lastStock.length, -1);
            }
            int stock = core.items.get(item);
            int countedIn = CoreFlowHook.counter.takeIn(id), lost = CoreFlowHook.counter.takeLost(id);
            float estIn = deltas.takeIn(id), estOut = deltas.takeOut(id);
            int prev = lastStock[id];
            lastStock[id] = stock;
            if(!isTracked(item)) continue;
            h.record(ResourceStats.stockKey(item.name), stock);
            if(exact && prev >= 0){
                var flows = minclaude.stats.FlowCounter.reconcile(countedIn, stock - prev);
                h.record(ResourceStats.inKey(item.name), flows.in());
                h.record(ResourceStats.outKey(item.name), flows.out());
                h.record(ResourceStats.lostKey(item.name), lost);
            }else{
                h.record(ResourceStats.inKey(item.name), estIn);
                h.record(ResourceStats.outKey(item.name), estOut);
                if(exact) h.record(ResourceStats.lostKey(item.name), lost);
            }
        }
        scanner.scan(team(), h);
        h.tick();
        if(ModSettings.alerts()) evaluateAlerts(h);
    }

    private void evaluateAlerts(MetricHistory h){
        // Au moins une fenêtre d'analyse avant d'alerter, sinon la tendance n'a pas de sens.
        if(h.elapsedSeconds() < ResourceStats.DEFAULT_WINDOW) return;
        alerts.config.lowFraction = ModSettings.lowFraction();
        alerts.config.depletingSeconds = ModSettings.depletingSeconds();
        for(Item item : content.items()){
            if(!isTracked(item)) continue;
            for(var alert : alerts.evaluate(item.name, stats(item), h.elapsedSeconds())){
                alertListener.get(alert);
            }
        }
        evaluateBaseAlerts(scanner.last(), h.elapsedSeconds());
        for(Item item : content.items()){
            if(isTracked(item) && goals.check(item.name, core().items.get(item))){
                alertListener.get(new AlertEngine.Alert(item.name, AlertEngine.Type.GOAL_REACHED, goals.get(item.name)));
            }
        }
    }

    private void evaluateBaseAlerts(BaseScanner.Snapshot base, float now){
        var emit = (java.util.function.Consumer<java.util.List<AlertEngine.Alert>>)list -> list.forEach(alertListener::get);
        for(Item item : content.items()){
            int starved = base.industry().starvedBy(item.name);
            emit.accept(alerts.evaluateCondition(item.name, AlertEngine.Type.INDUSTRY_STARVED, starved > 0, starved, now));
        }
        var power = base.power();
        boolean shortage = power.networks() > 0 && power.consumedPerSecond() > 0 && power.satisfaction() < POWER_SHORTAGE_THRESHOLD;
        emit.accept(alerts.evaluateCondition("power", AlertEngine.Type.POWER_SHORTAGE, shortage, power.satisfaction(), now));
        int dry = base.defense().turretsNoAmmo();
        emit.accept(alerts.evaluateCondition("turrets", AlertEngine.Type.TURRETS_NO_AMMO, dry > 0, dry, now));
    }

    private void markTracked(int id){
        if(id >= tracked.length) tracked = java.util.Arrays.copyOf(tracked, Math.max(id + 1, tracked.length * 2));
        tracked[id] = true;
    }
}
