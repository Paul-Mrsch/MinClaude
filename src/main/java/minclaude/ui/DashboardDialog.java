package minclaude.ui;

import arc.Core;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import minclaude.stats.TimeRange;
import minclaude.tracking.ResourceTracker;
import minclaude.ui.tabs.*;
import mindustry.type.Item;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

/** Dashboard de gestion : onglets Ressources, Énergie, Industries, Défense, rafraîchis chaque seconde. */
public class DashboardDialog extends BaseDialog{
    private final ResourcesTab resources;
    private final Seq<DashboardTab> tabs = new Seq<>();
    private final Table body = new Table();
    private DashboardTab current;
    private float refreshTimer;

    public DashboardDialog(ResourceTracker tracker){
        super(Core.bundle.get("minclaude.dashboard.title"));
        resources = new ResourcesTab(tracker);
        tabs.addAll(resources, new PowerTab(tracker), new IndustryTab(tracker), new DefenseTab(tracker));
        current = resources;
        addCloseButton();
        shown(this::rebuild);
        // Temps réel et non temps de jeu : le dashboard reste à jour quand la partie est en pause.
        update(() -> {
            refreshTimer += Core.graphics.getDeltaTime();
            if(isShown() && refreshTimer >= 1f){
                refreshTimer = 0f;
                current.refresh();
            }
        });
    }

    private void rebuild(){
        cont.clear();
        cont.table(header -> {
            var group = new ButtonGroup<>();
            header.defaults().height(44f).minWidth(150f).pad(2f);
            for(DashboardTab tab : tabs){
                header.button(tab.title(), tab.icon, Styles.togglet, () -> showTab(tab.key))
                    .group(group).checked(b -> current == tab);
            }
        }).left().row();
        cont.add(body).grow();
        buildCurrent();
    }

    private void buildCurrent(){
        body.clear();
        current.build(body);
        current.refresh();
    }

    public void showTab(String key){
        DashboardTab tab = tabs.find(t -> t.key.equals(key));
        if(tab == null) return;
        current = tab;
        buildCurrent();
    }

    public String currentTab(){
        return current.key;
    }

    /** Pilotage programmatique (autotest en jeu). */
    public void setRange(TimeRange r){
        current.setRange(r);
    }

    public void select(Item item){
        showTab("resources");
        resources.select(item);
    }
}
