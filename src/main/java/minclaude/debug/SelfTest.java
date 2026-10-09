package minclaude.debug;

import arc.*;
import arc.files.Fi;
import arc.graphics.*;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.struct.Seq;
import arc.util.*;
import minclaude.MinClaudeMod;
import minclaude.content.*;
import minclaude.stats.*;
import mindustry.content.Items;
import mindustry.game.EventType.Trigger;
import mindustry.game.Gamemode;
import mindustry.type.Item;
import mindustry.type.UnitType;

import static mindustry.Vars.*;


/**
 * Recette automatique de l'interface dans le vrai client (rendu réel), sans clic humain.
 * Activée seulement si la variable d'environnement {@code MINCLAUDE_SELFTEST} contient un dossier de sortie.
 * Lance Ground Zero, injecte un historique synthétique, ouvre chaque écran, enregistre des captures PNG
 * et un rapport texte ({@code report.txt}), puis quitte le jeu.
 */
public final class SelfTest{
    private record Step(float delay, String name, Runnable action){}

    private final Fi out = new Fi(OS.env("MINCLAUDE_SELFTEST"));
    private final Seq<Step> steps = new Seq<>();
    private final StringBuilder report = new StringBuilder();
    private int index;
    private float timer;
    private String pendingShot;
    private int failures;

    public static boolean enabled(){
        String v = OS.env("MINCLAUDE_SELFTEST");
        return v != null && !v.isEmpty();
    }

