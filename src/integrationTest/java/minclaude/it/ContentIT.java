package minclaude.it;

import arc.struct.Seq;
import minclaude.content.*;
import mindustry.content.*;
import mindustry.ctype.UnlockableContent;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Le contenu du mod se charge dans le vrai jeu, est branché dans le tech tree et a ses sprites et traductions. */
class ContentIT{
    @BeforeAll
    static void boot(){
        HeadlessGame.start();
    }

    private static Seq<UnlockableContent> all(){
        Seq<UnlockableContent> all = new Seq<>();
        all.addAll(MCItems.all);
        all.addAll(MCBlocks.all);
        return all;
    }

    @Test
    void contentIsRegistered(){
        assertFalse(MCItems.all.isEmpty());
        assertFalse(MCBlocks.all.isEmpty());
        for(UnlockableContent c : all()){
            assertSame(c, mindustry.Vars.content.getByName(c.getContentType(), c.name), c.name);
        }
    }

    @Test
    void contentIsInTechTree(){
        for(UnlockableContent c : all()){
            assertNotNull(c.techNode, "absent du tech tree : " + c.name);
        }
        assertSame(Blocks.siliconSmelter.techNode, MCBlocks.cobaltSmelter.techNode.parent);
        assertSame(MCBlocks.cobaltWall.techNode, MCBlocks.cobaltWallLarge.techNode.parent);
    }

    @Test
    void smelterProducesCobalt(){
        var smelter = (mindustry.world.blocks.production.GenericCrafter)MCBlocks.cobaltSmelter;
        assertSame(MCItems.cobalt, smelter.outputItem.item);
        assertTrue(smelter.hasPower);
    }

    @Test
    void everyContentHasSpriteOfRightSizeAndTranslations() throws IOException{
        Path sprites = HeadlessGame.projectDir().resolve("assets/sprites");
        Properties en = load("bundle.properties"), fr = load("bundle_fr.properties");
        for(UnlockableContent c : all()){
            // Sans mod courant, le nom n'est pas préfixé en test : on reconstruit la clé du jeu réel.
            String type = c.getContentType().name();
            String key = type + ".minclaude-" + c.name;
            assertTrue(en.containsKey(key + ".name"), "traduction EN manquante : " + key);
            assertTrue(fr.containsKey(key + ".name"), "traduction FR manquante : " + key);

            Path sprite;
            try(Stream<Path> s = Files.walk(sprites)){
                sprite = s.filter(p -> p.getFileName().toString().equals(c.name + ".png")).findFirst().orElse(null);
            }
            assertNotNull(sprite, "sprite manquant : " + c.name);
            int expected = c instanceof mindustry.world.Block b ? 32 * b.size : 32;
            assertEquals(expected, javax.imageio.ImageIO.read(sprite.toFile()).getWidth(), "taille du sprite " + c.name);
        }
    }

    private static Properties load(String file) throws IOException{
        Properties p = new Properties();
        try(Reader r = Files.newBufferedReader(HeadlessGame.projectDir().resolve("assets/bundles").resolve(file), StandardCharsets.UTF_8)){
            p.load(r);
        }
        return p;
    }
}
