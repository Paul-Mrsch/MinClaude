package minclaude.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisTest{
    @Test
    void slopeOfLinearSeries(){
        assertEquals(2f, Trend.slope(new float[]{0, 2, 4, 6, 8}, 1f), 1e-5);
        assertEquals(-0.5f, Trend.slope(new float[]{10, 5, 0}, 10f), 1e-5);
        assertEquals(0f, Trend.slope(new float[]{7}, 1f));
        assertEquals(0f, Trend.slope(new float[]{3, 3, 3}, 1f));
    }

    @Test
    void secondsUntilTarget(){
        assertEquals(50f, Trend.secondsUntil(100, -2, 0));
        assertEquals(Float.POSITIVE_INFINITY, Trend.secondsUntil(100, 2, 0), "s'éloigne de la cible");
        assertEquals(Float.POSITIVE_INFINITY, Trend.secondsUntil(100, 0, 0));
        assertEquals(0f, Trend.secondsUntil(0, -1, 0));
        assertEquals(10f, Trend.secondsUntil(90, 1, 100));
    }

    @Test
    void deltaAccumulatorSplitsInAndOut(){
        DeltaAccumulator d = new DeltaAccumulator(2);
        d.observe(0, 100); // première observation : pas une entrée
        d.observe(0, 110);
        d.observe(0, 105);
        d.observe(0, 120);
        assertEquals(25f, d.takeIn(0));
        assertEquals(5f, d.takeOut(0));
        assertEquals(0f, d.takeIn(0), "remis à zéro après lecture");
    }

    @Test
    void deltaAccumulatorGrowsAndResets(){
        DeltaAccumulator d = new DeltaAccumulator(1);
        d.observe(40, 1);
        d.observe(40, 4);
        assertEquals(3f, d.takeIn(40));
        d.reset();
        d.observe(40, 100);
        assertEquals(0f, d.takeIn(40), "après reset le stock initial n'est pas une entrée");
        assertEquals(0f, d.takeIn(999));
    }

    @Test
    void resourceStatsFromHistory(){
        MetricHistory h = new MetricHistory();
        for(int i = 0; i < 60; i++){
            h.record(ResourceStats.stockKey("copper"), 600 - i * 2);
            h.record(ResourceStats.inKey("copper"), 1);
            h.record(ResourceStats.outKey("copper"), 3);
            h.tick();
        }
        ResourceStats s = ResourceStats.compute(h, "copper", 4000, 60);
        assertEquals(482f, s.stock());
        assertEquals(-2f, s.slopePerSec(), 1e-4);
        assertEquals(1f, s.inPerSec(), 1e-5);
        assertEquals(3f, s.outPerSec(), 1e-5);
        assertEquals(-2f, s.netPerSec(), 1e-5);
        assertEquals(241f, s.secondsToEmpty(), 1e-2);
        assertEquals(Float.POSITIVE_INFINITY, s.secondsToFull());
    }

    @Test
    void resourceStatsForUnknownItem(){
        ResourceStats s = ResourceStats.compute(new MetricHistory(), "nope", 100, 60);
        assertEquals(0f, s.stock());
        assertTrue(Float.isInfinite(s.secondsToEmpty()));
    }

    @Test
    void formatting(){
        assertEquals("950", Format.amount(950));
        assertEquals("1.2k", Format.amount(1234));
        assertEquals("2.5M", Format.amount(2_500_000));
        assertEquals("-3k", Format.amount(-3000));
        assertEquals("+120/min", Format.ratePerMinute(2));
        assertEquals("-54/min", Format.ratePerMinute(-53.7f / 60f), "arrondi dès 10/min");
        assertEquals("+4.5/min", Format.ratePerMinute(4.5f / 60f));
        assertEquals("-30/min", Format.ratePerMinute(-0.5f));
        assertEquals("0/min", Format.ratePerMinute(0));
        assertEquals("45s", Format.duration(45));
        assertEquals("3m 20s", Format.duration(200));
        assertEquals("2h 05m", Format.duration(7500));
        assertEquals("∞", Format.duration(Float.POSITIVE_INFINITY));
    }
}
