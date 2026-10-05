package minclaude.ui.tabs;

import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import minclaude.stats.*;
import minclaude.tracking.BaseScanner;
import minclaude.tracking.ResourceTracker;
import minclaude.ui.LineGraph;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;

/** Onglet Énergie : production et consommation dans le temps, stockage des batteries, couverture de la demande. */
public class PowerTab extends DashboardTab{
    static final Color PRODUCED = Pal.heal, CONSUMED = Pal.remove, STORED = Pal.accent, CAPACITY = Color.gray;

    private final LineGraph flowGraph = new LineGraph().minHeight(200f);
    private final LineGraph storageGraph = new LineGraph().minHeight(140f);
    private final Table details = new Table();

    public PowerTab(ResourceTracker tracker){
        super("power", Icon.power, tracker);
    }

    @Override
    public void build(Table body){
        body.table(t -> {
            rangeBar(t);
            t.table(legend -> {
                legend.add("■ " + text("minclaude.power.produced")).color(PRODUCED).padRight(16f);
                legend.add("■ " + text("minclaude.power.consumed")).color(CONSUMED);
            }).left().row();
            t.add(flowGraph).grow().pad(4f).row();
            t.table(legend -> {
                legend.add("■ " + text("minclaude.power.stored")).color(STORED).padRight(16f);
                legend.add("■ " + text("minclaude.power.capacity")).color(CAPACITY);
            }).left().row();
            t.add(storageGraph).growX().pad(4f).row();
            t.add(details).growX().pad(4f);
        }).grow().top();
    }

    @Override
    public void refresh(){
        MetricHistory h = tracker.history();
        flowGraph.clearSeries();
        storageGraph.clearSeries();
        flowGraph.addSeries(h.view(BaseScanner.POWER_PRODUCED, range), PRODUCED);
        flowGraph.addSeries(h.view(BaseScanner.POWER_CONSUMED, range), CONSUMED);
        storageGraph.addSeries(h.view(BaseScanner.POWER_STORED, range), STORED);
        storageGraph.addSeries(h.view(BaseScanner.POWER_CAPACITY, range), CAPACITY);

        details.clear();
        details.left();
        var p = tracker.base().power();
        if(p.networks() == 0){
            details.add(text("minclaude.power.none")).color(Color.lightGray);
            return;
        }
        stat(details, text("minclaude.power.produced"), Format.amount(p.producedPerSecond()) + "/s", PRODUCED);
        stat(details, text("minclaude.power.consumed"), Format.amount(p.consumedPerSecond()) + "/s", CONSUMED);
        float balance = p.balancePerSecond();
        stat(details, text("minclaude.power.balance"), (balance > 0 ? "+" : "") + Format.amount(balance) + "/s", balance >= 0 ? PRODUCED : CONSUMED);
        details.row();
        stat(details, text("minclaude.power.satisfaction"), Math.round(p.satisfaction() * 100) + "%", p.satisfaction() >= 1f ? PRODUCED : CONSUMED);
        stat(details, text("minclaude.power.stored"), Format.amount(p.stored()) + " / " + Format.amount(p.capacity()), STORED);
        stat(details, text("minclaude.power.networks"), String.valueOf(p.networks()), Color.white);
    }
}
