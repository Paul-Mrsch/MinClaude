package minclaude.assets;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Vérifie les fichiers du mod sans démarrer le jeu (le répertoire de travail est la racine du projet). */
class AssetsTest{
    private static final Path BUNDLES = Path.of("assets/bundles");
    private static final Path SPRITES = Path.of("assets/sprites");

    private static Properties bundle(String file) throws IOException{
        Properties p = new Properties();
        try(Reader r = Files.newBufferedReader(BUNDLES.resolve(file), StandardCharsets.UTF_8)){
            p.load(r);
        }
        return p;
    }

    @Test
    void frenchAndEnglishHaveSameKeys() throws IOException{
        Set<Object> en = bundle("bundle.properties").keySet(), fr = bundle("bundle_fr.properties").keySet();
        Set<Object> missingFr = new TreeSet<>(en), missingEn = new TreeSet<>(fr);
        missingFr.removeAll(fr);
        missingEn.removeAll(en);
        assertTrue(missingFr.isEmpty(), "absent de bundle_fr : " + missingFr);
        assertTrue(missingEn.isEmpty(), "absent de bundle : " + missingEn);
    }

    @Test
    void noEmptyTranslations() throws IOException{
        for(String file : List.of("bundle.properties", "bundle_fr.properties")){
            Properties p = bundle(file);
            for(String key : p.stringPropertyNames()){
                assertFalse(p.getProperty(key).isBlank(), file + " : " + key + " est vide");
            }
        }
    }

    @Test
    void contentHasNameDescriptionAndSprite() throws IOException{
        Properties en = bundle("bundle.properties");
        Pattern p = Pattern.compile("^(item|block|liquid|unit)\\.minclaude-(.+)\\.name$");
        Set<String> sprites = spriteNames();
        int count = 0;
        for(String key : en.stringPropertyNames()){
            Matcher m = p.matcher(key);
            if(!m.matches()) continue;
            count++;
            String name = m.group(2);
            assertTrue(en.containsKey(m.group(1) + ".minclaude-" + name + ".description"), "description manquante : " + key);
            assertTrue(sprites.contains(name), "sprite manquant pour " + name);
        }
        assertTrue(count > 0, "aucun contenu trouvé dans les bundles");
    }

    @Test
    void spritesAreMultiplesOfATile() throws IOException{
        try(Stream<Path> files = Files.walk(SPRITES)){
            for(Path f : files.filter(f -> f.toString().endsWith(".png")).toList()){
                BufferedImage img = ImageIO.read(f.toFile());
                assertNotNull(img, "PNG illisible : " + f);
                assertEquals(img.getWidth(), img.getHeight(), "sprite non carré : " + f);
                assertEquals(0, img.getWidth() % 32, "taille non multiple de 32 : " + f);
            }
        }
    }

    @Test
    void manifestIsValid() throws IOException{
        String hjson = Files.readString(Path.of("mod.hjson"));
        assertTrue(hjson.contains("name: \"minclaude\""), "le nom interne sert de préfixe au contenu, il ne doit pas changer");
        assertTrue(hjson.contains("java: true"));
        Matcher main = Pattern.compile("main: \"([\\w.]+)\"").matcher(hjson);
        assertTrue(main.find());
        Path mainSource = Path.of("src/main/java", main.group(1).replace('.', '/') + ".java");
        assertTrue(Files.exists(mainSource), "classe principale introuvable : " + mainSource);
        Matcher min = Pattern.compile("minGameVersion: ([\\d.]+)").matcher(hjson);
        assertTrue(min.find());
        // Les mods Java doivent viser au moins la build 154 (Vars.minJavaModGameVersion).
        assertTrue(Double.parseDouble(min.group(1)) >= 154);
        assertTrue(Files.exists(Path.of("icon.png")), "icône du mod manquante");
    }

    private static Set<String> spriteNames() throws IOException{
        Set<String> names = new HashSet<>();
        try(Stream<Path> files = Files.walk(SPRITES)){
            files.map(f -> f.getFileName().toString()).filter(n -> n.endsWith(".png"))
                .forEach(n -> names.add(n.substring(0, n.length() - 4)));
        }
        return names;
    }
}
