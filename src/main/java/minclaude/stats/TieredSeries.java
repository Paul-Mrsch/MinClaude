package minclaude.stats;

import java.io.*;

/**
 * Historique multi-résolution d'une métrique, alimenté par un échantillon par seconde de jeu :
 * <ul>
 *     <li>fin : 1 s, 10 dernières minutes ;</li>
 *     <li>moyen : moyenne sur 10 s, dernière heure ;</li>
 *     <li>grossier : toute la partie, pas initial de 60 s qui double à chaque compactage.</li>
 * </ul>
 */
public final class TieredSeries{
    public static final int FINE_CAPACITY = 600;
    public static final int MEDIUM_STEP = 10;
    public static final int MEDIUM_CAPACITY = 360;
    public static final int COARSE_CAPACITY = 720;
    public static final int COARSE_BASE_STEP = 60;

    private final RingSeries fine = new RingSeries(FINE_CAPACITY);
    private final RingSeries medium = new RingSeries(MEDIUM_CAPACITY);
    private final CompactingSeries coarse = new CompactingSeries(COARSE_CAPACITY, COARSE_BASE_STEP);
    private float mediumAcc;
    private int mediumCount;
    private long samples;

    public void add(float value){
        fine.add(value);
        mediumAcc += value;
        if(++mediumCount >= MEDIUM_STEP){
            medium.add(mediumAcc / mediumCount);
            mediumAcc = 0f;
            mediumCount = 0;
        }
        coarse.add(value);
        samples++;
    }

    /** Nombre total d'échantillons reçus (= secondes suivies). */
    public long samples(){
        return samples;
    }

    public float latest(){
        return fine.size() == 0 ? 0f : fine.get(fine.size() - 1);
    }

    /** Les {@code seconds} dernières secondes à pleine résolution (au plus 600). */
    public float[] recent(int seconds){
        return fine.last(seconds);
    }

    /** Choisit la résolution la plus fine qui couvre la plage demandée. */
    public SeriesView view(TimeRange range){
        int wanted = range.seconds < 0 ? (int)Math.min(Integer.MAX_VALUE, samples) : range.seconds;
        if(wanted <= FINE_CAPACITY){
            return new SeriesView(fine.last(wanted), 1f);
        }
        if(wanted <= MEDIUM_CAPACITY * MEDIUM_STEP){
            return new SeriesView(medium.last(wanted / MEDIUM_STEP), MEDIUM_STEP);
        }
        return new SeriesView(coarse.toArray(), coarse.step());
    }

    void write(DataOutput out) throws IOException{
        out.writeLong(samples);
        fine.write(out);
        medium.write(out);
        out.writeFloat(mediumAcc);
        out.writeInt(mediumCount);
        coarse.write(out);
    }

    void read(DataInput in) throws IOException{
        samples = in.readLong();
        fine.read(in);
        medium.read(in);
        mediumAcc = in.readFloat();
        mediumCount = in.readInt();
        coarse.read(in);
    }
}