    public void start(){
        out.mkdirs();
        line("MinClaude autotest — écran " + Core.graphics.getWidth() + "x" + Core.graphics.getHeight());

        step(3f, "partie", () -> {
            var map = maps.loadInternalMap("serpulo/groundZero");
            control.playMap(map, map.applyRules(Gamemode.survival));
        });
        step(4f, "historique", this::injectHistory);
        step(2f, "reprise", () -> {
            // Rafraîchissement du HUD pendant la pause : on vérifie que le panneau se met à jour malgré tout.
            check("partie en pause", state.isPaused());
        });
        step(1f, "hud", () -> {
            shot("01-hud");
            checkOverlay();
        });
        step(0.5f, "alerte", () -> MinClaudeMod.showAlert(new AlertEngine.Alert(Items.copper.name, AlertEngine.Type.DEPLETING, 200f)));
        step(1f, "alerte-capture", () -> shot("02-alerte"));
        step(1f, "dashboard", MinClaudeMod::toggleDashboard);
        step(1.5f, "dashboard-10min", () -> {
            check("dashboard ouvert", MinClaudeMod.dashboard.isShown());
            shot("03-dashboard-10min");
        });
        step(0.5f, "dashboard-1h", () -> MinClaudeMod.dashboard.setRange(TimeRange.HOUR));
        step(1.5f, "dashboard-1h-capture", () -> shot("04-dashboard-1h"));
        step(0.5f, "dashboard-partie", () -> {
            MinClaudeMod.dashboard.setRange(TimeRange.WHOLE_GAME);
            MinClaudeMod.dashboard.select(Items.lead);
        });
        step(1.5f, "dashboard-partie-capture", () -> shot("05-dashboard-partie-plomb"));
        step(0.5f, "dashboard-1min", () -> {
            MinClaudeMod.dashboard.setRange(TimeRange.MINUTE);
            MinClaudeMod.dashboard.select(MCItems.cobalt);
        });
        step(1.5f, "dashboard-1min-capture", () -> shot("06-dashboard-1min-cobalt"));
        // Régression : la courbe du charbon (couleur presque noire) doit rester visible.
        step(0.5f, "dashboard-charbon", () -> {
            MinClaudeMod.dashboard.setRange(TimeRange.TEN_MINUTES);
            MinClaudeMod.dashboard.select(Items.coal);
        });
        step(1.5f, "dashboard-charbon-capture", () -> shot("06b-dashboard-charbon"));
        step(0.5f, "fermeture", MinClaudeMod::toggleDashboard);
        // Après l'animation de fermeture.
        step(1f, "fermeture-vérif", () -> check("dashboard fermé", !MinClaudeMod.dashboard.isShown()));
        step(0.5f, "options-jeu", () -> {
            ui.settings.show();
            Reflect.invoke(ui.settings, "visible", new Object[]{0}, int.class);
        });
        step(1.5f, "options-jeu-capture", () -> shot("07a-options-jeu-reference"));
        step(0.5f, "options", () -> {
            int category = 4 + ui.settings.getCategories().indexOf(c -> c.name.equals(Core.bundle.get("minclaude.settings")));
            check("catégorie d'options présente", category >= 4);
            Reflect.invoke(ui.settings, "visible", new Object[]{category}, int.class);
        });
        step(1.5f, "options-capture", () -> shot("07-options"));
        step(0.5f, "commandes", () -> {
            ui.settings.hide();
            check("raccourci enregistré", arc.input.KeyBind.all.contains(MinClaudeMod.dashboardKey));
            ui.controls.show();
        });
        step(1f, "commandes-défilement", () -> {
            // La catégorie MinClaude est en bas de la liste.
            arc.scene.ui.ScrollPane pane = ui.controls.find(e -> e instanceof arc.scene.ui.ScrollPane);
            check("liste des commandes trouvée", pane != null);
            pane.layout();
            pane.setScrollPercentY(1f);
            pane.updateVisualScroll();
        });
        step(1f, "commandes-capture", () -> shot("08-commandes"));
        step(0.5f, "fiche-fonderie", () -> {
            ui.controls.hide();
            ui.content.show(MCBlocks.cobaltSmelter);
        });
        step(1.5f, "fiche-fonderie-capture", () -> shot("09-fiche-fonderie"));
        step(0.5f, "fiche-mur", () -> {
            ui.content.hide();
            ui.content.show(MCBlocks.cobaltWallLarge);
        });
        step(1.5f, "fiche-mur-capture", () -> shot("10-fiche-grand-mur"));
        step(0.5f, "fiche-cobalt", () -> {
            ui.content.hide();
            ui.content.show(MCItems.cobalt);
        });
        step(1.5f, "fiche-cobalt-capture", () -> shot("11-fiche-cobalt"));
        // ---- V1 : base de démonstration, onglets Énergie / Industries / Défense, contenu en jeu ----
        step(0.5f, "base-demo", () -> {
            ui.content.hide();
            buildDemoBase();
            buildDemoBaseV3();
            spawnDemoUnits();
            state.set(mindustry.core.GameState.State.playing);
        });
        step(3f, "base-pause", () -> {
            state.set(mindustry.core.GameState.State.paused);
            MinClaudeMod.tracker.scanNow();
            var base = MinClaudeMod.tracker.base();
            check("usines relevées", base.industry().total() >= 4);
            check("goulot détecté (bauxite)", base.industry().starvedBy(MCItems.bauxite.name) > 0);
            check("énergie relevée", base.power().networks() > 0);
            check("tourelle sans munitions relevée", base.defense().turretsNoAmmo() > 0);
            check("unités alliées du mod relevées", base.defense().units().stream().anyMatch(e -> e.getKey().equals(MCUnits.warden.name)));
            check("capacité installée relevée (fonderie de laiton)", base.production().flow(MCItems.brass.name) != null);
            check("foreuse hors gisement = sans entrée", base.industry().block(MCBlocks.percussionDrill.name) != null
                && base.industry().block(MCBlocks.percussionDrill.name).count(minclaude.logic.IndustryStatus.NO_INPUT) == 1);
        });
        for(String tab : new String[]{"power", "industry", "defense"}){
            step(0.5f, "onglet-" + tab, () -> {
                if(!MinClaudeMod.dashboard.isShown()) MinClaudeMod.toggleDashboard();
                MinClaudeMod.dashboard.showTab(tab);
                check("onglet " + tab + " affiché", tab.equals(MinClaudeMod.dashboard.currentTab()));
            });
            step(1.5f, "onglet-" + tab + "-capture", () -> shot("12-onglet-" + tab));
        }
        step(0.5f, "objectif", () -> {
            MinClaudeMod.tracker.goals().set(Items.lead.name, 3000);
            MinClaudeMod.dashboard.select(Items.lead);
            check("objectif enregistré", MinClaudeMod.tracker.goals().get(Items.lead.name) == 3000);
        });
        step(1.5f, "objectif-capture", () -> shot("12b-ressources-objectif"));
        step(0.5f, "monde-base", () -> {
            MinClaudeMod.toggleDashboard();
            lookAt(demoX * tilesize, demoY * tilesize + 2 * tilesize);
        });
        step(2f, "monde-base-capture", () -> shot("13-base-demo"));
        step(0.5f, "monde-base-v2", () -> lookAt(demoX * tilesize, (demoY + 7) * tilesize));
        step(2f, "monde-base-v2-capture", () -> shot("13b-base-demo-v2"));
        step(0.5f, "monde-base-v3", () -> {
            if(v3X >= 0) lookAt((v3X + 11) * tilesize, (v3Y + 6) * tilesize);
        });
        step(2f, "monde-base-v3-capture", () -> shot("13c-base-demo-v3"));
        step(0.5f, "adaptation", () -> {
            MinClaudeMod.waves.adapt();
            check("adaptation des vagues déclenchée (peu d'anti-aérien)", MinClaudeMod.waves.current().bonus(minclaude.logic.AdaptiveWaves.Family.AIR) > 0);
            MinClaudeMod.toggleDashboard();
            MinClaudeMod.dashboard.showTab("defense");
        });
        step(1.5f, "adaptation-capture", () -> {
            shot("12c-defense-adaptation");
        });
        step(0.5f, "adaptation-fermeture", MinClaudeMod::toggleDashboard);
        // ---- V5 : énergie et bâtiments utilitaires (après l'adaptation : la Tempête compte comme anti-aérien) ----
        step(0.5f, "regions-atlas", this::checkRegions);
        step(0.5f, "monde-base-v5", () -> {
            buildDemoBaseV5();
            if(v5X >= 0) lookAt((v5X + 9) * tilesize, (v5Y + 5) * tilesize);
            state.set(mindustry.core.GameState.State.playing);
        });
        step(2f, "monde-base-v5-pause", () -> {
            state.set(mindustry.core.GameState.State.paused);
            var turbine = world.build(v5X + 11, v5Y + 2);
            check("turbine industrielle en marche", turbine instanceof mindustry.world.blocks.power.ConsumeGenerator.ConsumeGeneratorBuild g && g.productionEfficiency > 0.9f);
            var battery = world.build(v5X + 5, v5Y + 6);
            check("batterie en invar chargée par la turbine", battery != null && battery.power.graph.getBatteryStored() > 0);
        });
        step(1f, "monde-base-v5-capture", () -> shot("13d-base-demo-v5"));
        step(0.5f, "fiche-tempete", () -> ui.content.show(MCBlocks.tempest));
        step(1.5f, "fiche-tempete-capture", () -> shot("26-fiche-tempete"));
        step(0.5f, "fiche-tempete-fermeture", () -> ui.content.hide());
        step(0.5f, "monde-minerais", () -> {
            var ore = nearestModOre();
            check("minerais du mod présents sur la carte", ore != null);
            if(ore != null) lookAt(ore.worldx(), ore.worldy());
        });
        step(2f, "monde-minerais-capture", () -> shot("14-minerais"));
        step(0.5f, "fiche-rivet", () -> ui.content.show(MCBlocks.rivet));
        step(1.5f, "fiche-rivet-capture", () -> shot("15-fiche-riveteuse"));
        step(0.5f, "fiche-gardien", () -> {
            ui.content.hide();
            ui.content.show(MCUnits.warden);
        });
        step(1.5f, "fiche-gardien-capture", () -> shot("16-fiche-gardien"));
        step(0.5f, "fiche-maraudeur", () -> {
            ui.content.hide();
            ui.content.show(MCUnits.marauder);
        });
        step(1.5f, "fiche-maraudeur-capture", () -> shot("17-fiche-maraudeur"));
        step(0.5f, "fiche-salve", () -> {
            ui.content.hide();
            ui.content.show(MCBlocks.volley);
        });
        step(1.5f, "fiche-salve-capture", () -> shot("19-fiche-salve"));
        step(0.5f, "fiche-bastion", () -> {
            ui.content.hide();
            ui.content.show(MCUnits.bastion);
        });
        step(1.5f, "fiche-bastion-capture", () -> shot("20-fiche-bastion"));
        step(0.5f, "fiche-azote", () -> {
            ui.content.hide();
            ui.content.show(MCLiquids.nitrogen);
        });
        step(1.5f, "fiche-azote-capture", () -> shot("21-fiche-azote"));
        step(0.5f, "fiche-canon", () -> {
            ui.content.hide();
            ui.content.show(MCBlocks.railgun);
        });
        step(1.5f, "fiche-canon-capture", () -> shot("22-fiche-canon-electrique"));
        step(0.5f, "fiche-colosse", () -> {
            ui.content.hide();
            ui.content.show(MCUnits.colossus);
        });
        step(1.5f, "fiche-colosse-capture", () -> shot("23-fiche-colosse"));
        step(0.5f, "fiche-seigneur", () -> {
            ui.content.hide();
            ui.content.show(MCUnits.warlord);
        });
        step(1.5f, "fiche-seigneur-capture", () -> shot("24-fiche-seigneur"));
        step(0.5f, "fiche-fregate", () -> {
            ui.content.hide();
            ui.content.show(MCUnits.frigate);
        });
        step(1.5f, "fiche-fregate-capture", () -> shot("25-fiche-fregate"));
        // ---- V4 : performance dans le client réel, base de démo en combat ----
        step(0.5f, "perf-debut", () -> {
            ui.content.hide();
            frames.clear();
            measuring = true;
            state.set(mindustry.core.GameState.State.playing);
        });
        step(5f, "perf-fin", () -> {
            measuring = false;
            state.set(mindustry.core.GameState.State.paused);
            frames.sort();
            float avg = 0;
            for(int i = 0; i < frames.size; i++) avg += frames.get(i);
            avg /= Math.max(1, frames.size);
            float p95 = frames.isEmpty() ? 0 : frames.get((int)(frames.size * 0.95f));
            line(String.format(java.util.Locale.ROOT, "info perf : %d images, %.1f ms en moyenne (%.0f i/s), 95e centile %.1f ms, %d unités, %d bâtiments",
                frames.size, avg * 1000, 1f / Math.max(avg, 1e-4f), p95 * 1000, mindustry.gen.Groups.unit.size(), mindustry.gen.Groups.build.size()));
            check("fluidité en combat (≥ 30 i/s en moyenne)", avg > 0 && 1f / avg >= 30f);
        });
        step(0.5f, "options-v1", () -> {
            ui.content.hide();
            ui.settings.show();
            int category = 4 + ui.settings.getCategories().indexOf(c -> c.name.equals(Core.bundle.get("minclaude.settings")));
            Reflect.invoke(ui.settings, "visible", new Object[]{category}, int.class);
        });
        step(1.5f, "options-v1-capture", () -> shot("18-options-v1"));
        step(0.5f, "fermeture-options", () -> ui.settings.hide());
        step(1f, "fin", this::finish);

        Events.run(Trigger.uiDrawEnd, this::capture);
        Core.app.addListener(new ApplicationListener(){
            @Override
            public void update(){
                tick();
            }
        });
    }

