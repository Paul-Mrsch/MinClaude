package minclaude.stats;

import java.util.Arrays;

/**
 * Estime les entrées et les sorties d'un stock en l'observant à chaque tick : une hausse compte comme une entrée,
 * une baisse comme une sortie. Une entrée et une sortie au même tick se compensent : c'est une estimation par défaut.
 */
public final class DeltaAccumulator{
    private static final int UNKNOWN = Integer.MIN_VALUE;

    private int[] previous;
    private float[] in, out;

    public DeltaAccumulator(int capacity){
        previous = new int[capacity];
        in = new float[capacity];
        out = new float[capacity];
        reset();
    }

    public void observe(int id, int stock){
        ensure(id);
        int prev = previous[id];
        if(prev != UNKNOWN){
            int d = stock - prev;
            if(d > 0) in[id] += d;
            else out[id] -= d;
        }
        previous[id] = stock;
    }

    /** Lit puis remet à zéro le total des entrées de {@code id}. */
    public float takeIn(int id){
        if(id >= in.length) return 0f;
        float v = in[id];
        in[id] = 0f;
        return v;
    }

    public float takeOut(int id){
        if(id >= out.length) return 0f;
        float v = out[id];
        out[id] = 0f;
        return v;
    }

    /** Oublie tout, à appeler au changement de carte pour ne pas compter le stock initial comme une entrée. */
    public void reset(){
        Arrays.fill(previous, UNKNOWN);
        Arrays.fill(in, 0f);
        Arrays.fill(out, 0f);
    }

    private void ensure(int id){
        if(id < previous.length) return;
        int n = Math.max(id + 1, previous.length * 2);
        int old = previous.length;
        previous = Arrays.copyOf(previous, n);
        Arrays.fill(previous, old, n, UNKNOWN);
        in = Arrays.copyOf(in, n);
        out = Arrays.copyOf(out, n);
    }
}
