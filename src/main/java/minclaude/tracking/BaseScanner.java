package minclaude.tracking;

import arc.struct.Seq;
import minclaude.logic.*;
import minclaude.stats.MetricHistory;
import mindustry.game.SpawnGroup;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.type.ItemStack;
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild;
import mindustry.world.blocks.production.*;
import mindustry.world.consumers.*;

import java.util.ArrayList;
import java.util.List;

import static mindustry.Vars.*;

/**
 * Parcourt les bâtiments et unités d'une équipe une fois par seconde : énergie (enregistrée dans l'historique),
 * état des usines, défense et prochaine vague. Les résultats sont des instantanés immuables pour l'interface.
 */
public final class BaseScanner{
    public static final String POWER_PRODUCED = "power/produced";
    public static final String POWER_CONSUMED = "power/consumed";
    public static final String POWER_STORED = "power/stored";
    public static final String POWER_CAPACITY = "power/capacity";
    public static final String POWER_SATISFACTION = "power/satisfaction";

    /** Dernier instantané, lu par l'interface. */
    public record Snapshot(PowerAggregator power, IndustryReport industry, DefenseReport defense, ProductionModel production){
        public static final Snapshot EMPTY = new Snapshot(new PowerAggregator(), IndustryReport.EMPTY, DefenseReport.EMPTY, ProductionModel.EMPTY);
    }

    private volatile Snapshot last = Snapshot.EMPTY;

    public Snapshot last(){
        return last;
    }

    public void reset(){
        last = Snapshot.EMPTY;
    }

    public Snapshot scan(Team team, MetricHistory history){
        PowerAggregator power = new PowerAggregator();
        IndustryReport industry = new IndustryReport();
        DefenseReport defense = new DefenseReport();
        ProductionModel production = new ProductionModel();

        Seq<Building> buildings = team.data().buildings;
        for(int i = 0; i < buildings.size; i++){
            Building b = buildings.get(i);
            if(b.power != null && b.power.graph != null){
                var g = b.power.graph;
                power.add(g.getID(), g.getLastPowerProduced(), g.getLastPowerNeeded(), g.getLastPowerStored(), g.getLastCapacity());
            }
            if(isIndustry(b)){
                addIndustry(b, industry);
                addProduction(b, production);
            }
            if(b instanceof TurretBuild t){
                var turret = (mindustry.world.blocks.defense.turrets.Turret)b.block;
                defense.addTurret(t.hasAmmo(), b.healthf(), turret.targetAir, turret.targetGround);
            }
            if(b.block instanceof mindustry.world.blocks.defense.Wall) defense.addWall();
        }

        team.data().units.each(u -> defense.addUnit(u.type.name));
        scanWave(defense);

        if(power.networks() > 0){
            history.record(POWER_PRODUCED, power.producedPerSecond());
            history.record(POWER_CONSUMED, power.consumedPerSecond());
            history.record(POWER_STORED, power.stored());
            history.record(POWER_CAPACITY, power.capacity());
            history.record(POWER_SATISFACTION, power.satisfaction() * 100f);
        }

        Snapshot s = new Snapshot(power, industry, defense, production);
        last = s;
        return s;
    }

    static boolean isIndustry(Building b){
        return b instanceof GenericCrafter.GenericCrafterBuild || b instanceof Separator.SeparatorBuild || b instanceof Drill.DrillBuild;
    }

    private static void addIndustry(Building b, IndustryReport report){
        List<String> missing = missingItems(b);
        boolean usesPower = b.block.consPower != null;
        float satisfaction = b.power == null ? 1f : b.power.status;
        IndustryStatus status = IndustryStatus.classify(b.enabled, b.shouldConsume(), b.efficiency, usesPower, satisfaction, !missing.isEmpty());
        // Une foreuse posée hors d'un gisement n'a « rien à produire » : c'est un manque d'entrée, pas une sortie pleine.
        if(b instanceof Drill.DrillBuild d && d.dominantItems == 0) status = IndustryStatus.NO_INPUT;
        report.add(b.block.name, status, b.efficiency, missing);
    }

    /** Production et consommation théoriques d'une usine (par seconde), pondérées par son rendement actuel. */
    static void addProduction(Building b, ProductionModel model){
        float eff = b.efficiency;
        if(b.block instanceof GenericCrafter gc && gc.craftTime > 0){
            float perSecond = 60f / gc.craftTime;
            if(gc.outputItems != null) for(ItemStack s : gc.outputItems) model.produces(s.item.name, s.amount * perSecond, eff);
            consumption(b, perSecond, model);
        }else if(b.block instanceof Separator sep && sep.craftTime > 0){
            float perSecond = 60f / sep.craftTime;
            int total = 0;
            for(ItemStack s : sep.results) total += s.amount;
            for(ItemStack s : sep.results) model.produces(s.item.name, perSecond * s.amount / Math.max(1, total), eff);
            consumption(b, perSecond, model);
        }else if(b instanceof Drill.DrillBuild d && d.dominantItem != null && d.dominantItems > 0){
            Drill drill = (Drill)b.block;
            model.produces(d.dominantItem.name, 60f / drill.getDrillTime(d.dominantItem) * d.dominantItems, eff);
        }
    }

    private static void consumption(Building b, float craftsPerSecond, ProductionModel model){
        for(Consume c : b.block.nonOptionalConsumers){
            if(c instanceof ConsumeItems ci){
                for(ItemStack s : ci.items) model.consumes(s.item.name, s.amount * craftsPerSecond, b.efficiency);
            }
        }
    }

    /** Ressources exigées par le bloc et absentes de son stock interne. */
    static List<String> missingItems(Building b){
        List<String> missing = new ArrayList<>(2);
        if(b.items == null) return missing;
        for(Consume c : b.block.nonOptionalConsumers){
            if(c instanceof ConsumeItems ci){
                for(ItemStack stack : ci.items){
                    if(!b.items.has(stack.item, stack.amount)) missing.add(stack.item.name);
                }
            }
        }
        return missing;
    }

    private static void scanWave(DefenseReport defense){
        if(!state.rules.waves){
            defense.setWave(state.wave, Float.POSITIVE_INFINITY, state.enemies);
            return;
        }
        defense.setWave(state.wave, state.rules.waveTimer ? state.wavetime / 60f : Float.POSITIVE_INFINITY, state.enemies);
        int ground = Math.max(1, spawner.countGroundSpawns()), flyers = Math.max(1, spawner.countFlyerSpawns());
        for(SpawnGroup group : state.rules.spawns){
            int spawned = group.getSpawned(state.wave - 1);
            int points = group.spawn != -1 ? 1 : group.type.flying ? flyers : ground;
            defense.addNextWaveUnits(group.type.name, spawned * points);
        }
    }
}