    private void step(float delay, String name, Runnable action){
        steps.add(new Step(delay, name, action));
    }

    private final arc.struct.FloatSeq frames = new arc.struct.FloatSeq();
    private boolean measuring;

    private void tick(){
        if(measuring) frames.add(Core.graphics.getDeltaTime());
        if(index >= steps.size) return;
        timer += Core.graphics.getDeltaTime();
        Step s = steps.get(index);
        if(timer < s.delay) return;
        timer = 0f;
        index++;
        try{
            s.action.run();
            line("ok   étape " + s.name);
        }catch(Throwable t){
            failures++;
            line("FAIL étape " + s.name + " : " + Strings.getStackTrace(t));
        }
    }

    private int demoX, demoY;

    /** Un exemplaire de chaque bâtiment du mod près du noyau, dans des états variés (sans entrée, sans énergie…). */
    private void buildDemoBase(){
        var core = MinClaudeMod.tracker.core();
        var team = state.rules.defaultTeam;
        mindustry.world.Tile spot = null;
        for(int r = 6; r < 50 && spot == null; r++){
            for(int dx = -r; dx <= r && spot == null; dx += 2){
                if(free(core.tile.x + dx, core.tile.y + r, 18, 12)) spot = world.tile(core.tile.x + dx, core.tile.y + r);
            }
        }
        check("zone libre pour la base de démo", spot != null);
        if(spot == null) return;
        int x = spot.x, y = spot.y;
        demoX = x + 9;
        demoY = y + 3;
        // Ligne 1 : fonderie d'aluminium sans entrée + panneau solaire (goulot bauxite/charbon)
        put(MCBlocks.aluminumSmelter, x + 1, y + 1, team);
        put(mindustry.content.Blocks.solarPanel, x + 3, y + 1, team);
        put(mindustry.content.Blocks.solarPanel, x + 3, y, team);
        // fonderie de laiton approvisionnée mais sans énergie
        var brass = put(MCBlocks.brassFoundry, x + 6, y + 1, team);
        if(brass != null){
            brass.items.add(Items.copper, 10);
            brass.items.add(MCItems.zinc, 10);
        }
        put(MCBlocks.oreWasher, x + 9, y + 1, team);
        put(MCBlocks.percussionDrill, x + 12, y + 1, team);
        put(MCBlocks.rivet, x + 15, y + 1, team);
        // Ligne 2 : murs, convoyeur renforcé, nœud
        put(MCBlocks.nickelWall, x, y + 4, team);
        put(MCBlocks.cobaltWall, x + 1, y + 4, team);
        put(MCBlocks.nickelWallLarge, x + 3, y + 4, team);
        put(MCBlocks.cobaltWallLarge, x + 5, y + 4, team);
        for(int i = 0; i < 6; i++) put(MCBlocks.reinforcedConveyor, x + 7 + i, y + 4, team);
        put(MCBlocks.aluminumNode, x + 14, y + 4, team);

        // V2, ligne 3 : industries et défense
        put(MCBlocks.steelFurnace, x + 1, y + 7, team);
        put(MCBlocks.alloyPress, x + 4, y + 7, team);
        put(MCBlocks.brineMixer, x + 7, y + 7, team);
        put(MCBlocks.cryogenizer, x + 10, y + 7, team);
        put(MCBlocks.electrolyzer, x + 13, y + 7, team);
        put(MCBlocks.volley, x + 16, y + 7, team);
        // V2, ligne 4 : murs, conteneur, tourelle Givre, convoyeur blindé avec virage et jonction
        put(MCBlocks.steelWall, x, y + 10, team);
        put(MCBlocks.steelWallLarge, x + 2, y + 10, team);
        put(MCBlocks.invarContainer, x + 5, y + 10, team);
        put(MCBlocks.frost, x + 8, y + 10, team);
        for(int i = 0; i < 4; i++) putRotated(MCBlocks.platedConveyor, x + 10 + i, y + 10, 0, team);
        putRotated(MCBlocks.platedConveyor, x + 14, y + 10, 1, team);  // virage vers le haut
        putRotated(MCBlocks.platedConveyor, x + 14, y + 11, 1, team);
        putRotated(MCBlocks.reinforcedConveyor, x + 12, y + 9, 1, team); // entrée latérale par le bas
        putRotated(MCBlocks.reinforcedConveyor, x + 12, y + 11, 3, team); // entrée latérale par le haut
    }

