package minclaude.stats;

/** Extrait d'une série prêt à afficher : valeurs de la plus ancienne à la plus récente, espacées de {@code stepSeconds}. */
public record SeriesView(float[] values, float stepSeconds){
    public static final SeriesView EMPTY = new SeriesView(new float[0], 1f);

    public boolean isEmpty(){
        return values.length == 0;
    }

    public float last(){
        return values.length == 0 ? 0f : values[values.length - 1];
    }

    public float min(){
        float m = Float.POSITIVE_INFINITY;
        for(float v : values) m = Math.min(m, v);
        return values.length == 0 ? 0f : m;
    }

    public float max(){
        float m = Float.NEGATIVE_INFINITY;
        for(float v : values) m = Math.max(m, v);
        return values.length == 0 ? 0f : m;
    }

    public float durationSeconds(){
        return values.length * stepSeconds;
    }
}
