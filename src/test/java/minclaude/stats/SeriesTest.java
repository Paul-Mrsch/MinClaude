package minclaude.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeriesTest{
    @Test
    void ringKeepsMostRecentValues(){
        RingSeries r = new RingSeries(3);
        for(int i = 1; i <= 5; i++) r.add(i);
        assertEquals(3, r.size());
        assertArrayEquals(new float[]{3, 4, 5}, r.last(10));
        assertArrayEquals(new float[]{4, 5}, r.last(2));
        assertEquals(3, r.get(0));
    }

    @Test
    void ringRejectsInvalidIndex(){
        RingSeries r = new RingSeries(2);
        r.add(1);
        assertThrows(IndexOutOfBoundsException.class, () -> r.get(1));
        assertThrows(IllegalArgumentException.class, () -> new RingSeries(0));
    }

    @Test
    void compactingAveragesBaseSamples(){
        CompactingSeries c = new CompactingSeries(4, 2);
        c.add(1);
        c.add(3);
        c.add(5);
        assertEquals(1, c.size());
        assertEquals(2f, c.get(0));
        assertEquals(2, c.step());
    }

    @Test
    void compactingHalvesAndDoublesStepWhenFull(){
        CompactingSeries c = new CompactingSeries(4, 1);
        for(int i = 1; i <= 4; i++) c.add(i);
        // [1,2,3,4] plein -> [1.5, 3.5], pas = 2
        assertEquals(2, c.size());
        assertEquals(2, c.step());
        assertArrayEquals(new float[]{1.5f, 3.5f}, c.toArray());
        c.add(10);
        c.add(20);
        assertArrayEquals(new float[]{1.5f, 3.5f, 15f}, c.toArray());
    }

    @Test
    void compactingStaysBoundedOverLongGames(){
        CompactingSeries c = new CompactingSeries(720, 60);
        // 100 heures de jeu
        for(int i = 0; i < 360_000; i++) c.add(i % 7);
        assertTrue(c.size() < 720);
        assertTrue(c.step() * c.size() <= 360_000);
        assertTrue(c.step() * c.size() > 360_000 / 2, "la série couvre toujours plus de la moitié de la partie");
    }

    @Test
    void tieredPicksResolutionForRange(){
        TieredSeries t = new TieredSeries();
        for(int i = 0; i < 4000; i++) t.add(i);

        SeriesView minute = t.view(TimeRange.MINUTE);
        assertEquals(60, minute.values().length);
        assertEquals(1f, minute.stepSeconds());
        assertEquals(3999f, minute.last());

        SeriesView tenMin = t.view(TimeRange.TEN_MINUTES);
        assertEquals(600, tenMin.values().length);

        SeriesView hour = t.view(TimeRange.HOUR);
        assertEquals(10f, hour.stepSeconds());
        assertEquals(360, hour.values().length);
        // Moyenne des secondes 3990..3999
        assertEquals(3994.5f, hour.last(), 1e-3);

        SeriesView whole = t.view(TimeRange.WHOLE_GAME);
        assertEquals(60f, whole.stepSeconds());
        assertEquals(4000 / 60, whole.values().length);
    }

    @Test
    void wholeGameUsesFineResolutionForShortGames(){
        TieredSeries t = new TieredSeries();
        for(int i = 0; i < 30; i++) t.add(i);
        SeriesView v = t.view(TimeRange.WHOLE_GAME);
        assertEquals(30, v.values().length);
        assertEquals(1f, v.stepSeconds());
    }

    @Test
    void historyIsEmptyForUnknownKey(){
        MetricHistory h = new MetricHistory();
        assertTrue(h.view("nope", TimeRange.MINUTE).isEmpty());
        h.record("a", 1);
        h.tick();
        assertEquals(1, h.elapsedSeconds());
        assertEquals(1, h.view("a", TimeRange.MINUTE).values().length);
    }
}
