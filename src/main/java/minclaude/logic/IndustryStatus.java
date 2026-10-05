package minclaude.logic;

/** État d'une usine, plus précis que le BlockStatus du jeu (qui ne distingue pas le manque d'énergie). */
public enum IndustryStatus{
    ACTIVE,
    /** Il manque une ressource (ou un liquide) en entrée. */
    NO_INPUT,
    /** Les entrées sont là mais le réseau électrique ne fournit rien. */
    NO_POWER,
    /** La sortie est pleine : rien n'évacue la production. */
    NO_OUTPUT,
    /** Désactivée par un processeur logique. */
    DISABLED;

    /**
     * @param enabled           le bloc n'est pas désactivé par la logique
     * @param shouldConsume     faux quand la sortie est pleine
     * @param efficiency        efficacité actuelle (0 = arrêtée)
     * @param usesPower         le bloc consomme de l'énergie
     * @param powerSatisfaction part de l'énergie demandée qui est fournie (0..1)
     * @param inputsMissing     au moins une ressource d'entrée manque
     */
    public static IndustryStatus classify(boolean enabled, boolean shouldConsume, float efficiency, boolean usesPower,
                                          float powerSatisfaction, boolean inputsMissing){
        if(!enabled) return DISABLED;
        if(!shouldConsume) return NO_OUTPUT;
        if(efficiency > 0f) return ACTIVE;
        if(inputsMissing) return NO_INPUT;
        if(usesPower && powerSatisfaction <= 0f) return NO_POWER;
        return NO_INPUT;
    }

    public boolean blocked(){
        return this != ACTIVE && this != DISABLED;
    }
}
