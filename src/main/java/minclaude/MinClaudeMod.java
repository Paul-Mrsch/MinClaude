package minclaude;

import arc.Core;
import arc.Events;
import arc.input.KeyBind;
import arc.input.KeyCode;
import arc.util.Log;
import minclaude.ai.SmartAI;
import minclaude.ai.SquadManager;
import minclaude.ai.WaveAdapter;
import minclaude.content.*;
import minclaude.debug.SelfTest;
import minclaude.stats.Format;
import minclaude.tracking.*;
import minclaude.ui.*;
import minclaude.world.WorldSetup;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.io.SaveVersion;
import mindustry.mod.Mod;

import static mindustry.Vars.*;

/** Point d'entrée du mod MinClaude. */
public class MinClaudeMod extends Mod{
    public static final ResourceTracker tracker = new ResourceTracker();
    public static final SquadManager squads = new SquadManager();
    public static final WaveAdapter waves = new WaveAdapter();
    public static DashboardDialog dashboard;
    public static KeyBind dashboardKey;

    public MinClaudeMod(){
        tracker.register();
        WorldSetup.register();
        SaveVersion.addCustomChunk(ResourceTracker.CHUNK_NAME, new HistoryChunk(tracker));
        SaveVersion.addCustomChunk(ResourceTracker.GOALS_CHUNK_NAME, new GoalsChunk(tracker.goals()));
        squads.register();
        waves.register();

        Events.on(ClientLoadEvent.class, e -> {
            ModSettings.register();
            dashboard = new DashboardDialog(tracker);
            new ResourceOverlay(tracker, MinClaudeMod::toggleDashboard).build();
            tracker.onAlert(MinClaudeMod::showAlert);
            if(SelfTest.enabled()) new SelfTest().start();
        });

        Events.run(Trigger.update, () -> {
            if(dashboardKey != null && state.isGame() && !Core.scene.hasField() && Core.input.keyTap(dashboardKey)){
                toggleDashboard();
            }
        });
    }

    @Override
    public void init(){
        int cores = CoreFlowHook.install();
        Log.info("[MinClaude] Mesure exacte des flux sur @ types de noyau.", cores);
        int smart = SmartAI.install();
        Log.info("[MinClaude] IA intelligente installée sur @ types d'unités.", smart);
        if(headless) return;
        dashboardKey = KeyBind.add("minclaude-dashboard", KeyCode.k, "minclaude");
        dashboardKey.load();
    }

    @Override
    public void loadContent(){
        MCItems.load();
        MCLiquids.load();
        MCBlocks.load();
        MCUnits.load();
        MCTechTree.load();
        Log.info("[MinClaude] Contenu chargé : @ ressources, @ liquides, @ blocs, @ unités.", MCItems.all.size, MCLiquids.all.size, MCBlocks.all.size, MCUnits.all.size);
    }

    public static void toggleDashboard(){
        if(dashboard == null) return;
        if(dashboard.isShown()) dashboard.hide();
        else dashboard.show();
    }

    public static void showAlert(minclaude.stats.AlertEngine.Alert alert){
        var item = content.item(alert.item());
        String name = item == null ? alert.item() : item.localizedName;
        String text = switch(alert.type()){
            case DEPLETING -> Core.bundle.format("minclaude.alert.depleting", name, Format.duration(alert.value()));
            case LOW_STOCK -> Core.bundle.format("minclaude.alert.low", name, Format.amount(alert.value()));
            case CORE_FULL -> Core.bundle.format("minclaude.alert.full", name);
            case INDUSTRY_STARVED -> Core.bundle.format("minclaude.alert.starved", (int)alert.value(), name);
            case POWER_SHORTAGE -> Core.bundle.format("minclaude.alert.power", (int)(alert.value() * 100));
            case TURRETS_NO_AMMO -> Core.bundle.format("minclaude.alert.ammo", (int)alert.value());
            case GOAL_REACHED -> Core.bundle.format("minclaude.alert.goal", name, Format.amount(alert.value()));
        };
        ui.hudfrag.showToast(Icon.warning, text);
    }
}
