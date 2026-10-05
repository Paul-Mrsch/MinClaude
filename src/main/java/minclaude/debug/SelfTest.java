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

    private void tick(){
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
