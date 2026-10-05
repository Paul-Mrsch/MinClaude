package minclaude.stats;

import java.io.*;

/** Tampon circulaire de capacité fixe ; quand il est plein, la valeur la plus ancienne est écrasée. Index 0 = la plus ancienne. */
public final class RingSeries{
    private final float[] data;
    private int start, size;

    public RingSeries(int capacity){
        if(capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        data = new float[capacity];
    }

    public void add(float value){
        if(size < data.length){
            data[(start + size) % data.length] = value;
            size++;
        }else{
            data[start] = value;
            start = (start + 1) % data.length;
        }
    }

    public float get(int index){
        if(index < 0 || index >= size) throw new IndexOutOfBoundsException(index);
        return data[(start + index) % data.length];
    }

    public int size(){
        return size;
    }

    public int capacity(){
        return data.length;
    }

    public void clear(){
        start = size = 0;
    }

    /** Copie des {@code count} dernières valeurs (ou moins si la série est plus courte), de la plus ancienne à la plus récente. */
    public float[] last(int count){
        int n = Math.min(count, size);
        float[] out = new float[n];
        for(int i = 0; i < n; i++) out[i] = get(size - n + i);
        return out;
    }

    void write(DataOutput out) throws IOException{
        out.writeInt(size);
        for(int i = 0; i < size; i++) out.writeFloat(get(i));
    }

    void read(DataInput in) throws IOException{
        clear();
        int n = in.readInt();
        if(n < 0) throw new IOException("negative series size: " + n);
        for(int i = 0; i < n; i++) add(in.readFloat());
    }
}
