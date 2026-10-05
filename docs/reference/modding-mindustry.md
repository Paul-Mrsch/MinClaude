# Référence : modding Java pour Mindustry (v160.5, octobre 2026)

> Vérifié le 2026-10-05 contre les sources suivantes : tag `v160.5` d'Anuken/Mindustry, Arc au commit `8eb00ffff0`, et MindustryJavaModTemplate au commit `32898e9` (2026-10-03).
> Tous les extraits des sections 3 et 4 **compilent** contre `Anuken:Mindustry:v160` (dependencies.jar) avec `./gradlew jar`. Le test headless de la section 4 **passe** (JDK 24, Gradle 9.8.0).
> Les points marqués ⚠ n'ont pas été vérifiés en jeu, ou restent incertains.

## 1. Versions

| Élément | Valeur |
|---|---|
| Dernière release stable | **v160.5** (titre GitHub : « v8 Build 160.5 »), publiée le **2026-09-20** (13:56 UTC), `prerelease=false` |
| Série v160 | v160 (2026-09-11), puis .1 (09-11), .2 (09-12), .3 (09-13), .4 (09-14), .5 (09-20). Release précédente : v159.7 (2026-07-19) |
| `minGameVersion` | Le template utilise **`160.3`**. Pour cibler la v160.5 : `160` (accepte toute la série 160.x) ou `160.5`. Ce champ est une **String** comparée par `Version.isAtLeast` sous la forme « build.revision » |
| Plancher pour les mods Java | `Vars.minJavaModGameVersion = 154`. Un mod Java dont la valeur majeure de `minGameVersion` est inférieure à 154 est marqué « legacy » et refusé (sauf `legacyCompatible: true`). Pour les mods non-Java, le plancher est `minModGameVersion = 136` |
| Arc | `archash=8eb00ffff0` (gradle.properties de Mindustry, commit Arc du 2026-09-18). Arc est **inclus** dans `dependencies.jar` : il n'y a pas besoin de le déclarer à part. Coordonnée jitpack équivalente : `com.github.Anuken.Arc:arc-core:8eb00ffff0` |
| Java | **JDK 17** (`sourceCompatibility`/`targetCompatibility = 17`, `--release 17` dans Mindustry ; JDK 17 dans la CI du template). Un JDK plus récent (24 testé) fonctionne pour compiler, car la cible reste 17 |
| Gradle wrapper (template) | **9.8.0** (`gradle-9.8.0-bin.zip`). Mindustry lui-même utilise 9.3.1 |
| Assets de la release | `Mindustry.jar`, `server-release.jar`, `dependencies.jar` (environ 15 Mo : classes Mindustry core + server + Arc, y compris le backend headless), `assets.jar` (environ 35 Mo : bundles, cartes, sprites) |

## 2. MindustryJavaModTemplate (état actuel)

**Changement important :** le template est passé au **Kotlin DSL**. Il contient un seul fichier `build.gradle.kts`, et **ni `settings.gradle` ni `gradle.properties`**. Gradle 9.8 accepte une build sans fichier settings (vérifié). Le nom du projet est alors le **nom du dossier**, et le jar s'appelle `<nomDossier>Desktop.jar`. Pour fixer ce nom, on peut ajouter un `settings.gradle.kts` contenant `rootProject.name = "MonMod"`.

### Arborescence
```
.github/workflows/commitTest.yml   # CI : ./gradlew deploy, JDK 17, upload de l'artifact
.github/workflows/release.yml      # attache le jar à la release GitHub
assets/sprites/frog.png            # tout le contenu de assets/ est copié à la racine du jar
gradle/wrapper/{gradle-wrapper.jar,gradle-wrapper.properties}
src/example/ExampleJavaMod.java    # sources Java (sourceSet main = "src", sans src/main/java)
build.gradle.kts
mod.hjson
gradlew, gradlew.bat, README.md, .gitignore
```
Arborescence typique d'`assets/` : `sprites/` (PNG, sous-dossiers libres : `items/`, `blocks/`, `units/`…), `sprites-override/` (remplacement de sprites vanilla, sans préfixe), `bundles/bundle.properties` et `bundle_fr.properties`, `sounds/`, `music/`, `maps/`, `shaders/`. Un `icon.png` placé à la racine du projet est inclus dans le jar.

