package minclaude.tracking;

import arc.util.Log;
import minclaude.stats.HistoryCodec;
import minclaude.stats.MetricHistory;
import mindustry.io.SaveFileReader.CustomChunk;

import java.io.*;

/**
 * Enregistre l'historique dans la sauvegarde. La lecture consomme exactement {@code length} octets avant de les décoder :
 * un historique corrompu ou d'une version future est ignoré sans jamais casser le chargement de la partie.
 */
public final class HistoryChunk implements CustomChunk{
    private final ResourceTracker tracker;

    public HistoryChunk(ResourceTracker tracker){
        this.tracker = tracker;
    }

    @Override
    public void write(DataOutput stream) throws IOException{
        HistoryCodec.write(stream, tracker.history());
    }

    @Override
    public void read(DataInput stream, int length) throws IOException{
        byte[] data = new byte[length];
        stream.readFully(data);
        try{
            tracker.setHistory(HistoryCodec.fromBytes(data));
        }catch(Exception e){
            Log.err("[MinClaude] Historique illisible, il est ignoré.", e);
            tracker.setHistory(new MetricHistory());
        }
    }

    @Override
    public void read(DataInput stream) throws IOException{
        // Utilisé seulement si le jeu ne fournit pas la longueur : décodage direct.
        tracker.setHistory(HistoryCodec.read(stream));
    }

    @Override
    public boolean writeNet(){
        // Le mod est solo (PC) : inutile d'envoyer l'historique aux clients.
        return false;
    }
}
