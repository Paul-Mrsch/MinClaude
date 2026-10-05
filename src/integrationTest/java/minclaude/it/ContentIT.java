package minclaude.it;

import arc.struct.Seq;
import minclaude.ai.SmartGroundAI;
import minclaude.content.*;
import mindustry.content.*;
import mindustry.ctype.UnlockableContent;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.world.Block;
import mindustry.world.blocks.distribution.Conveyor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.production.Drill;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.units.UnitFactory;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import static mindustry.Vars.content;
import static org.junit.jupiter.api.Assertions.*;

/** Le contenu du mod se charge dans le vrai jeu, est branché dans le tech tree et a ses sprites et traductions. */
class ContentIT{
    private static Map<String, Path> sprites;

    @BeforeAll
    static void boot() throws IOException{
        HeadlessGame.start();
        sprites = new HashMap<>();
        try(Stream<Path> s = Files.walk(HeadlessGame.projectDir().resolve("assets/sprites"))){
            s.filter(p -> p.toString().endsWith(".png")).forEach(p -> sprites.put(p.getFileName().toString().replace(".png", ""), p));
        }
    }

    private static Seq<UnlockableContent> all(){
        Seq<UnlockableContent> all = new Seq<>();
        all.addAll(MCItems.all);
        all.addAll(MCBlocks.all);
        all.addAll(MCUnits.all);
        return all;
    }

    @Test
    void contentIsRegistered(){
        assertEquals(6, MCItems.all.size);
        assertEquals(4, MCBlocks.ores.size);
        assertEquals(2, MCUnits.allies.size);
        assertEquals(2, MCUnits.enemies.size);
        for(UnlockableContent c : all()){
            assertSame(c, content.getByName(c.getContentType(), c.name), c.name);
        }
    }

    @Test
    void techTreeHasPlayableContentOnly(){
        for(UnlockableContent c : all()){
            boolean environment = c instanceof OreBlock;
            boolean enemy = c instanceof UnitType u && MCUnits.enemies.contains(u);
            if(environment || enemy) assertNull(c.techNode, "ne doit pas être recherchable : " + c.name);
            else assertNotNull(c.techNode, "absent du tech tree : " + c.name);
        }
        assertSame(Blocks.siliconSmelter.techNode, MCBlocks.cobaltSmelter.techNode.parent);
        assertSame(MCBlocks.cobaltWall.techNode, MCBlocks.cobaltWallLarge.techNode.parent);
        assertSame(UnitTypes.dagger.techNode, MCUnits.warden.techNode.parent);
    }

    @Test
    void recipes(){
        assertSame(MCItems.cobalt, ((GenericCrafter)MCBlocks.cobaltSmelter).outputItem.item);
        assertSame(MCItems.aluminum, ((GenericCrafter)MCBlocks.aluminumSmelter).outputItem.item);
        assertSame(MCItems.brass, ((GenericCrafter)MCBlocks.brassFoundry).outputItem.item);
        assertTrue(((Drill)MCBlocks.percussionDrill).tier >= MCItems.cobalt.hardness, "la foreuse à percussion extrait le cobalt");
        for(OreBlock ore : MCBlocks.ores){
            assertNotNull(ore.itemDrop);
            assertTrue(ore.oreDefault, "généré par défaut dans les cartes personnalisées");
        }
    }

    @Test
    void alliesAreBuiltInVanillaFactories(){
        assertTrue(((UnitFactory)Blocks.groundFactory).plans.contains(p -> p.unit == MCUnits.warden));
        assertTrue(((UnitFactory)Blocks.airFactory).plans.contains(p -> p.unit == MCUnits.aid));
        for(UnitType enemy : MCUnits.enemies){
            for(Block b : content.blocks()){
                if(b instanceof UnitFactory f) assertFalse(f.plans.contains(p -> p.unit == enemy), enemy.name + " constructible dans " + b.name);
            }
        }
    }

    @Test
    void smartAiIsInstalledOnGroundUnitsOnly(){
        assertInstanceOf(SmartGroundAI.class, UnitTypes.dagger.aiController.get());
        assertInstanceOf(SmartGroundAI.class, MCUnits.marauder.aiController.get());
        assertFalse(UnitTypes.flare.aiController.get() instanceof SmartGroundAI);
        assertFalse(UnitTypes.mono.aiController.get() instanceof SmartGroundAI, "les mineurs gardent leur IA");
    }

    @Test
    void everyContentHasItsSpritesAndTranslations() throws IOException{
        Properties en = load("bundle.properties"), fr = load("bundle_fr.properties");
        for(UnlockableContent c : all()){
            // Sans mod courant, le nom n'est pas préfixé en test : on reconstruit la clé du jeu réel.
            String key = c.getContentType().name() + ".minclaude-" + c.name;
            assertTrue(en.containsKey(key + ".name"), "traduction EN manquante : " + key);
            assertTrue(fr.containsKey(key + ".name"), "traduction FR manquante : " + key);
            assertTrue(en.containsKey(key + ".description") && fr.containsKey(key + ".description"), "description manquante : " + key);

            for(String region : requiredRegions(c)){
                assertTrue(sprites.containsKey(region), "sprite manquant : " + region + ".png pour " + c.name);
            }
            if(c instanceof Block b && !(c instanceof OreBlock) && !(c instanceof Conveyor)){
                assertEquals(32 * b.size, javax.imageio.ImageIO.read(sprites.get(c.name).toFile()).getWidth(), "taille du sprite " + c.name);
            }
        }
    }

    /** Régions que le jeu cherche pour ce contenu (sans le préfixe du mod, ajouté au chargement). */
    static List<String> requiredRegions(UnlockableContent c){
        List<String> r = new ArrayList<>();
        if(c instanceof OreBlock){
            for(int i = 1; i <= 3; i++) r.add(c.name + i);
        }else if(c instanceof Conveyor){
            for(int shape = 0; shape < 7; shape++) for(int frame = 0; frame < 4; frame++) r.add(c.name + "-" + shape + "-" + frame);
        }else if(c instanceof Drill){
            r.add(c.name);
            r.add(c.name + "-rotator");
            r.add(c.name + "-top");
        }else if(c instanceof UnitType u){
            r.add(c.name);
            if(!u.flying){
                r.add(c.name + "-leg");
                r.add(c.name + "-base");
            }
            for(Weapon w : u.weapons){
                if(!w.name.isEmpty()) r.add(w.name.replaceFirst("^minclaude-", ""));
            }
        }else{
            r.add(c.name);
        }
        return r;
    }

    private static Properties load(String file) throws IOException{
        Properties p = new Properties();
        try(Reader r = Files.newBufferedReader(HeadlessGame.projectDir().resolve("assets/bundles").resolve(file), StandardCharsets.UTF_8)){
            p.load(r);
        }
        return p;
    }
}
