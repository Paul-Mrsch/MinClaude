package minclaude.stats;

import java.io.*;

/**
 * Format binaire versionné de {@link MetricHistory}, enregistré dans la sauvegarde.
 * Tout changement de format incrémente {@link #VERSION} et garde la lecture des versions précédentes.
 */
public final class HistoryCodec{
    public static final int MAGIC = 0x4D434831; // "MCH1"
    public static final int VERSION = 1;

    private HistoryCodec(){}

    public static void write(DataOutput out, MetricHistory history) throws IOException{
        synchronized(history){
            out.writeInt(MAGIC);
            out.writeInt(VERSION);
            out.writeLong(history.elapsedSeconds());
            var keys = history.keys();
            out.writeInt(keys.size());
            for(String key : keys){
                out.writeUTF(key);
                history.get(key).write(out);
            }
        }
    }

    public static MetricHistory read(DataInput in) throws IOException{
        if(in.readInt() != MAGIC) throw new IOException("not a MinClaude history");
        int version = in.readInt();
        if(version < 1 || version > VERSION) throw new IOException("unsupported history version " + version);
        MetricHistory history = new MetricHistory();
        history.setElapsedSeconds(in.readLong());
        int count = in.readInt();
        if(count < 0) throw new IOException("negative key count");
        for(int i = 0; i < count; i++){
            String key = in.readUTF();
            TieredSeries s = new TieredSeries();
            s.read(in);
            history.put(key, s);
        }
        return history;
    }

    public static byte[] toBytes(MetricHistory history) throws IOException{
        var bytes = new ByteArrayOutputStream();
        write(new DataOutputStream(bytes), history);
        return bytes.toByteArray();
    }

    public static MetricHistory fromBytes(byte[] data) throws IOException{
        return read(new DataInputStream(new ByteArrayInputStream(data)));
    }
}
