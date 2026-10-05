package minclaude.ui.tabs;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import minclaude.logic.DefenseReport;
import minclaude.stats.Format;
import minclaude.tracking.ResourceTracker;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.ui.Styles;

import java.util.List;
import java.util.Map;

import static mindustry.Vars.content;

/** Onglet Défense : prochaine vague et sa composition, état des tourelles, unités alliées. */
public class DefenseTab extends DashboardTab{
    private final Table wave = new Table();
    private final Table turrets = new Table();
    private final Table units = new Table();

    public DefenseTab(ResourceTracker tracker){
        super("defense", Icon.defense, tracker);
    }

    @Override
    public void build(Table body){
        body.table(left -> {
            left.top().left();
            left.add(text("minclaude.defense.wave")).color(Pal.accent).left().row();
            left.add(wave).growX().left().padBottom(16f).row();
            left.add(text("minclaude.defense.turrets")).color(Pal.accent).left().row();
            left.add(turrets).growX().left();
        }).width(520f).growY().top();
        body.table(right -> {
            right.top().left();
            right.add(text("minclaude.defense.units")).color(Pal.accent).left().row();
            right.pane(Styles.smallPane, units).grow().top();
        }).grow().top().padLeft(12f);
    }

    @Override
    public void refresh(){
        DefenseReport d = tracker.base().defense();

        wave.clear();
        wave.left();
        stat(wave, text("minclaude.defense.current"), String.valueOf(d.wave()), Color.white);
        stat(wave, text("minclaude.defense.next"), Format.duration(d.secondsToNextWave()), Pal.accent);
        stat(wave, text("minclaude.defense.enemies"), String.valueOf(d.enemiesAlive()), d.enemiesAlive() > 0 ? Pal.remove : Color.lightGray);
        wave.row();
        wave.table(c -> unitIcons(c, d.nextWave(), Pal.remove, text("minclaude.defense.nowave"))).colspan(3).left();

        turrets.clear();
        turrets.left();
        stat(turrets, text("minclaude.defense.total"), String.valueOf(d.turrets()), Color.white);
        stat(turrets, text("minclaude.defense.noammo"), String.valueOf(d.turretsNoAmmo()), d.turretsNoAmmo() > 0 ? Pal.remove : Color.lightGray);
        stat(turrets, text("minclaude.defense.damaged"), String.valueOf(d.turretsDamaged()), d.turretsDamaged() > 0 ? Color.orange : Color.lightGray);

        units.clear();
        units.top().left();
        units.add(Core.bundle.format("minclaude.defense.unittotal", d.unitTotal())).color(Color.lightGray).left().row();
        units.table(c -> unitIcons(c, d.units(), Pal.heal, text("minclaude.defense.nounits"))).left();
    }

    private static void unitIcons(Table t, List<Map.Entry<String, Integer>> list, Color color, String empty){
        t.left();
        if(list.isEmpty()){
            t.add(empty).color(Color.lightGray);
            return;
        }
        int col = 0;
        for(var e : list){
            UnitType type = content.unit(e.getKey());
            t.table(c -> {
                if(type != null) c.image(type.uiIcon).size(32f).padRight(4f);
                c.add("×" + e.getValue()).color(color);
            }).left().padRight(14f).padBottom(4f)
                .tooltip(type == null ? e.getKey() : type.localizedName);
            if(++col % 5 == 0) t.row();
        }
    }
}
