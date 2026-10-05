package minclaude.it;

import arc.struct.Seq;
import arc.util.Time;
import minclaude.ai.SmartAI;
import minclaude.content.*;
import minclaude.logic.TargetScorer;
import mindustry.content.*;
import mindustry.core.GameState.State;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Unit;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.production.GenericCrafter;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static minclaude.it.HeadlessGame.*;
import static mindustry.Vars.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Équilibrage (A4.1) : compare chaque contenu du mod au vanilla de même rôle, écrit le rapport
 * {@code docs/equilibrage.md} et échoue si une valeur sort des bornes. Les bornes sont larges : elles visent
 * les erreurs (un zéro de trop, une unité T4 plus faible qu'une T2), pas les choix de conception.
 */
class BalanceIT{
    private static final StringBuilder md = new StringBuilder();
    private static final List<String> problems = new ArrayList<>();

    @BeforeAll
    static void boot(){
        HeadlessGame.start();
        md.append("# Équilibrage de MinClaude\n\n")
            .append("_Généré automatiquement par `BalanceIT` (`./gradlew integrationTest`) à partir du vrai jeu : ne pas modifier à la main._\n\n")
            .append("Chaque contenu du mod est comparé au vanilla de même rôle. Le **ratio** compare la valeur du mod à la médiane du vanilla : ")
            .append("1,0 = identique. Les tests échouent hors des bornes indiquées.\n\n");
    }

    @AfterAll
    static void write() throws IOException{
        md.append("\n## Valeurs hors bornes\n\n");
        if(problems.isEmpty()) md.append("Aucune.\n");
        else problems.forEach(p -> md.append("- ").append(p).append('\n'));
        Path out = projectDir().resolve("docs/equilibrage.md");
        Files.writeString(out, md.toString());
        assertTrue(problems.isEmpty(), "valeurs hors bornes (voir docs/equilibrage.md) : " + problems);
    }

    /** Valeur d'un coût : somme des coûts des objets (item.cost) × quantité. */
    static float value(ItemStack[] stacks){
        float v = 0;
        for(ItemStack s : stacks) v += s.item.cost * s.amount;
        return Math.max(v, 1e-3f);
    }

    static float median(Seq<Float> values){
        values.sort();
        return values.isEmpty() ? 0 : values.get(values.size / 2);
    }

    static void check(String what, float ratio, float min, float max){
        if(ratio < min || ratio > max) problems.add(what + String.format(Locale.ROOT, " : ratio %.2f hors de [%.2f ; %.2f]", ratio, min, max));
    }

    static String f(float v){
        return String.format(Locale.ROOT, Math.abs(v) >= 100 ? "%.0f" : "%.2f", v);
    }

    @Test
    void walls(){
        Seq<Float> ref = new Seq<>();
        for(Block b : new Block[]{Blocks.copperWall, Blocks.titaniumWall, Blocks.plastaniumWall, Blocks.thoriumWall, Blocks.phaseWall, Blocks.surgeWall}){
            ref.add(b.health / value(b.requirements));
        }
        float med = median(ref);
        md.append("## Murs : PV par valeur de coût\n\nMédiane vanilla (cuivre → surtension, 1x1) : ").append(f(med)).append("\n\n")
            .append("| Mur | PV | Valeur du coût | PV / valeur | Ratio |\n|---|---|---|---|---|\n");
        for(Block b : MCBlocks.all){
            if(!(b instanceof Wall)) continue;
            float hpv = b.health / value(b.requirements), ratio = hpv / med;
            md.append("| ").append(b.name).append(" | ").append(b.health).append(" | ").append(f(value(b.requirements)))
                .append(" | ").append(f(hpv)).append(" | ").append(f(ratio)).append(" |\n");
            check("mur " + b.name, ratio, 0.4f, 2.5f);
        }
        md.append('\n');
    }

    static float turretDps(Turret t){
        float best = 0;
        if(t instanceof ItemTurret it){
            for(BulletType b : it.ammoTypes.values()) best = Math.max(best, b.estimateDPS() * t.shoot.shots * 60f / t.reload);
        }else if(t instanceof LiquidTurret lt){
            for(BulletType b : lt.ammoTypes.values()) best = Math.max(best, b.estimateDPS() * t.shoot.shots * 60f / t.reload);
        }else if(t instanceof PowerTurret pt){
            best = pt.shootType.estimateDPS() * t.shoot.shots * 60f / t.reload;
        }
        return best;
    }

    @Test
    void turrets(){
        Seq<Float> ref = new Seq<>();
        Block[] vanilla = {Blocks.duo, Blocks.hail, Blocks.scatter, Blocks.lancer, Blocks.arc, Blocks.wave, Blocks.salvo, Blocks.ripple, Blocks.fuse, Blocks.cyclone};
        md.append("## Tourelles : dégâts par seconde (meilleure munition) par valeur de coût\n\n| Tourelle | DPS | Portée | Valeur | DPS / valeur | Ratio |\n|---|---|---|---|---|---|\n");
        for(Block b : vanilla){
            Turret t = (Turret)b;
            float dps = turretDps(t);
            if(dps > 0) ref.add(dps / value(b.requirements));
            md.append("| ").append(b.name).append(" (vanilla) | ").append(f(dps)).append(" | ").append(f(t.range / tilesize)).append(" | ")
                .append(f(value(b.requirements))).append(" | ").append(f(dps / value(b.requirements))).append(" | — |\n");
        }
        float med = median(ref);
        for(Block b : MCBlocks.all){
            if(!(b instanceof Turret t)) continue;
            float dps = turretDps(t), dpv = dps / value(b.requirements), ratio = dpv / med;
            md.append("| **").append(b.name).append("** | ").append(f(dps)).append(" | ").append(f(t.range / tilesize)).append(" | ")
                .append(f(value(b.requirements))).append(" | ").append(f(dpv)).append(" | ").append(f(ratio)).append(" |\n");
            // Givre : tourelle de contrôle (gel), ses dégâts bruts sont faibles par conception.
            if(b == MCBlocks.frost) continue;
            check("tourelle " + b.name, ratio, 0.25f, 4f);
        }
        md.append("\nMédiane vanilla : ").append(f(med)).append(" DPS par unité de valeur. La tourelle Givre (contrôle par le gel) est hors comparaison.\n\n");
    }

    static float crafterRatio(GenericCrafter gc){
        ItemStack[] in = Arrays.stream(gc.nonOptionalConsumers).filter(c -> c instanceof mindustry.world.consumers.ConsumeItems)
            .flatMap(c -> Arrays.stream(((mindustry.world.consumers.ConsumeItems)c).items)).toArray(ItemStack[]::new);
        if(in.length == 0 || gc.outputItems == null) return -1;
        return value(gc.outputItems) / value(in);
    }

    @Test
    void crafters(){
        Seq<Float> ref = new Seq<>();
        md.append("## Usines : valeur produite / valeur consommée (par cycle)\n\n")
            .append("Les liquides et l'énergie ne sont pas valorisés : le vanilla « perd » donc aussi de la valeur. ")
            .append("Le ratio du mod est comparé à la médiane du vanilla.\n\n")
            .append("| Usine | Ratio valeur | Énergie / s | Ratio / vanilla |\n|---|---|---|---|\n");
        for(Block b : new Block[]{Blocks.graphitePress, Blocks.siliconSmelter, Blocks.kiln, Blocks.plastaniumCompressor, Blocks.phaseWeaver,
            Blocks.surgeSmelter, Blocks.pyratiteMixer, Blocks.blastMixer, Blocks.pulverizer}){
            var gc = (GenericCrafter)b;
            float r = crafterRatio(gc);
            if(r <= 0) continue;
            ref.add(r);
            md.append("| ").append(b.name).append(" (vanilla) | ").append(f(r)).append(" | ")
                .append(f(gc.consPower == null ? 0 : gc.consPower.usage * 60f)).append(" | — |\n");
        }
        float med = median(ref);
        for(Block b : MCBlocks.all){
            if(!(b instanceof GenericCrafter gc)) continue;
            float r = crafterRatio(gc);
            if(r <= 0) continue;
            float power = gc.consPower == null ? 0 : gc.consPower.usage * 60f;
            md.append("| **").append(b.name).append("** | ").append(f(r)).append(" | ").append(f(power)).append(" | ").append(f(r / med)).append(" |\n");
            check("usine " + b.name, r / med, 0.5f, 2.5f);
        }
        md.append("\nMédiane vanilla : ").append(f(med)).append(".\n\n");
    }

    /** DPS d'une unité : somme des armes (×2 si en miroir). */
    static float unitDps(UnitType u){
        float d = 0;
        for(var w : u.weapons) d += w.dps() * (w.mirror ? 2 : 1);
        return d;
    }

    static float power(UnitType u){
        // PV effectifs (l'armure retire un forfait à chaque coup ; approximation : +3 % par point) × dégâts.
        return (float)Math.sqrt(u.health * (1 + u.armor * 0.03f) * Math.max(1f, unitDps(u)));
    }

    @Test
    void units(){
        UnitType[][] vanillaTiers = {
            {UnitTypes.dagger, UnitTypes.flare, UnitTypes.risso, UnitTypes.nova},
            {UnitTypes.mace, UnitTypes.horizon, UnitTypes.minke, UnitTypes.pulsar},
            {UnitTypes.fortress, UnitTypes.zenith, UnitTypes.bryde, UnitTypes.quasar},
            {UnitTypes.scepter, UnitTypes.antumbra, UnitTypes.sei},
            {UnitTypes.reign, UnitTypes.eclipse, UnitTypes.omura}
        };
        Map<UnitType, Integer> tier = new LinkedHashMap<>();
        for(UnitType u : new UnitType[]{MCUnits.warden, MCUnits.aid, MCUnits.scout, MCUnits.engineer, MCUnits.skiff, MCUnits.swarmling, MCUnits.marauder, MCUnits.wasp, MCUnits.phantom}) tier.put(u, 1);
        for(UnitType u : new UnitType[]{MCUnits.sentinel, MCUnits.relay, MCUnits.corvette, MCUnits.stalker, MCUnits.sapper, MCUnits.shocker, MCUnits.hornet}) tier.put(u, 2);
        for(UnitType u : new UnitType[]{MCUnits.bastion, MCUnits.beacon, MCUnits.frigate, MCUnits.ravager, MCUnits.siegebreaker, MCUnits.gunship}) tier.put(u, 3);
        for(UnitType u : new UnitType[]{MCUnits.citadel, MCUnits.sanctum, MCUnits.brute, MCUnits.juggernaut, MCUnits.dreadwing}) tier.put(u, 4);
        for(UnitType u : new UnitType[]{MCUnits.colossus, MCUnits.halo, MCUnits.warlord, MCUnits.leviathan}) tier.put(u, 5);
        assertEquals(MCUnits.all.size, tier.size(), "chaque unité du mod a un rang d'équilibrage");

        md.append("## Unités : puissance par rang\n\n")
            .append("Puissance = √(PV effectifs × DPS). Les unités de soutien (soin, construction) ont peu de DPS : leur ratio est indicatif.\n\n")
            .append("| Rang | Médiane vanilla (PV / DPS / puissance) |\n|---|---|\n");
        float[] medPower = new float[6];
        for(int t = 0; t < 5; t++){
            Seq<Float> p = new Seq<>(), hp = new Seq<>(), dps = new Seq<>();
            for(UnitType u : vanillaTiers[t]){
                p.add(power(u));
                hp.add(u.health);
                dps.add(unitDps(u));
            }
            medPower[t + 1] = median(p);
            md.append("| T").append(t + 1).append(" | ").append(f(median(hp))).append(" / ").append(f(median(dps))).append(" / ").append(f(medPower[t + 1])).append(" |\n");
        }
        md.append("\n| Unité | Camp | Rang | PV | Armure | DPS | Puissance | Ratio |\n|---|---|---|---|---|---|---|---|\n");
        Set<UnitType> support = Set.of(MCUnits.aid, MCUnits.relay, MCUnits.beacon, MCUnits.sanctum, MCUnits.halo, MCUnits.engineer);
        for(var e : tier.entrySet()){
            UnitType u = e.getKey();
            int t = e.getValue();
            float ratio = power(u) / medPower[t];
            boolean enemy = MCUnits.enemies.contains(u);
            md.append("| ").append(u.name).append(" | ").append(enemy ? "ennemi" : "allié").append(" | T").append(t).append(" | ").append(f(u.health))
                .append(" | ").append(f(u.armor)).append(" | ").append(f(unitDps(u))).append(" | ").append(f(power(u))).append(" | ").append(f(ratio)).append(" |\n");
            if(!support.contains(u)) check("unité " + u.name + " (T" + t + ")", ratio, 0.35f, 2.8f);
        }
        md.append('\n');

        // Lignées : chaque amélioration est plus résistante que la précédente.
        UnitType[][] lines = {{MCUnits.warden, MCUnits.sentinel, MCUnits.bastion, MCUnits.citadel, MCUnits.colossus},
            {MCUnits.aid, MCUnits.relay, MCUnits.beacon, MCUnits.sanctum, MCUnits.halo}, {MCUnits.skiff, MCUnits.corvette, MCUnits.frigate}};
        for(UnitType[] line : lines){
            for(int i = 1; i < line.length; i++){
                if(line[i].health <= line[i - 1].health) problems.add("lignée : " + line[i].name + " n'a pas plus de PV que " + line[i - 1].name);
            }
        }
    }

    /**
     * Simulation : l'unité, immobilisée, fait face à 3 duos au graphite et 2 grêles. On mesure le temps pour la détruire
     * (survie) face à cette défense de début de partie, comparé à l'unité vanilla de même rang.
     */
    @Test
    void combatSimulation(){
        md.append("## Simulation de combat : survie face à une défense type\n\n")
            .append("3 duos au graphite + 2 lancers (alimentés), unité immobilisée à 5 cases, IA vanilla. Temps de destruction, plafonné à 60 s.\n\n")
            .append("| Unité | Rang | Survie (s) | Référence vanilla (s) | Ratio |\n|---|---|---|---|---|\n");
        SmartAI.setProfile(TargetScorer.Profile.forDifficulty(1, true));
        Object[][] pairs = {
            {MCUnits.swarmling, UnitTypes.dagger, 1}, {MCUnits.marauder, UnitTypes.dagger, 1}, {MCUnits.stalker, UnitTypes.mace, 2},
            {MCUnits.sapper, UnitTypes.mace, 2}, {MCUnits.shocker, UnitTypes.mace, 2}, {MCUnits.ravager, UnitTypes.fortress, 3},
            {MCUnits.siegebreaker, UnitTypes.fortress, 3}, {MCUnits.warden, UnitTypes.dagger, 1}, {MCUnits.sentinel, UnitTypes.mace, 2},
            {MCUnits.bastion, UnitTypes.fortress, 3}
        };
        Map<UnitType, Float> cache = new HashMap<>();
        for(Object[] p : pairs){
            UnitType mod = (UnitType)p[0], ref = (UnitType)p[1];
            float sMod = survive(mod), sRef = cache.computeIfAbsent(ref, BalanceIT::survive);
            float ratio = sMod / Math.max(sRef, 0.1f);
            md.append("| ").append(mod.name).append(" | T").append(p[2]).append(" | ").append(f(sMod)).append(" | ")
                .append(ref.name).append(" : ").append(f(sRef)).append(" | ").append(f(ratio)).append(" |\n");
            check("simulation " + mod.name, ratio, 0.3f, 3.5f);
        }
        SmartAI.setProfile(null);
        md.append('\n');
    }

    static float survive(UnitType type){
        Time.setDeltaProvider(() -> 1f);
        logic.reset();
        world.loadMap(groundZero);
        state.set(State.playing);
        state.rules.waves = false;
        var at = freeArea(8, 12);
        var team = state.rules.defaultTeam;
        for(int i = 0; i < 3; i++){
            var duo = place(Blocks.duo, team, at.x + 2 + i * 2, at.y);
            duo.handleStack(Items.graphite, 30, null);
        }
        for(int i = 0; i < 2; i++){
            place(Blocks.lancer, team, at.x + 2 + i * 3, at.y + 2);
        }
        // Batterie chargée sous les lancers.
        for(int i = 0; i < 6; i++){
            var bat = place(Blocks.battery, team, at.x + 1 + i, at.y + 4);
            if(bat != null && bat.power != null) bat.power.status = 1f;
        }
        Unit u = type.spawn(state.rules.waveTeam, (at.x + 4) * tilesize, (at.y + 8) * tilesize);
        u.apply(StatusEffects.unmoving, 100000f);
        u.apply(StatusEffects.disarmed, 100000f);
        int ticks = 0;
        while(!u.dead() && u.isValid() && ticks < 3600){
            run(1);
            ticks++;
        }
        return ticks / 60f;
    }
}
