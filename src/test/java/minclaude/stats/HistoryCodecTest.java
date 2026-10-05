package minclaude.stats;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class HistoryCodecTest{
    private static MetricHistory sample(int seconds){
        MetricHistory h = new MetricHistory();
        for(int i = 0; i < seconds; i++){
            h.record("item/copper/stock", i * 3);
            h.record("item/lead/stock", 1000 - i);
            h.tick();
        }
        return h;
    }

    @Test
    void roundTripPreservesEveryView() throws IOException{
        MetricHistory h = sample(5000);
        MetricHistory back = HistoryCodec.fromBytes(HistoryCodec.toBytes(h));
        assertEquals(h.elapsedSeconds(), back.elapsedSeconds());
        assertEquals(h.keys(), back.keys());
        for(String key : h.keys()){
            for(TimeRange r : TimeRange.values()){
                SeriesView a = h.view(key, r), b = back.view(key, r);
                assertArrayEquals(a.values(), b.values(), key + " " + r);
                assertEquals(a.stepSeconds(), b.stepSeconds());
            }
            assertEquals(h.get(key).samples(), back.get(key).samples());
        }
    }

    @Test
    void restoredHistoryContinuesAccumulatingIdentically() throws IOException{
        // Sauvegarder au milieu d'un pas de 10 s puis continuer doit donner le même résultat que sans sauvegarde.
        MetricHistory a = sample(1234);
        MetricHistory b = HistoryCodec.fromBytes(HistoryCodec.toBytes(a));
        for(int i = 0; i < 777; i++){
            a.record("item/copper/stock", i);
            b.record("item/copper/stock", i);
        }
        for(TimeRange r : TimeRange.values()){
            assertArrayEquals(a.view("item/copper/stock", r).values(), b.view("item/copper/stock", r).values());
        }
    }

    @Test
    void emptyHistoryRoundTrip() throws IOException{
        MetricHistory back = HistoryCodec.fromBytes(HistoryCodec.toBytes(new MetricHistory()));
        assertEquals(0, back.size());
    }

    @Test
    void rejectsForeignData(){
        assertThrows(IOException.class, () -> HistoryCodec.fromBytes(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}));
    }

    @Test
    void rejectsFutureVersion() throws IOException{
        var bytes = new ByteArrayOutputStream();
        var out = new DataOutputStream(bytes);
        out.writeInt(HistoryCodec.MAGIC);
        out.writeInt(HistoryCodec.VERSION + 1);
        assertThrows(IOException.class, () -> HistoryCodec.fromBytes(bytes.toByteArray()));
    }

    @Test
    void rejectsTruncatedData() throws IOException{
        byte[] full = HistoryCodec.toBytes(sample(100));
        assertThrows(IOException.class, () -> HistoryCodec.fromBytes(Arrays.copyOf(full, full.length / 2)));
    }

    @Test
    void formatV1IsStable() throws IOException{
        // Garde-fou : si ce test casse, le format a changé -> incrémenter VERSION et garder la lecture de l'ancien.
        MetricHistory h = new MetricHistory();
        h.record("k", 2f);
        h.tick();
        byte[] bytes = HistoryCodec.toBytes(h);
        // magic(4) version(4) elapsed(8) count(4) utf"k"(3) samples(8) fine(4+4) medium(4) acc(4) cnt(4) coarse step/acc/cnt/size(16)
        assertEquals(4 + 4 + 8 + 4 + 3 + 8 + 8 + 4 + 4 + 4 + 16, bytes.length);
    }
}
