package minclaude.tracking;

import arc.Events;
import arc.func.Cons;
import arc.util.Time;
import minclaude.ModSettings;
import minclaude.stats.*;
import mindustry.game.EventType.*;
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

    private volatile MetricHistory history = new MetricHistory();
    private final DeltaAccumulator deltas = new DeltaAccumulator(64);
    private final AlertEngine alerts = new AlertEngine();
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

    /** Noyau suivi : celui de l'équipe du joueur, ou de l'équipe par défaut sans joueur (tests headless). */
    public CoreBuild core(){
        return player != null ? player.team().core() : state.rules.defaultTeam.core();
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
    }

    private void markTracked(int id){
        if(id >= tracked.length) tracked = java.util.Arrays.copyOf(tracked, Math.max(id + 1, tracked.length * 2));
        tracked[id] = true;
    }
}
