package minclaude.stats;

import minclaude.stats.AlertEngine.Type;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlertEngineTest{
    private static final float CAP = 4000;
    private static final float INF = Float.POSITIVE_INFINITY;

    private static ResourceStats stats(float stock, float in, float slope, float toEmpty){
        return new ResourceStats(stock, CAP, in, 0, slope, toEmpty, INF);
    }

    private static List<Type> types(List<AlertEngine.Alert> alerts){
        return alerts.stream().map(AlertEngine.Alert::type).toList();
    }

    @Test
    void depletingFiresOnceWhileConditionHolds(){
        AlertEngine e = new AlertEngine();
        assertEquals(List.of(Type.DEPLETING), types(e.evaluate("copper", stats(1000, 0, -10, 100), 0)));
        assertTrue(e.evaluate("copper", stats(990, 0, -10, 99), 1).isEmpty(), "pas de répétition");
        assertTrue(e.isActive("copper", Type.DEPLETING));
    }

    @Test
    void refiresOnlyAfterClearAndCooldown(){
        AlertEngine e = new AlertEngine();
        e.evaluate("copper", stats(1000, 0, -10, 100), 0);
        e.evaluate("copper", stats(1000, 0, 0, INF), 10); // condition levée
        assertTrue(e.evaluate("copper", stats(1000, 0, -10, 100), 20).isEmpty(), "délai de 120 s pas écoulé");
        e.evaluate("copper", stats(1000, 0, 0, INF), 200);
        assertEquals(List.of(Type.DEPLETING), types(e.evaluate("copper", stats(1000, 0, -10, 100), 201)));
    }

    @Test
    void lowStockRequiresHavingBeenAbundant(){
        AlertEngine e = new AlertEngine();
        // Toujours rare depuis le début : pas d'alerte.
        assertFalse(types(e.evaluate("scrap", stats(10, 0, 0, INF), 0)).contains(Type.LOW_STOCK));
        e.evaluate("lead", stats(1000, 0, 0, INF), 0);
        assertTrue(types(e.evaluate("lead", stats(100, 0, 0, INF), 1)).contains(Type.LOW_STOCK));
    }

    @Test
    void coreFullOnlyWhenItemsKeepArriving(){
        AlertEngine e = new AlertEngine();
        assertTrue(e.evaluate("sand", stats(CAP, 0, 0, INF), 0).isEmpty());
        assertEquals(List.of(Type.CORE_FULL), types(e.evaluate("sand", stats(CAP, 5, 0, INF), 1)));
    }

    @Test
    void itemsAreIndependentAndResetClearsState(){
        AlertEngine e = new AlertEngine();
        e.evaluate("a", stats(1000, 0, -10, 100), 0);
        assertFalse(e.evaluate("b", stats(1000, 0, -10, 100), 0).isEmpty());
        e.reset();
        assertFalse(e.isActive("a", Type.DEPLETING));
        assertFalse(e.evaluate("a", stats(1000, 0, -10, 100), 0).isEmpty());
    }

    @Test
    void configIsHonoured(){
        AlertEngine e = new AlertEngine();
        e.config.depletingSeconds = 30;
        assertTrue(e.evaluate("copper", stats(1000, 0, -10, 100), 0).isEmpty());
    }
}
