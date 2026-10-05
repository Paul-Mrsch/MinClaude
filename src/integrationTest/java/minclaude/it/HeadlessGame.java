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
                mod.loadContent();
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

    /** Racine du projet (fichiers du mod), transmise par Gradle. */
    public static Path projectDir(){
        return Path.of(System.getProperty("minclaude.projectDir", "."));
    }
}
