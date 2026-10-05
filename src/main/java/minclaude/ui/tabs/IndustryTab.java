package minclaude.ui.tabs;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import minclaude.logic.IndustryReport;
import minclaude.logic.IndustryStatus;
import minclaude.stats.Format;
import minclaude.tracking.ResourceTracker;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.ui.Styles;
import mindustry.world.Block;

import static mindustry.Vars.content;

/** Onglet Industries : état de chaque type d'usine et goulots d'étranglement (ressources qui bloquent le plus d'usines). */
public class IndustryTab extends DashboardTab{
    private static final IndustryStatus[] COLUMNS = {IndustryStatus.ACTIVE, IndustryStatus.NO_INPUT, IndustryStatus.NO_POWER, IndustryStatus.NO_OUTPUT};

    private final Table summary = new Table();
    private final Table blocks = new Table();
    private final Table bottlenecks = new Table();

    public IndustryTab(ResourceTracker tracker){
        super("industry", Icon.production, tracker);
    }

    public static Color color(IndustryStatus s){
        return switch(s){
            case ACTIVE -> Pal.heal;
            case NO_INPUT -> Pal.remove;
            case NO_POWER -> Pal.accent;
            case NO_OUTPUT -> Color.orange;
            case DISABLED -> Color.gray;
        };
    }

    @Override
    public void build(Table body){
        body.table(t -> {
            t.top().left();
            t.add(summary).left().row();
            t.pane(Styles.smallPane, blocks).grow().top().row();
        }).grow().top();
        body.table(t -> {
            t.top().left();
            t.add(text("minclaude.industry.bottlenecks")).color(Pal.accent).left().row();
            t.pane(Styles.smallPane, bottlenecks).width(360f).growY().top();
        }).width(380f).growY().top().padLeft(12f);
    }

    @Override
    public void refresh(){
        IndustryReport r = tracker.base().industry();

        summary.clear();
        summary.add(Core.bundle.format("minclaude.industry.summary", r.total(), r.blocked())).color(r.blocked() > 0 ? Pal.remove : Color.lightGray);

        blocks.clear();
        blocks.top().left();
        blocks.defaults().pad(3f).left();
        if(r.total() == 0){
            blocks.add(text("minclaude.industry.none")).color(Color.lightGray);
        }else{
            blocks.add("");
            blocks.add(text("minclaude.industry.block")).color(Color.lightGray);
            blocks.add("#").color(Color.lightGray);
            for(IndustryStatus s : COLUMNS) blocks.add(text("minclaude.industry.status." + s.name().toLowerCase())).color(color(s)).padLeft(10f);
            blocks.add(text("minclaude.industry.efficiency")).color(Color.lightGray).padLeft(10f);
            blocks.row();
            for(IndustryReport.BlockSummary b : r.blocks()){
                Block block = content.block(b.block);
                blocks.image(block == null ? Icon.box.getRegion() : block.uiIcon).size(28f);
                blocks.add(block == null ? b.block : block.localizedName).width(200f).ellipsis(true);
                blocks.add(String.valueOf(b.total()));
                for(IndustryStatus s : COLUMNS){
                    int n = b.count(s);
                    blocks.add(n == 0 ? "·" : String.valueOf(n)).color(n == 0 ? Color.darkGray : color(s)).padLeft(10f);
                }
                blocks.add(Math.round(b.averageEfficiency() * 100) + "%").padLeft(10f);
                blocks.row();
            }
        }

        bottlenecks.clear();
        bottlenecks.top().left();
        var list = r.bottlenecks();
        if(list.isEmpty()){
            bottlenecks.add(text("minclaude.industry.nobottleneck")).color(Color.lightGray).wrap().width(340f);
            return;
        }
        for(IndustryReport.Bottleneck b : list){
            Item item = content.item(b.item());
            bottlenecks.table(row -> {
                row.left();
                if(item != null) row.image(item.uiIcon).size(28f).padRight(6f);
                row.table(c -> {
                    c.left();
                    c.add(item == null ? b.item() : item.localizedName).left().row();
                    String trend = item == null ? "" : " · " + Format.amount(tracker.stats(item).stock()) + " " + text("minclaude.industry.instock");
                    c.add(Core.bundle.format("minclaude.industry.starvedby", b.starvedBuildings()) + trend).color(Pal.remove).left().wrap().width(290f);
                });
            }).growX().left().padBottom(6f).row();
        }
    }
}
