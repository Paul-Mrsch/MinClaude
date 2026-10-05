package minclaude.it;

import arc.ApplicationCore;
import arc.Core;
import arc.backend.headless.HeadlessApplication;
import arc.files.Fi;
import arc.util.Log;
import minclaude.MinClaudeMod;
import mindustry.Vars;
import mindustry.core.*;
import mindustry.maps.Map;
import mindustry.net.Net;

import java.nio.file.Path;

import static mindustry.Vars.*;

/**
 * Démarre Mindustry en headless une fois par JVM, comme les tests officiels (ApplicationTests) :
 * contenu vanilla, puis contenu du mod, puis logique de jeu. Le répertoire de travail doit contenir les assets du jeu.
 */
public final class HeadlessGame{
    public static MinClaudeMod mod;
    public static Map groundZero;
    private static boolean started;

    private HeadlessGame(){}

    public static synchronized void start(){
        if(started) return;
        started = true;
        boolean[] ready = {false};
        Throwable[] error = {null};
        Log.useColors = false;

        new HeadlessApplication(new ApplicationCore(){
            @Override
            public void setup(){
                Fi data = new Fi(System.getProperty("java.io.tmpdir")).child("minclaude-test-data");
                data.deleteDirectory();
                Core.settings.setDataDirectory(data);
                headless = true;
                loadLocales = false;
                net = new Net(null);
                Vars.init();
                world = new World();
                content.createBaseContent();
                mod = new MinClaudeMod();
                try{
                    mod.loadContent();
                }catch(IllegalArgumentException e){
                    // En test, le préfixe du mod n'est pas appliqué : un nom identique au vanilla provoque ce conflit.
                    throw new AssertionError("Nom de contenu déjà utilisé par le jeu de base (renommer le contenu du mod) : "
                        + e.getMessage(), e);
                }
                add(logic = new Logic());
                content.init();
                mod.init();
            }

            @Override
            public void init(){
                super.init();
                groundZero = maps.loadInternalMap("serpulo/groundZero");
                ready[0] = true;
                // Arrête la boucle headless : les tests pilotent le jeu depuis leur propre thread.
                Thread.currentThread().interrupt();
            }
        }, t -> error[0] = t);

        long start = System.currentTimeMillis();
        while(!ready[0]){
            if(error[0] != null) throw new AssertionError("échec du démarrage headless", error[0]);
            if(System.currentTimeMillis() - start > 60_000) throw new AssertionError("démarrage headless trop long");
            try{
                Thread.sleep(10);
            }catch(InterruptedException e){
                throw new AssertionError(e);
            }
        }
    }

    /** Fait avancer toute la simulation (bâtiments, réseaux électriques, unités, tracker) de {@code ticks} ticks. */
    public static void run(int ticks){
        for(int i = 0; i < ticks; i++) logic.update();
    }

    /** Case libre (sol praticable, sans bloc) au coin bas-gauche d'une zone {@code w}x{@code h}, en partant du noyau. */
    public static mindustry.world.Tile freeArea(int w, int h){
        var core = state.rules.defaultTeam.core();
        int cx = core.tile.x, cy = core.tile.y;
        for(int r = 4; r < 60; r++){
            for(int dx = -r; dx <= r; dx++){
                for(int dy = -r; dy <= r; dy++){
                    if(Math.max(Math.abs(dx), Math.abs(dy)) != r) continue;
                    if(areaFree(cx + dx, cy + dy, w, h)) return world.tile(cx + dx, cy + dy);
                }
            }
        }
        throw new AssertionError("aucune zone libre " + w + "x" + h);
    }

    private static boolean areaFree(int x, int y, int w, int h){
        for(int i = -1; i <= w; i++){
            for(int j = -1; j <= h; j++){
                var t = world.tile(x + i, y + j);
                if(t == null || t.block() != mindustry.content.Blocks.air || t.floor().isLiquid || t.floor().solid || t.build != null) return false;
            }
        }
        return true;
    }

    /** Pose un bloc ; pour un bloc de taille paire, (x, y) est la case de référence du jeu. */
    public static mindustry.gen.Building place(mindustry.world.Block block, mindustry.game.Team team, int x, int y){
        world.tile(x, y).setBlock(block, team, 0);
        return world.tile(x, y).build;
    }

    /** Racine du projet (fichiers du mod), transmise par Gradle. */
    public static Path projectDir(){
        return Path.of(System.getProperty("minclaude.projectDir", "."));
    }
}
