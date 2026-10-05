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
    private static final float TICKS_PER_SECOND = 60f;
    /** Alerte d'énergie quand moins de cette part de la demande est couverte. */
    private static final float POWER_SHORTAGE_THRESHOLD = 0.75f;

    private volatile MetricHistory history = new MetricHistory();
    private final DeltaAccumulator deltas = new DeltaAccumulator(64);
    private final AlertEngine alerts = new AlertEngine();
    private final BaseScanner scanner = new BaseScanner();
    private boolean[] tracked = new boolean[64];
    private float tickTimer;
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
        scanner.reset();
    }

    private void update(){
        if(!state.isPlaying()) return;
        CoreBuild core = core();
        if(core == null) return;

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
        for(Item item : content.items()){
            if(!isTracked(item)) continue;
            h.record(ResourceStats.stockKey(item.name), core.items.get(item));
            h.record(ResourceStats.inKey(item.name), deltas.takeIn(item.id));
            h.record(ResourceStats.outKey(item.name), deltas.takeOut(item.id));
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