### build.gradle.kts (verbatim)
```kotlin
plugins{
    java
}

sourceSets.main{
    java.setSrcDirs(listOf("src"))
}

// Mindustry version to depend on.
// Valid values:
// - latest: depend on the latest release of mindustry
// - be: depend on the very latest commit of mindustry
// - v<number>: depend on a specific version
val mindustryVersion = "v160"
val isWindows = System.getProperty("os.name").lowercase().contains("windows")
val sdkRoot: String? = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
val projectName = project.name

repositories{
    mavenCentral()

    //Downloads the dependencies JAR file from Mindustry releases; does not use any real repository. Surprisingly, this is the most reliable option.
    ivy{
        url = uri("https://github.com/")
        patternLayout{
            val pattern = when(mindustryVersion){
                "latest" -> "/[organisation]/[module]/releases/[revision]/download/dependencies.jar" //latest stable release
                "be" -> "/[organisation]/[module]/releases/download/master/[revision].jar" //latest commit (BE)
                else -> "/[organisation]/[module]/releases/download/[revision]/dependencies.jar" //specific release
            }
            val path = pattern.substring(0, pattern.lastIndexOf('/'))

            artifact("$path/[classifier].jar")
            artifact(pattern.replaceFirst(Regex("""\.jar$"""), "(-[classifier]).jar"))
        }
        metadataSources{ artifact() }

        content{
            if(mindustryVersion == "be"){
                //BE artifact version is always 'latest'
                includeVersion("Anuken", "MindustryBuilds", "latest")
            }else{
                includeVersion("Anuken", "Mindustry", mindustryVersion)
            }
        }
    }
}

java{
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies{
    compileOnly(if(mindustryVersion == "be") "Anuken:MindustryBuilds:latest" else "Anuken:Mindustry:$mindustryVersion")
}

tasks.jar{
    archiveFileName.set("${projectName}Desktop.jar")

    from(configurations.runtimeClasspath.map{ files ->
        files.map{ if(it.isDirectory) it else zipTree(it) }
    })

    from(projectDir){
        include("mod.hjson")
        include("icon.png")
    }

    from("assets/"){
        include("**")
    }
}

val jarAndroid = tasks.register("jarAndroid"){
    dependsOn(tasks.jar)

    doLast{
        val sdk = sdkRoot?.let{ File(it) }
        if(sdk == null || !sdk.exists()){
            throw GradleException("No valid Android SDK found. Ensure that ANDROID_HOME is set to your Android SDK directory.")
        }

        val platformRoot = File(sdk, "platforms").listFiles()
            ?.sortedDescending()
            ?.firstOrNull{ File(it, "android.jar").exists() }
            ?: throw GradleException("No android.jar found. Ensure that you have an Android platform installed.")

        //collect dependencies needed for desugaring
        val classpath = (configurations.compileClasspath.get().files +
                configurations.runtimeClasspath.get().files +
                File(platformRoot, "android.jar"))

        val d8 = if(isWindows) "d8.bat" else "d8"

        val command = buildList{
            add(d8)
            classpath.forEach{ addAll(listOf("--classpath", it.path)) }
            addAll(listOf("--min-api", "21"))
            addAll(listOf("--output", "${projectName}Android.jar"))
            add("${projectName}Desktop.jar")
        }

        //dex and desugar files - this requires d8 in your PATH
        val exitCode = ProcessBuilder(command)
            .directory(layout.buildDirectory.dir("libs").get().asFile)
            .inheritIO()
            .start()
            .waitFor()

        if(exitCode != 0) throw GradleException("d8 failed with exit code $exitCode")
    }
}

tasks.register<Jar>("deploy"){
    dependsOn(jarAndroid, tasks.jar)
    archiveFileName.set("$projectName.jar")

    val libs = layout.buildDirectory.dir("libs")
    from(provider{
        listOf(
            zipTree(libs.get().file("${projectName}Desktop.jar")),
            zipTree(libs.get().file("${projectName}Android.jar"))
        )
    })

    doLast{
        libs.get().file("${projectName}Android.jar").asFile.delete()
    }
}
```
Points clés :
- **Dépôt** : un dépôt `ivy` pointant vers `https://github.com/`, plus `mavenCentral()`.
- **Dépendance** : `compileOnly("Anuken:Mindustry:v160")`. L'URL résolue est `https://github.com/Anuken/Mindustry/releases/download/v160/dependencies.jar`. Pour `be`, la dépendance est `Anuken:MindustryBuilds:latest`, résolue vers `https://github.com/Anuken/MindustryBuilds/releases/download/master/latest.jar`.
- **Classifier** : grâce au motif `[classifier].jar`, `Anuken:Mindustry:v160.5:assets` résout `assets.jar` (vérifié, utile pour les tests, voir la section 4).
- `mindustryVersion` vaut `"v160"` par défaut. On peut mettre `"v160.5"`, car `includeVersion` filtre exactement cette version.
- Ne jamais utiliser `implementation` pour Mindustry ou Arc (le jar contiendrait tout le jeu). Utiliser `compileOnly`.