    private int v3X = -1, v3Y = -1;

    /** V3 : zone séparée (le contenu est plus gros), cherchée autour du noyau. */
    private void buildDemoBaseV3(){
        var core = MinClaudeMod.tracker.core();
        var team = state.rules.defaultTeam;
        // Toute la carte, de la plus proche du noyau à la plus lointaine, sans chevaucher la première démo (18x12).
        float best = Float.MAX_VALUE;
        for(int x = 1; x < world.width() - 23; x++){
            for(int y = 1; y < world.height() - 10; y++){
                boolean overlaps = x < demoX - 9 + 18 + 2 && x + 22 + 2 > demoX - 9 && y < demoY - 3 + 12 + 2 && y + 9 + 2 > demoY - 3;
                if(overlaps) continue;
                float d = core.tile.dst(world.tile(x, y));
                if(d < best && free(x, y, 22, 9)){
                    best = d;
                    v3X = x;
                    v3Y = y;
                }
            }
        }
        check("zone libre pour la démo V3", v3X >= 0);
        if(v3X < 0) return;
        int x = v3X, y = v3Y;
        put(MCBlocks.duraluminForge, x + 1, y + 1, team);
        put(MCBlocks.acidPlant, x + 4, y + 1, team);
        put(MCBlocks.cermetKiln, x + 7, y + 1, team);
        put(MCBlocks.carbonWeaver, x + 10, y + 1, team);
        put(MCBlocks.quantumResonator, x + 14, y + 1, team);
        put(MCBlocks.railgun, x + 18, y + 1, team);
        put(MCBlocks.cermetWall, x, y + 5, team);
        put(MCBlocks.cermetWallLarge, x + 2, y + 5, team);
        putRotated(MCBlocks.duraluminBridge, x + 5, y + 5, 0, team);
        putRotated(MCBlocks.duraluminBridge, x + 11, y + 5, 0, team);
        var bridge = world.build(x + 5, y + 5);
        if(bridge != null) bridge.configureAny(world.tile(x + 11, y + 5).pos());
        // Anti-sol seulement : l'ennemi s'adapte (plus de volants).
        for(int i = 0; i < 9; i++) put(mindustry.content.Blocks.hail, x + 13 + i, y + 6, team);
        for(int i = 0; i < 9; i++) put(mindustry.content.Blocks.hail, x + 13 + i, y + 7, team);
    }

