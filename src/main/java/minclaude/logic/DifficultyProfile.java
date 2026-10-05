package minclaude.logic;

/**
 * Multiplicateurs appliqués à l'équipe des vagues au début d'une nouvelle partie, selon la difficulté (1 Facile … 4 Brutal).
 * Ils s'ajoutent à ceux du jeu (règles de la carte ou du secteur).
 */
public record DifficultyProfile(float unitHealth, float unitDamage){
    public static DifficultyProfile forDifficulty(int difficulty){
        return switch(Math.max(1, Math.min(4, difficulty))){
            case 1 -> new DifficultyProfile(0.75f, 0.8f);
            case 2 -> new DifficultyProfile(1f, 1f);
            case 3 -> new DifficultyProfile(1.3f, 1.15f);
            default -> new DifficultyProfile(1.7f, 1.35f);
        };
    }
}
