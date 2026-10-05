package minclaude.stats;

import java.util.Arrays;

/**
 * Compte exactement ce qui entre dans le noyau (accepté) et ce qui y est détruit faute de place (perdu),
 * par ressource. Les sorties se déduisent ensuite exactement du stock (voir {@link #reconcile}).
 */
public final class FlowCounter{
    private int[] in = new int[64], lost = new int[64];

    public synchronized void record(int id, int accepted, int destroyed){
        ensure(id);
        in[id] += Math.max(0, accepted);
        lost[id] += Math.max(0, destroyed);
    }

    public synchronized int takeIn(int id){
        if(id >= in.length) return 0;
        int v = in[id];
        in[id] = 0;
        return v;
    }

    public synchronized int takeLost(int id){
        if(id >= lost.length) return 0;
        int v = lost[id];
        lost[id] = 0;
        return v;
    }

    public synchronized void reset(){
        Arrays.fill(in, 0);
        Arrays.fill(lost, 0);
    }

    /** Résultat d'une seconde : entrées et sorties exactes. */
    public record Flows(float in, float out){}

    /**
     * Le stock évolue de {@code delta = entrées - sorties} (les pertes n'entrent jamais dans le stock).
     * Donc sorties = entrées - delta. Si le résultat est négatif, des objets sont arrivés par un chemin non compté
     * (ex. lancement de secteur) : on les ajoute aux entrées.
     */
    public static Flows reconcile(int countedIn, int delta){
        int out = countedIn - delta;
        if(out < 0) return new Flows(countedIn - out, 0f);
        return new Flows(countedIn, out);
    }

    private void ensure(int id){
        if(id < in.length) return;
        int n = Math.max(id + 1, in.length * 2);
        in = Arrays.copyOf(in, n);
        lost = Arrays.copyOf(lost, n);
    }
}