    private int v5X = -1, v5Y = -1;

    /** V5 : énergie et soutien en marche (turbine, batteries, cuve pleine, nœud longue portée relié). */
    private void buildDemoBaseV5(){
        var core = MinClaudeMod.tracker.core();
        var team = state.rules.defaultTeam;
        float best = Float.MAX_VALUE;
        for(int x = 1; x < world.width() - 19; x++){
            for(int y = 1; y < world.height() - 10; y++){
                boolean overDemo = x < demoX - 9 + 18 + 2 && x + 18 + 2 > demoX - 9 && y < demoY - 3 + 12 + 2 && y + 10 + 2 > demoY - 3;
                boolean overV3 = v3X >= 0 && x < v3X + 22 + 2 && x + 18 + 2 > v3X && y < v3Y + 12 + 2 && y + 10 + 2 > v3Y;
                if(overDemo || overV3) continue;
                float d = core.tile.dst(world.tile(x, y));
                if(d < best && free(x, y, 18, 10)){
                    best = d;
                    v5X = x;
                    v5Y = y;
                }
            }
        }
        check("zone libre pour la démo V5", v5X >= 0);
        if(v5X < 0) return;
        int x = v5X, y = v5Y;
        var tank = put(MCBlocks.largeLiquidTank, x + 2, y + 2, team);
        var capacitor = put(MCBlocks.quantumCapacitor, x + 7, y + 2, team);
        var turbine = put(MCBlocks.industrialTurbine, x + 11, y + 2, team);
        var tempest = put(MCBlocks.tempest, x + 15, y + 2, team);
        var dome = put(MCBlocks.restorationDome, x + 2, y + 7, team);
        var battery = put(MCBlocks.invarBattery, x + 5, y + 6, team);
        var brine = put(MCBlocks.brineGenerator, x + 8, y + 6, team);
        var node = put(MCBlocks.longRangeNode, x + 11, y + 6, team);
        if(tank != null) tank.liquids.add(mindustry.content.Liquids.water, 2500f);
        if(turbine != null){
            turbine.items.add(Items.coal, 10);
            turbine.liquids.add(mindustry.content.Liquids.water, 60f);
        }
        if(brine != null) brine.liquids.add(MCLiquids.brine, 30f);
        if(tempest != null) tempest.handleStack(MCItems.steel, 20, null);
        // Le nœud longue portée relie la turbine, le générateur, les batteries et le dôme.
        if(node != null){
            for(var b : new mindustry.gen.Building[]{turbine, brine, battery, capacitor, dome}){
                if(b != null) node.configureAny(b.pos());
            }
        }
    }

