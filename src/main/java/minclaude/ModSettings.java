package minclaude;

import arc.Core;
import mindustry.Vars;
import mindustry.gen.Icon;

/** Options du mod (Paramètres > MinClaude). Les clés sont aussi utilisées dans les bundles : setting.&lt;clé&gt;.name. */
public final class ModSettings{
    public static final String OVERLAY = "minclaude-overlay";
    public static final String OVERLAY_ROWS = "minclaude-overlay-rows";
    public static final String ALERTS = "minclaude-alerts";
    public static final String LOW_PERCENT = "minclaude-low-percent";
    public static final String DEPLETING_MINUTES = "minclaude-depleting-minutes";
    public static final String DIFFICULTY = "minclaude-difficulty";
    public static final String SMART_AI = "minclaude-smart-ai";
    public static final String ORES = "minclaude-ores";
    public static final String ENEMIES = "minclaude-enemies";

    private ModSettings(){}

    public static void register(){
        Vars.ui.settings.addCategory(Core.bundle.get("minclaude.settings"), Icon.chartBar, t -> {
            t.checkPref(OVERLAY, true);
            t.sliderPref(OVERLAY_ROWS, 6, 3, 15, 1, i -> String.valueOf(i));
            t.checkPref(ALERTS, true);
            t.sliderPref(LOW_PERCENT, 5, 1, 50, 1, i -> i + "%");
            t.sliderPref(DEPLETING_MINUTES, 3, 1, 15, 1, i -> i + " min");
            t.sliderPref(DIFFICULTY, 2, 1, 4, 1, i -> Core.bundle.get("minclaude.difficulty." + i));
            t.checkPref(SMART_AI, true);
            t.checkPref(ENEMIES, true);
            t.checkPref(ORES, true);
        });
    }

    public static boolean overlay(){
        return Core.settings.getBool(OVERLAY, true);
    }

    public static int overlayRows(){
        return Core.settings.getInt(OVERLAY_ROWS, 6);
    }

    public static boolean alerts(){
        return Core.settings.getBool(ALERTS, true);
    }

    public static float lowFraction(){
        return Core.settings.getInt(LOW_PERCENT, 5) / 100f;
    }

    public static int difficulty(){
        return Core.settings.getInt(DIFFICULTY, 2);
    }

    public static boolean smartAi(){
        return Core.settings.getBool(SMART_AI, true);
    }

    /** Ajouter les ennemis du mod aux vagues des nouvelles parties. */
    public static boolean enemies(){
        return Core.settings.getBool(ENEMIES, true);
    }

    /** Générer les gisements du mod dans les nouvelles parties. */
    public static boolean ores(){
        return Core.settings.getBool(ORES, true);
    }

    public static float depletingSeconds(){
        return Core.settings.getInt(DEPLETING_MINUTES, 3) * 60f;
    }
}