### gradle-wrapper.properties
```
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-9.8.0-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists

```

### mod.hjson (template)
```hjson
#the mod name as displayed in-game
displayName: "Java Mod Template"

#the internal name of your mod
name: "example-java-mod"

#your name
author: "You"

#the fully qualified main class of the mod
main: "example.ExampleJavaMod"

#the mod description as seen in the mod dialog
description: "A Mindustry Java mod template."

#the mod version
version: 1.0.0

#the minimum game build required to run this mod
minGameVersion: 160.3

#this is a java mod
java: true
```
Autres champs de `Mods.ModMeta` (v160.5) :
- `subtitle`, `repo` (« user/repo », utilisé par le navigateur de mods)
- `dependencies` et `softDependencies` (listes de noms de mods)
- `hidden` (mod uniquement côté serveur ou client, **ne peut pas ajouter de contenu**, n'est pas exigé des clients)
- `iosCompatible`, `texturescale` (1.0 par défaut), `pregenerated`
- `contentOrder` (String[]), `legacyCompatible` ; `hideBrowser: true` est lu par l'indexeur du navigateur de mods (README), pas par le jeu

`name` est le **préfixe de tout le contenu**. Avec `name: "example-java-mod"`, un `new Item("steel")` porte le nom interne `example-java-mod-steel` (`ContentLoader.transformName` : `currentMod.name + "-" + name`).

### Construire et installer
- `./gradlew jar` produit `build/libs/<projet>Desktop.jar`. Le jar contient les classes, `mod.hjson`, `icon.png` et le contenu de `assets/**` placé à la racine. Ce jar fonctionne **sur desktop uniquement**.
- `./gradlew deploy` produit `build/libs/<projet>.jar`, utilisable sur desktop et Android. Il faut le SDK Android, `ANDROID_HOME` et `d8` dans le PATH (`build-tools/<ver>`).
- **Dossier des mods sur macOS** (version non-Steam) : `~/Library/Application Support/Mindustry/mods/`. Ce chemin vient de `OS.getAppDataDirectoryString("Mindustry")` suivi de `mods/`. On peut copier le jar à cet endroit, ou utiliser « Mods > Importer ».
  - ⚠ Version Steam : `Vars.loadSettings()` fixe le dossier de données à `Core.files.local("saves/")`, donc relatif au dossier d'installation Steam du jeu. Le chemin exact sur macOS n'a pas été vérifié.
  - On peut forcer le dossier de données avec `-Dmindustry.data.dir=…` ou la variable d'environnement `MINDUSTRY_DATA_DIR` (`ClientLauncher.setup`).
- Exemple de déploiement local :
  ```bash
  ./gradlew jar && cp build/libs/*Desktop.jar ~/Library/Application\ Support/Mindustry/mods/
  ```

## 3. API essentielle (v160.5)

### 3.1 Cycle de vie de `mindustry.mod.Mod`
```java
public abstract class Mod{
    public Fi getConfigFolder();   // mods/<nom>/
    public Fi getConfig();         // mods/<nom>/config.json
    public void init();            // après la création de tous les modules ; ui existe côté client
    public void loadContent();     // création du contenu
    public void packSprites(MultiPacker packer);
    public void registerServerCommands(CommandHandler handler);
    public void registerClientCommands(CommandHandler handler);
}
```
Ordre côté client (`ClientLauncher`) :
1. **Constructeur du mod** : enregistrer ici les `Events.on` et `SaveVersion.addCustomChunk`.
2. `content.createBaseContent()` : contenu vanilla, **y compris les tech trees** Serpulo et Erekir.
3. `content.createModContent()`, qui appelle `mod.loadContent()`.
4. `new UI()`, puis `content.init()`, puis l'événement `ContentInitEvent`, puis `content.load()` (icônes et régions).
5. `init()` de chaque module, puis **`Mod.init()`**, puis l'événement **`ClientLoadEvent`**.

Sur un serveur, `Vars.headless == true` et `Vars.ui == null`. Toujours protéger le code UI avec `if(!Vars.headless)` ou en le plaçant dans `ClientLoadEvent`.

### 3.2 Événements (`arc.Events`, `mindustry.game.EventType.*`)
```java
Events.on(ClientLoadEvent.class, e -> {});   // fin du chargement client
Events.on(WorldLoadEvent.class, e -> {});    // monde chargé (nouvelle partie ou save)
Events.on(SaveLoadEvent.class, e -> {});     // e.isMap (boolean)
Events.on(SaveWriteEvent.class, e -> {});    // déclenché dans SaveIO.write, juste avant l'écriture
Events.on(ResetEvent.class, e -> {});        // retour au menu / reset de l'état
Events.on(StateChangeEvent.class, e -> {});  // e.from, e.to (GameState.State)
Events.on(WaveEvent.class, e -> {});
Events.on(ContentInitEvent.class, e -> {});
Events.run(Trigger.update, () -> {});        // chaque frame (utiliser Vars.state.isPlaying())
Events.run(Trigger.draw, () -> {});          // dessin dans le monde
```
Autres déclencheurs de l'enum `Trigger` : `beforeGameUpdate`, `afterGameUpdate`, `drawOver`, `preDraw`, `postDraw`, `uiDrawBegin`, `uiDrawEnd`, `newGame`, etc.

Autres événements utiles : `BlockBuildEndEvent`, `BlockDestroyEvent`, `UnitCreateEvent`, `UnitDestroyEvent`, `TileChangeEvent`, `CoreChangeEvent`, `ResearchEvent`, `UnlockEvent`, `SectorCaptureEvent`, `GameOverEvent`, `PlayEvent`, `WorldLoadBeginEvent`, `WorldLoadEndEvent`.

### 3.3 Contenu (Item, Liquid, GenericCrafter, Conveyor, Wall, ItemTurret, UnitType) et tech tree
Le tech tree vanilla est déjà construit quand `loadContent()` s'exécute. Le contexte statique de `TechTree.node(...)` n'est donc plus actif, et il faut s'accrocher avec **`new TechNode(parent.techNode, contenu, exigences)`**. Le constructeur ajoute le nœud aux enfants du parent et assigne `content.techNode`.

Le code ci-dessous a été vérifié à la compilation et en test headless.
```java
package example;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.content.TechTree.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;

import static mindustry.type.ItemStack.*;

public class ModContent{
    public static Item steel;
    public static Liquid coolant;
    public static Block steelForge, steelConveyor, steelWall, steelTurret;
    public static UnitType scout;

    public static void load(){
        steel = new Item("steel", Color.valueOf("8a9ba8")){{
            hardness = 3;
            cost = 1.2f;
        }};

        coolant = new Liquid("coolant", Color.valueOf("7fd4ff")){{
            heatCapacity = 0.9f;
            temperature = 0.25f;
        }};

        steelForge = new GenericCrafter("steel-forge"){{
            requirements(Category.crafting, with(Items.copper, 60, Items.lead, 40));
            size = 2;
            craftTime = 60f;
            hasPower = true;
            outputItem = new ItemStack(steel, 1);
            consumeItems(with(Items.coal, 1, Items.titanium, 1));
            consumePower(1f);
        }};

        steelConveyor = new Conveyor("steel-conveyor"){{
            requirements(Category.distribution, with(steel, 1, Items.copper, 1));
            health = 90;
            speed = 0.06f;
            displayedSpeed = 8f;
        }};

        steelWall = new Wall("steel-wall"){{
            requirements(Category.defense, with(steel, 6));
            health = 140 * 4;
        }};

        steelTurret = new ItemTurret("steel-turret"){{
            requirements(Category.turret, with(steel, 50, Items.copper, 40));
            ammo(
                Items.graphite, new BasicBulletType(3.5f, 20){{
                    width = 9f; height = 12f; lifetime = 50f;
                }},
                steel, new BasicBulletType(4f, 32){{
                    width = 10f; height = 13f; lifetime = 45f; pierce = true;
                }}
            );
            size = 2;
            reload = 25f;
            range = 180f;
            health = 600;
            limitRange();
        }};

        scout = new UnitType("scout"){{
            constructor = UnitEntity::create; //flying
            flying = true;
            speed = 2.5f;
            health = 120;
            hitSize = 9f;
            weapons.add(new Weapon(){{
                reload = 20f; x = 2f; y = 1f;
                bullet = new BasicBulletType(2.5f, 8);
            }});
        }};
    }

    public static void loadTechTree(){
        attach(Blocks.graphitePress, steelForge);
        attach(Blocks.conveyor, steelConveyor);
        attach(Blocks.copperWall, steelWall);
        attach(Blocks.duo, steelTurret);
        //item: produced objective
        TechNode n = new TechNode(Items.graphite.techNode, steel, steel.researchRequirements());
        n.objectives.add(new mindustry.game.Objectives.Produce(steel));
    }

    static void attach(mindustry.ctype.UnlockableContent parent, mindustry.ctype.UnlockableContent child){
        new TechNode(parent.techNode, child, child.researchRequirements());
    }
}
```
Notes :
- `ItemStack.with(Object...)` et `requirements(Category, ItemStack[])` existent tous les deux.
- Les champs de `GenericCrafter` sont `outputItem`, `outputLiquid`/`outputLiquids`, `craftTime` et `drawer`. Les méthodes de consommation sont `consumeItem`, `consumeItems`, `consumeLiquid` et `consumePower`.
- `UnitType` : pour un mod, il faut **assigner `constructor`**. Exemples : `MechUnit::create` (unité terrestre à pattes), `UnitEntity::create` (unité volante), `LegsUnit::create`, `UnitWaterMove::create`, `PayloadUnit::create`, `TankUnit::create`. Par défaut, c'est `EntityMapping.map(name)` ou `UnitEntity::create`, et `init()` enregistre le nom dans `EntityMapping`. Pour rendre l'unité constructible : `new UnitFactory.UnitPlan(type, tempsTicks, with(...))` dans le champ `plans` d'une `UnitFactory`.
- `ItemTurret.ammo(Object...)` prend des paires Item / BulletType. `limitRange()` ajuste la portée des balles.

### 3.4 Bundles (`assets/bundles/bundle.properties`, `bundle_fr.properties`)
Les clés de contenu suivent la forme `<type>.<nomInterne>.name|description|details`, avec un nom interne **préfixé** par le nom du mod (`UnlockableContent`).
```properties
item.example-java-mod-steel.name = Acier
item.example-java-mod-steel.description = Alliage robuste.
liquid.example-java-mod-coolant.name = Liquide de refroidissement
block.example-java-mod-steel-forge.name = Forge d'acier
block.example-java-mod-steel-conveyor.name = Convoyeur en acier
unit.example-java-mod-scout.name = Éclaireur
setting.example-show-graph.name = Afficher le graphe
setting.example-interval.name = Intervalle
keybind.example-toggle-hud.name = Basculer le HUD
category.example-java-mod.name = Example Mod
```
Les clés `setting.<nom>.name` (et `.description`) servent à `SettingsTable`. Les clés `keybind.<nom>.name` et `category.<cat>.name` servent à `KeybindDialog`. Dans le code, utiliser `Core.bundle.get("clé")` ou `Core.bundle.format("clé", args)`. La chaîne `"@clé"` est résolue dans certains libellés de l'UI.

### 3.5 Sprites
- **Dossier** : `assets/sprites/**.png` (les sous-dossiers sont libres). Mindustry les empaquette au démarrage et **préfixe le nom de région par le nom du mod** : `sprites/items/steel.png` devient la région `example-java-mod-steel`. Pas de préfixe si le nom contient déjà `-<modname>-`, ni dans `sprites-override/`.
- Le nom du fichier doit donc correspondre au nom **non préfixé** du contenu. Pour l'item `steel`, le fichier est `steel.png`. L'icône est cherchée dans l'ordre : `<type>-<nom>-full`, `<nom>-full`, `<nom>`, `<type>-<nom>`, `<nom>1`. L'icône UI est `<type>-<nom>-ui`.
- **Blocs** : `steel-forge.png`. Suffixes courants : `-top`, `-team`, `-liquid`, `-heat`, `-preview`, `-base` (tourelles, sinon `block-<taille>`), convoyeurs : `@Load("@-#1-#2", lengths = {7, 4})` donc `steel-conveyor-0-0.png` … `steel-conveyor-6-3.png` (7 formes × 4 frames d'animation).
- **Unités** : `scout.png`, `scout-cell.png`, plus `-leg`/`-base` selon le type. Les noms de `Weapon` ne sont **pas** préfixés automatiquement (`Core.atlas.find(name)`). Écrire `new Weapon("example-java-mod-gun")` pour un sprite du mod.
- Les sous-dossiers `blocks/environment`, `rubble` et `ui` vont dans des pages d'atlas dédiées.
- Dans le code : `Core.atlas.find("example-java-mod-frog")`.

### 3.6 Settings, keybinds, UI, dessin, accès au noyau, sauvegarde
Classe principale vérifiée à la compilation :
```java
package example;

import arc.*;
import arc.input.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.mod.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.content.*;

import java.io.*;

public class ExampleJavaMod extends Mod{
    public static KeyBind toggleHud;
    public static int counter;
    GraphElement graph;

    public ExampleJavaMod(){
        SaveVersion.addCustomChunk("example-java-mod-data", new SaveFileReader.CustomChunk(){
            @Override public void write(DataOutput stream) throws IOException{
                stream.writeByte(1);
                stream.writeInt(counter);
            }
            @Override public void read(DataInput stream) throws IOException{
                byte version = stream.readByte();
                counter = stream.readInt();
            }
            @Override public boolean writeNet(){ return false; }
        });

        Events.on(WorldLoadEvent.class, e -> { /* nouvelle carte ou save chargee */ });
        Events.on(SaveLoadEvent.class, e -> Log.info("save loaded, counter=@", counter));
        Events.on(SaveWriteEvent.class, e -> {});
        Events.on(ResetEvent.class, e -> counter = 0);
        Events.run(Trigger.update, () -> {
            if(!Vars.state.isPlaying()) return;
            if(Vars.player == null || Vars.player.team().core() == null) return;
            if(Vars.state.tick % 60 < 1 && graph != null){
                graph.push(Vars.player.team().core().items.get(Items.copper));
            }
        });

        Events.on(ClientLoadEvent.class, e -> {
            Vars.ui.settings.addCategory("Example", Icon.settings, t -> {
                t.checkPref("example-show-graph", true);
                t.sliderPref("example-interval", 60, 10, 600, 10, v -> v + " ticks");
            });

            Vars.ui.hudGroup.fill(t -> {
                t.top().right().marginTop(120f);
                t.table(Styles.black6, p -> {
                    p.label(() -> "Cuivre: " + (Vars.player.team().core() == null ? 0 : Vars.player.team().core().items.get(Items.copper))).row();
                    graph = new GraphElement();
                    p.add(graph).size(200f, 80f);
                }).visible(() -> Core.settings.getBool("example-show-graph"));
            });
        });
    }

    @Override
    public void init(){
        if(Vars.headless) return;
        toggleHud = KeyBind.add("example-toggle-hud", KeyCode.h, "example-java-mod");
        Events.run(Trigger.update, () -> {
            if(Core.input.keyTap(toggleHud) && !Core.scene.hasField()){
                Core.settings.put("example-show-graph", !Core.settings.getBool("example-show-graph"));
            }
        });
    }

    @Override
    public void loadContent(){
        ModContent.load();
        ModContent.loadTechTree();
    }
}
```
Élément de graphe personnalisé (`Element.draw()`, avec `Draw`, `Lines` et `Fill` d'Arc) :
```java
package example;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.struct.*;
import mindustry.graphics.*;

public class GraphElement extends Element{
    public final FloatSeq values = new FloatSeq();
    public int maxPoints = 120;

    public void push(float v){
        values.add(v);
        if(values.size > maxPoints) values.removeIndex(0);
    }

    @Override
    public void draw(){
        validate();
        Draw.color(0f, 0f, 0f, 0.5f * parentAlpha);
        Fill.crect(x, y, width, height);

        if(values.size >= 2){
            float max = 1f;
            for(int i = 0; i < values.size; i++) max = Math.max(max, values.get(i));
            Lines.stroke(2f);
            Draw.color(Pal.accent, parentAlpha);
            float step = width / (maxPoints - 1);
            for(int i = 1; i < values.size; i++){
                Lines.line(
                    x + (i - 1) * step, y + values.get(i - 1) / max * height,
                    x + i * step, y + values.get(i) / max * height);
            }
        }
        Draw.reset();
    }
}
```
Rappels d'API (vérifiés) :
- **Settings** : `Vars.ui.settings.addCategory(String name, Cons<SettingsTable>)`. Il existe aussi des surcharges avec une `Drawable` (`Icon.*`) ou une `String region`. `SettingsTable` propose `checkPref(name, def[, Boolc])`, `sliderPref(name, def, min, max[, step], StringProcessor[, Intc])`, `textPref(name, def[, Cons<String>])`, `areaTextPref`, `pref(Setting)`. La lecture et l'écriture passent par `Core.settings.getBool/getInt/getString(name, def)` et `Core.settings.put(name, v)`.
- **Keybinds** (Arc `arc.input.KeyBind`) : `KeyBind.add(String name, KeybindValue def, String category)`, où `def` est un `KeyCode` ou un `new KeyBind.Axis(min, max)`. La valeur est chargée et sauvegardée automatiquement dans Settings, et le raccourci apparaît dans le dialogue des contrôles. Pour la lecture : `Core.input.keyTap(bind)`, `keyDown(bind)`, `axis(bind)`. La surcharge sans catégorie n'est « pas pour les mods ». ⚠ Le moment de création (dans `init()`) est vérifié à la compilation seulement. La documentation d'Arc impose seulement que `Core.settings` soit chargé, ce qui est le cas.
- **UI scene2d** : `Vars.ui.hudGroup` (un `WidgetGroup`, visible seulement en jeu). `Group.fill(Cons<Table>)` crée une `Table` qui remplit le parent. Côté `Table` : `add(...)`, `row()`, `label(Prov<CharSequence>)`, `button(String, Runnable)`, `image(TextureRegion|Drawable)`, `table(Drawable, Cons<Table>)`, `top()/right()/margin*`. Côté `Element` : `update(Runnable)` et `visible(Boolp)`. Pour les dialogues : `new BaseDialog("titre")` avec `cont`, `buttons`, `addCloseButton()`, `show()`, `hide()`. Pour les styles : `mindustry.ui.Styles` (`black6`, `defaultt`, `cleart`…), `mindustry.gen.Icon`, `mindustry.gen.Tex`, `mindustry.graphics.Pal`.
- **Dessin** : `Fill.rect(x, y, w, h)` est **centré** sur (x, y), alors que `Fill.crect` part du coin. `Lines.stroke(float)`, `Lines.line(x1, y1, x2, y2)`, `Lines.polyline(FloatSeq, boolean wrap)`, `Lines.rect(x, y, w, h)`, `Draw.color(Color[, alpha])`, `Draw.reset()`. Multiplier par `parentAlpha` pour respecter les fondus.
- **Ressources du noyau** : `Vars.player.team().core()` renvoie un `CoreBuild` (peut être null). Sur ce noyau, `.items` est un `ItemModule` avec `get(Item)`, `has(Item, int)`, `total()`, `add`, `remove`. Alternative : `Vars.state.rules.defaultTeam.core()`.
- **Données dans la save** : `SaveVersion.addCustomChunk(String name, SaveFileReader.CustomChunk chunk)` est statique. Enregistrer le chunk tôt (constructeur du mod). Les chunks inconnus sont ignorés à la lecture (`skipChunk`), ce qui garde la compatibilité si le mod est retiré.
  ```java
  public interface CustomChunk{ // mindustry.io.SaveFileReader.CustomChunk
      void write(DataOutput stream) throws IOException;
      void read(DataInput stream) throws IOException;
      default void read(DataInput stream, int length) throws IOException{ read(stream); }
      default boolean shouldWrite(){ return true; }
      default boolean writeNet(){ return true; } // aussi envoyé aux clients qui se connectent (NetworkIO)
  }
  ```
  Le chunk est écrit dans la région `"custom"`, en dernier dans la save. Penser à remettre l'état à zéro sur `ResetEvent`, ou avant chaque chargement : `read` n'est pas appelé si la save ne contient pas le chunk. Préfixer le nom par le nom du mod et écrire un octet de version.
  Alternatives :
  - `Vars.state.rules.tags` (`StringMap`, sérialisé avec les règles) pour de petites valeurs texte.
  - Redéfinir `write(Writes)`, `read(Reads, byte revision)` et `version()` dans une sous-classe de `Building` pour des données par bloc.
  - `Core.settings` pour des données globales indépendantes de la save.

## 4. Tests unitaires headless (JUnit)

**Oui, c'est faisable**, avec deux niveaux :

1. **Logique pure (recommandé en priorité)** : mettre les calculs (statistiques, séries pour graphes, règles métier, sérialisation de vos données) dans un paquet sans `import mindustry.*`/`arc.*` côté UI, par exemple `src/<mod>/logic/`. Ces classes se testent avec JUnit seul, sans aucun démarrage du jeu. Pour le code des chunks, travailler sur `DataOutput`/`DataInput` permet des tests aller-retour avec `ByteArrayOutputStream`.
2. **Intégration headless** : `dependencies.jar` contient `arc.backend.headless.HeadlessApplication` et tout `mindustry.*`. Il manque les fichiers internes (cartes, bundles) : sans eux, `Vars.init()` échoue sur `maps/default/maze.msav`. La solution vérifiée : résoudre `assets.jar` via le classifier `assets`, le décompresser, et lancer les tests avec ce dossier comme `workingDir`. `Core.bundle` vaut par défaut un bundle vide, et `loadLocales = false` évite de charger les traductions.

Ajouts à `build.gradle.kts` (vérifiés : 2 tests passent) :
```kotlin
sourceSets.test{
    java.setSrcDirs(listOf("test"))
}

dependencies{
    testImplementation(if(mindustryVersion == "be") "Anuken:MindustryBuilds:latest" else "Anuken:Mindustry:$mindustryVersion")
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val gameAssets by configurations.creating

dependencies{
    gameAssets("Anuken:Mindustry:$mindustryVersion:assets")
}

val unpackGameAssets = tasks.register<Sync>("unpackGameAssets"){
    from(gameAssets.elements.map{ els -> els.map{ zipTree(it) } })
    into(layout.buildDirectory.dir("game-assets"))
}

tasks.test{
    dependsOn(unpackGameAssets)
    workingDir = layout.buildDirectory.dir("game-assets/assets").get().asFile
    useJUnitPlatform()
    testLogging{ showStandardStreams = true; exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL }
}
```
Ces dépendances restent hors du jar, car la tâche `jar` n'empaquette que `runtimeClasspath` (jar de 56 Ko vérifié).

`test/example/HeadlessTest.java` (même démarrage que `ServerLauncher` et `tests/ApplicationTests` de Mindustry) :
```java
package example;

import arc.*;
import arc.backend.headless.*;
import arc.files.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.game.*;
import mindustry.mod.*;
import mindustry.net.*;
import org.junit.jupiter.api.*;

import static mindustry.Vars.*;
import static org.junit.jupiter.api.Assertions.*;

public class HeadlessTest{
    static volatile boolean ready;
    static volatile Throwable error;

    @BeforeAll
    static void boot() throws Exception{
        new HeadlessApplication(new ApplicationCore(){
            @Override
            public void setup(){
                Core.settings.setDataDirectory(new Fi(System.getProperty("java.io.tmpdir")).child("mindustry-test-data"));
                headless = true;
                loadLocales = false;
                net = new Net(null);
                Vars.init();
                world = new World();
                content.createBaseContent();
                ModContent.load();
                ModContent.loadTechTree();
                content.init();
                add(logic = new Logic());
            }
            @Override
            public void init(){
                super.init();
                ready = true;
            }
        }, t -> error = t);
        long start = System.currentTimeMillis();
        while(!ready){
            if(error != null) fail(error);
            if(System.currentTimeMillis() - start > 30000) fail("timeout");
            Thread.sleep(10);
        }
    }

    @Test
    void contentLoaded(){
        assertEquals("copper", Items.copper.name);
        assertNotNull(content.getByName(ContentType.block, "conveyor"));
        assertNotNull(content.getByName(ContentType.block, "steel-forge"));
        assertEquals(Blocks.graphitePress.techNode, ModContent.steelForge.techNode.parent);
        assertEquals(2, ModContent.steelTurret.size);
    }

    @Test
    void customChunkRoundTrip() throws Exception{
        var bout = new java.io.ByteArrayOutputStream();
        new java.io.DataOutputStream(bout).writeInt(42);
        assertEquals(42, new java.io.DataInputStream(new java.io.ByteArrayInputStream(bout.toByteArray())).readInt());
    }
}
```
Limites et conseils :
- Sans mod courant (`content.setCurrentMod`), le contenu créé en test **n'est pas préfixé** (`steel-forge` au lieu de `example-java-mod-steel-forge`).
- La boucle headless tourne dans un thread séparé : synchroniser l'accès (`Core.app.post`) si vous simulez des ticks. Pour un monde réel, faire comme `ApplicationTests` : `maps.loadInternalMap("serpulo/groundZero")`, `world.loadMap(...)`, `state.set(State.playing)`, puis `logic.update()` ou `Time.setDeltaProvider(() -> 1f)`.
- Mindustry utilise `forkEvery = 1` (un JVM par classe de test) car l'état global (`Vars`) est statique. Faire de même si plusieurs classes démarrent le jeu.
- Pas d'UI ni de rendu en headless (`MockGraphics`) : `Element.draw()`, `Vars.ui` et l'atlas ne sont pas testables ainsi.

Structure recommandée :
```
src/<mod>/            Mod principal, contenu, UI (dépend de Mindustry)
src/<mod>/logic/      logique pure, testable sans le jeu
test/<mod>/logic/     tests JUnit purs (rapides)
test/<mod>/it/        tests d'intégration headless (HeadlessApplication + assets.jar)
```

## Sources
- https://api.github.com/repos/Anuken/Mindustry/releases/latest (v160.5)
- https://github.com/Anuken/MindustryJavaModTemplate (commit 32898e9, 2026-10-03)
- https://github.com/Anuken/Mindustry/tree/v160.5 : `core/src/mindustry/{mod/Mod.java, mod/Mods.java, io/SaveVersion.java, io/SaveFileReader.java, game/EventType.java, content/TechTree.java, ui/dialogs/SettingsMenuDialog.java, core/UI.java, ClientLauncher.java, Vars.java}`, `server/.../ServerLauncher.java`, `tests/.../ApplicationTests.java`
- https://github.com/Anuken/Arc/tree/8eb00ffff0 : `arc/input/KeyBind.java`, `arc/scene/Group.java`, `arc/graphics/g2d/{Lines,Fill,Draw}.java`, `arc/util/OS.java`
- Wiki : https://mindustrygame.github.io/wiki/modding/ (non relu en détail ; le code source fait foi)