    /** Toutes les régions que le jeu cherche pour les blocs du mod sont dans le vrai atlas (pas de texture d'erreur). */
    private void checkRegions(){
        int missing = 0;
        for(var b : MCBlocks.all){
            java.util.List<String> names = new java.util.ArrayList<>(java.util.List.of(b instanceof mindustry.world.blocks.environment.OreBlock ? b.name + "1" : b.name));
            if(b instanceof mindustry.world.blocks.power.Battery || b instanceof mindustry.world.blocks.defense.MendProjector) names.add(b.name + "-top");
            if(b instanceof mindustry.world.blocks.liquid.LiquidRouter) names.add(b.name + "-bottom");
            if(MCBlocks.rotors.contains(b)) names.add(b.name + "-rotator");
            if(MCBlocks.glowing.contains(b)) names.add(b.name + "-glow");
            if(b instanceof mindustry.world.blocks.defense.turrets.ItemTurret) names.add(b.name + "-heat");
            for(String n : names){
                if(!Core.atlas.has(n)){
                    missing++;
                    line("info région absente : " + n);
                }
            }
        }
        check("régions des blocs du mod dans l'atlas", missing == 0);
    }

    private static void putRotated(mindustry.world.Block block, int x, int y, int rotation, mindustry.game.Team team){
        var t = world.tile(x, y);
        if(t != null) t.setNet(block, team, rotation);
    }

