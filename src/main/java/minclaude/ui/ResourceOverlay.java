package minclaude.ui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import minclaude.ModSettings;
import minclaude.stats.*;
import minclaude.tracking.ResourceTracker;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.ui.Styles;

import static mindustry.Vars.*;

/**
 * Mini-panneau permanent du HUD, à droite de l'écran : les ressources les plus critiques (alertes d'abord,
 * puis les plus fortes baisses) avec stock, débit net et échéance. Un bouton ouvre le dashboard complet.
 */
public class ResourceOverlay{
    private static final float REFRESH_SECONDS = 0.5f;
    private static final float MINIMAP_CLEARANCE = 160f;
    private static final float PANEL_WIDTH = 300f;

    private final ResourceTracker tracker;
    private final Runnable openDashboard;
    private final Table rows = new Table();
    /** Temps réel écoulé : le temps de jeu est figé en pause, or le panneau doit rester à jour. */
    private float refreshTimer = REFRESH_SECONDS;
    private boolean collapsed;

    public ResourceOverlay(ResourceTracker tracker, Runnable openDashboard){
        this.tracker = tracker;
        this.openDashboard = openDashboard;
    }

    public void build(){
        ui.hudGroup.fill(root -> {
            root.name = "minclaude-overlay";
            // Sous la minimap, en haut à droite : zone libre du HUD vanilla (le centre accueille les textes d'aide).
            root.top().right().marginTop(MINIMAP_CLEARANCE).marginRight(4f);
            root.visible(() -> state.isGame() && ui.hudfrag.shown && !ui.minimapfrag.shown());
            root.table(Styles.black6, panel -> {
                panel.margin(6f);
                panel.table(header -> {
                    header.add(Core.bundle.get("minclaude.overlay.title")).color(Pal.accent).left().growX();
                    header.button(Icon.chartBarSmall, Styles.clearNonei, openDashboard).size(32f)
                        .tooltip(Core.bundle.get("minclaude.dashboard.open"));
                    header.button(Icon.downOpenSmall, Styles.clearNonei, () -> {
                        collapsed = !collapsed;
                        rebuildRows();
                    }).size(32f)
                        .update(b -> b.getImage().setDrawable(collapsed ? Icon.leftOpenSmall : Icon.downOpenSmall));
                }).growX().row();
                // Replié : rebuildRows() vide la table, le panneau se réduit à l'en-tête.
                panel.add(rows).growX();
            }).width(PANEL_WIDTH);
            root.update(() -> {
                refreshTimer += Core.graphics.getDeltaTime();
                if(refreshTimer >= REFRESH_SECONDS){
                    refreshTimer = 0f;
                    rebuildRows();
                }
            });
        });
    }

    private void rebuildRows(){
        rows.clear();
        if(collapsed || !ModSettings.overlay()) return;

        Seq<Item> items = content.items().select(tracker::isTracked);
        if(items.isEmpty()){
            rows.add(Core.bundle.get("minclaude.overlay.empty")).color(Color.lightGray).wrap().width(PANEL_WIDTH - 20f);
            return;
        }

        var stats = new arc.struct.ObjectMap<Item, ResourceStats>();
        for(Item item : items) stats.put(item, tracker.stats(item));
        // Les plus urgentes en premier : épuisement le plus proche, puis plus forte baisse.
        items.sort((a, b) -> {
            ResourceStats sa = stats.get(a), sb = stats.get(b);
            int c = Float.compare(sa.secondsToEmpty(), sb.secondsToEmpty());
            return c != 0 ? c : Float.compare(sa.slopePerSec(), sb.slopePerSec());
        });

        int n = Math.min(items.size, ModSettings.overlayRows());
        for(int i = 0; i < n; i++){
            Item item = items.get(i);
            ResourceStats s = stats.get(item);
            rows.image(item.uiIcon).size(24f).padRight(4f);
            rows.add(Format.amount(s.stock())).left().width(54f);
            rows.add(Format.ratePerMinute(s.slopePerSec())).color(rateColor(s.slopePerSec())).left().width(96f);
            // Temps avant épuisement, seulement si le stock baisse (la police du jeu n'a pas de symbole sablier).
            rows.add(Float.isInfinite(s.secondsToEmpty()) ? "" : Format.duration(s.secondsToEmpty()))
                .color(Pal.remove).right().growX();
            rows.row();
        }
    }

    static Color rateColor(float slope){
        if(slope > 0.01f) return Pal.heal;
        if(slope < -0.01f) return Pal.remove;
        return Color.lightGray;
    }
}
