package minclaude.tracking;

import arc.util.Log;
import minclaude.logic.StockGoals;
import mindustry.io.SaveFileReader.CustomChunk;

import java.io.*;

/** Enregistre les objectifs de stock dans la sauvegarde ; un bloc illisible est ignoré sans casser le chargement. */
public final class GoalsChunk implements CustomChunk{
    private final StockGoals goals;

    public GoalsChunk(StockGoals goals){
        this.goals = goals;
    }

    @Override
    public void write(DataOutput stream) throws IOException{
        goals.write(stream);
    }

    @Override
    public void read(DataInput stream, int length) throws IOException{
        byte[] data = new byte[length];
        stream.readFully(data);
        try{
            goals.read(new DataInputStream(new ByteArrayInputStream(data)));
        }catch(Exception e){
            Log.err("[MinClaude] Objectifs illisibles, ils sont ignorés.", e);
            goals.clear();
        }
    }

    @Override
    public void read(DataInput stream) throws IOException{
        goals.read(stream);
    }

    @Override
    public boolean writeNet(){
        return false;
    }
}