    private void spawnDemoUnits(){
        float wx = demoX * tilesize, wy = (demoY + 4) * tilesize;
        MCUnits.warden.spawn(state.rules.defaultTeam, wx - 24f, wy);
        MCUnits.aid.spawn(state.rules.defaultTeam, wx - 8f, wy + 8f);
        MCUnits.marauder.spawn(state.rules.waveTeam, wx + 16f, wy);
        MCUnits.wasp.spawn(state.rules.waveTeam, wx + 32f, wy + 8f);
        float vy = wy + 7 * tilesize;
        MCUnits.sentinel.spawn(state.rules.defaultTeam, wx - 40f, vy);
        MCUnits.bastion.spawn(state.rules.defaultTeam, wx - 16f, vy);
        MCUnits.relay.spawn(state.rules.defaultTeam, wx + 4f, vy + 10f);
        MCUnits.ravager.spawn(state.rules.waveTeam, wx + 30f, vy);
        MCUnits.hornet.spawn(state.rules.waveTeam, wx + 52f, vy + 10f);
        MCUnits.brute.spawn(state.rules.waveTeam, wx + 80f, vy);
        if(v3X >= 0){
            float ux = (v3X + 2) * tilesize, uy = (v3Y + 9) * tilesize;
            UnitType[] v3 = {MCUnits.scout, MCUnits.engineer, MCUnits.citadel, MCUnits.colossus, MCUnits.halo};
            for(int i = 0; i < v3.length; i++) v3[i].spawn(state.rules.defaultTeam, ux + i * 36f, uy);
            UnitType[] foes = {MCUnits.swarmling, MCUnits.stalker, MCUnits.juggernaut, MCUnits.warlord, MCUnits.leviathan, MCUnits.dreadwing};
            for(int i = 0; i < foes.length; i++) foes[i].spawn(state.rules.waveTeam, ux + i * 44f, uy + 70f);
        }
    }

    private static boolean free(int x, int y, int w, int h){
        for(int i = 0; i < w; i++){
            for(int j = 0; j < h; j++){
                var t = world.tile(x + i, y + j);
                // Hors des zones sombres du bord de carte, où rien n'est visible.
                // Les rochers décoratifs (placés au hasard au chargement) comptent comme libres : la pose les remplace.
                // Pas les murs de roche (StaticWall hérite de Prop mais n'est pas remplaçable).
                boolean freeBlock = t != null && (t.block() == mindustry.content.Blocks.air || (t.block() instanceof mindustry.world.blocks.environment.Prop && t.block().alwaysReplace));
                if(t == null || !freeBlock || t.floor().isLiquid || t.floor().solid
                    || t.floor() == mindustry.content.Blocks.empty || world.getDarkness(x + i, y + j) > 0) return false;
            }
        }
        return true;
    }

    private static mindustry.gen.Building put(mindustry.world.Block block, int x, int y, mindustry.game.Team team){
        var t = world.tile(x, y);
        if(t == null) return null;
        t.setNet(block, team, 0);
        return t.build;
    }

    private static mindustry.world.Tile nearestModOre(){
        var core = MinClaudeMod.tracker.core();
        mindustry.world.Tile[] best = {null};
        float[] dst = {Float.MAX_VALUE};
        world.tiles.eachTile(t -> {
            if(MCBlocks.ores.contains(o -> o == t.overlay())){
                float d = core.dst(t);
                if(d < dst[0]){
                    dst[0] = d;
                    best[0] = t;
                }
            }
        });
        return best[0];
    }

