package minclaude.logic;

import java.io.*;
import java.util.*;

/**
 * Objectifs de stock fixés par le joueur, par ressource. Une alerte est émise une seule fois quand l'objectif est atteint,
 * puis réarmée si le stock repasse sous 90 % de l'objectif. Enregistrés dans la sauvegarde (format versionné).
 */
public final class StockGoals{
    public static final int MAGIC = 0x4D434731; // "MCG1"
    public static final int VERSION = 1;
    /** Le stock doit repasser sous cette part de l'objectif pour que l'alerte se réarme. */
    public static final float REARM_FRACTION = 0.9f;

    private final LinkedHashMap<String, Integer> goals = new LinkedHashMap<>();
    private final Set<String> reached = new HashSet<>();

    public synchronized void set(String item, int amount){
        if(amount <= 0){
            goals.remove(item);
            reached.remove(item);
        }else{
            goals.put(item, amount);
            reached.remove(item);
        }
    }

    public synchronized int get(String item){
        return goals.getOrDefault(item, 0);
    }

    public synchronized boolean has(String item){
        return goals.containsKey(item);
    }

    public synchronized Map<String, Integer> all(){
        return new LinkedHashMap<>(goals);
    }

    /** Progression 0..1 vers l'objectif. */
    public synchronized float progress(String item, float stock){
        int g = get(item);
        return g <= 0 ? 0f : Math.min(1f, stock / g);
    }

    /** @return vrai au moment où l'objectif vient d'être atteint */
    public synchronized boolean check(String item, float stock){
        int g = get(item);
        if(g <= 0) return false;
        if(stock >= g && reached.add(item)) return true;
        if(stock < g * REARM_FRACTION) reached.remove(item);
        return false;
    }

    public synchronized void clear(){
        goals.clear();
        reached.clear();
    }

    public synchronized void write(DataOutput out) throws IOException{
        out.writeInt(MAGIC);
        out.writeInt(VERSION);
        out.writeInt(goals.size());
        for(var e : goals.entrySet()){
            out.writeUTF(e.getKey());
            out.writeInt(e.getValue());
            out.writeBoolean(reached.contains(e.getKey()));
        }
    }

    public synchronized void read(DataInput in) throws IOException{
        if(in.readInt() != MAGIC) throw new IOException("not MinClaude goals");
        int version = in.readInt();
        if(version < 1 || version > VERSION) throw new IOException("unsupported goals version " + version);
        int n = in.readInt();
        if(n < 0) throw new IOException("negative goal count");
        LinkedHashMap<String, Integer> g = new LinkedHashMap<>();
        Set<String> r = new HashSet<>();
        for(int i = 0; i < n; i++){
            String item = in.readUTF();
            g.put(item, in.readInt());
            if(in.readBoolean()) r.add(item);
        }
        goals.clear();
        goals.putAll(g);
        reached.clear();
        reached.addAll(r);
    }
}
