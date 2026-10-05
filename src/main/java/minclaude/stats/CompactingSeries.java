package minclaude.stats;

import java.io.*;

/**
 * Série couvrant toute la partie avec une mémoire bornée : on accumule {@code step} échantillons par point.
 * Quand le tableau est plein, les points sont fusionnés deux à deux (moyenne) et le pas double.
 */
public final class CompactingSeries{
    private final float[] data;
    private int size;
    private int step;
    private float acc;
    private int count;

    /** @param capacity nombre de points (pair) ; @param baseStep échantillons par point au départ */
    public CompactingSeries(int capacity, int baseStep){
        if(capacity < 2 || capacity % 2 != 0) throw new IllegalArgumentException("capacity must be even and >= 2");
        if(baseStep <= 0) throw new IllegalArgumentException("baseStep must be > 0");
        data = new float[capacity];
        step = baseStep;
    }

    public void add(float value){
        acc += value;
        if(++count >= step){
            data[size++] = acc / count;
            acc = 0f;
            count = 0;
            if(size == data.length) compact();
        }
    }

    private void compact(){
        int half = data.length / 2;
        for(int i = 0; i < half; i++) data[i] = (data[2 * i] + data[2 * i + 1]) / 2f;
        size = half;
        step *= 2;
    }

    public int size(){
        return size;
    }

    /** Échantillons de base par point. */
    public int step(){
        return step;
    }

    public float get(int index){
        if(index < 0 || index >= size) throw new IndexOutOfBoundsException(index);
        return data[index];
    }

    public float[] toArray(){
        float[] out = new float[size];
        System.arraycopy(data, 0, out, 0, size);
        return out;
    }

    void write(DataOutput out) throws IOException{
        out.writeInt(step);
        out.writeFloat(acc);
        out.writeInt(count);
        out.writeInt(size);
        for(int i = 0; i < size; i++) out.writeFloat(data[i]);
    }

    void read(DataInput in) throws IOException{
        int s = in.readInt();
        float a = in.readFloat();
        int c = in.readInt();
        int n = in.readInt();
        if(s <= 0 || c < 0 || n < 0 || n >= data.length) throw new IOException("invalid compacting series header");
        step = s;
        acc = a;
        count = c;
        size = n;
        for(int i = 0; i < n; i++) data[i] = in.readFloat();
    }
}