    /** Centre la vue : la caméra suit l'unité du joueur, on déplace donc les deux. */
    private static void lookAt(float x, float y){
        if(player.unit() != null) player.unit().set(x, y);
        Core.camera.position.set(x, y);
    }

    /** 15 min de données artificielles mais réalistes, pour remplir graphiques et mini-panneau. */
    private void injectHistory(){
        var core = MinClaudeMod.tracker.core();
        check("noyau présent", core != null);
        MetricHistory h = new MetricHistory();
        int seconds = 900;
        Item[] items = {Items.copper, Items.lead, Items.sand, Items.graphite, Items.coal, MCItems.cobalt};
        for(int t = 0; t < seconds; t++){
            for(Item item : items){
                float stock = stockAt(item, t), prev = t == 0 ? stock : stockAt(item, t - 1);
                float delta = stock - prev;
                // Flux de fond dans les deux sens, plus la variation nette du côté qui convient.
                float in = inBase(item) + Math.max(0, delta);
                float out = inBase(item) + Math.max(0, -delta);
                h.record(ResourceStats.stockKey(item.name), stock);
                h.record(ResourceStats.inKey(item.name), in);
                h.record(ResourceStats.outKey(item.name), out);
            }
            h.tick();
        }
        for(Item item : items) core.items.set(item, (int)stockAt(item, seconds - 1));
        MinClaudeMod.tracker.setHistory(h);
        // La pause fige l'historique pour des captures reproductibles.
        state.set(mindustry.core.GameState.State.paused);
    }

    private static float stockAt(Item item, int t){
        if(item == Items.copper) return Math.max(0, 3000 - t * 3.1f);
        if(item == Items.lead) return 400 + t * 2.2f;
        if(item == Items.sand) return 3900 + 80 * Mathf.sin(t / 30f);
        if(item == Items.coal) return 1500 - t * 0.9f;
        if(item == Items.graphite) return 800 + 300 * Mathf.sin(t / 90f) + t * 0.2f;
        return t < 300 ? 0 : (t - 300) * 0.8f;
    }

    private static float inBase(Item item){
        return item == Items.copper ? 4f : item == Items.lead ? 1f : 2f;
    }

    private void checkOverlay(){
        Element root = ui.hudGroup.find("minclaude-overlay");
        check("mini-panneau présent", root != null);
        if(root == null) return;
        check("mini-panneau visible", root.visible);
        Element panel = ((arc.scene.Group)root).getChildren().first();
        Vec2 p = panel.localToStageCoordinates(new Vec2(0, 0));
        float w = Core.graphics.getWidth(), hh = Core.graphics.getHeight();
        line("info mini-panneau x=" + (int)p.x + " y=" + (int)p.y + " w=" + (int)panel.getWidth() + " h=" + (int)panel.getHeight());
        check("mini-panneau dans l'écran", p.x >= 0 && p.y >= 0 && p.x + panel.getWidth() <= w && p.y + panel.getHeight() <= hh);
        check("mini-panneau a des lignes", panel.getHeight() > 60f);
    }

    private void shot(String name){
        pendingShot = name;
    }

    /** Capture après le dessin de l'interface, sinon l'image serait incomplète. */
    private void capture(){
        if(pendingShot == null) return;
        String name = pendingShot;
        pendingShot = null;
        int w = Core.graphics.getBackBufferWidth(), h = Core.graphics.getBackBufferHeight();
        Pixmap pix = ScreenUtils.getFrameBufferPixmap(0, 0, w, h, true);
        PixmapIO.writePng(out.child(name + ".png"), pix);
        pix.dispose();
        line("shot " + name + ".png");
    }

    private void check(String what, boolean ok){
        if(!ok) failures++;
        line((ok ? "ok   " : "FAIL ") + what);
    }

    private void line(String s){
        report.append(s).append('\n');
        Log.info("[MinClaude selftest] @", s);
        out.child("report.txt").writeString(report.toString());
    }

    private void finish(){
        line(failures == 0 ? "RÉSULTAT : OK" : "RÉSULTAT : " + failures + " échec(s)");
        Core.app.exit();
    }
}
