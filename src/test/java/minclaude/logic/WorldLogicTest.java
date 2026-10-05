package minclaude.logic;

import minclaude.logic.TargetScorer.Kind;
import minclaude.logic.TargetScorer.Profile;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WorldLogicTest{
    private static final Profile NORMAL = Profile.forDifficulty(2, true);

    @Test
    void easyOrDisabledMeansVanilla(){
        assertFalse(Profile.forDifficulty(1, true).enabled());
        assertFalse(Profile.forDifficulty(4, false).enabled());
        assertFalse(TargetScorer.inSeekRange(Profile.forDifficulty(1, true), 1f));
        assertTrue(Profile.forDifficulty(4, true).seekTiles() > NORMAL.seekTiles());
    }

    @Test
    void weakPointsBeatWalls(){
        float gen = TargetScorer.score(NORMAL, Kind.GENERATOR, 6, 1f, false);
        float wall = TargetScorer.score(NORMAL, Kind.WALL, 2, 1f, true);
        assertTrue(gen > wall, "un générateur un peu plus loin passe avant un mur à portée");
        assertTrue(TargetScorer.score(NORMAL, Kind.TURRET_NO_AMMO, 5, 1f, false) > TargetScorer.score(NORMAL, Kind.TURRET, 5, 1f, false));
    }

    @Test
    void distanceAndHealthMatter(){
        assertTrue(TargetScorer.score(NORMAL, Kind.CONVEYOR, 2, 1f, false) > TargetScorer.score(NORMAL, Kind.CONVEYOR, 8, 1f, false));
        assertTrue(TargetScorer.score(NORMAL, Kind.TURRET, 5, 0.1f, false) > TargetScorer.score(NORMAL, Kind.TURRET, 5, 1f, false));
        assertTrue(TargetScorer.inSeekRange(NORMAL, NORMAL.seekTiles()));
        assertFalse(TargetScorer.inSeekRange(NORMAL, NORMAL.seekTiles() + 0.1f));
    }

    @Test
    void oreScatterIsDeterministicAndSparse(){
        var specs = new OreScatter.OreSpec[]{new OreScatter.OreSpec("a", 0.85f, 12f), new OreScatter.OreSpec("b", 0.84f, 11f)};
        Set<Long> first = new HashSet<>(), second = new HashSet<>();
        int[] perOre = new int[2];
        int n1 = OreScatter.scatter(200, 200, 42L, specs, (x, y) -> true, (x, y, i) -> {
            first.add((long)x << 32 | y);
            perOre[i]++;
        });
        int n2 = OreScatter.scatter(200, 200, 42L, specs, (x, y) -> true, (x, y, i) -> second.add((long)x << 32 | y));
        assertEquals(first, second, "même graine = mêmes gisements");
        assertEquals(n1, n2);
        float coverage = n1 / 40000f;
        assertTrue(coverage > 0.005f && coverage < 0.12f, "couverture raisonnable : " + coverage);
        assertTrue(perOre[0] > 0 && perOre[1] > 0, "chaque minerai apparaît");
    }

    @Test
    void oreScatterRespectsEligibilityAndSeed(){
        var specs = new OreScatter.OreSpec[]{new OreScatter.OreSpec("a", 0.8f, 10f)};
        int[] placed = {0};
        OreScatter.scatter(100, 100, 1L, specs, (x, y) -> x < 50, (x, y, i) -> {
            assertTrue(x < 50);
            placed[0]++;
        });
        assertTrue(placed[0] > 0);
        Set<Long> a = new HashSet<>(), b = new HashSet<>();
        OreScatter.scatter(100, 100, 1L, specs, (x, y) -> true, (x, y, i) -> a.add((long)x << 32 | y));
        OreScatter.scatter(100, 100, 2L, specs, (x, y) -> true, (x, y, i) -> b.add((long)x << 32 | y));
        assertNotEquals(a, b, "graines différentes = gisements différents");
    }

    @Test
    void noiseStaysInRange(){
        for(int i = 0; i < 1000; i++){
            float v = OreScatter.noise(7L, i * 13, i * 7, 9f);
            assertTrue(v >= 0f && v <= 1f);
        }
    }

    @Test
    void harderMeansEarlierAndMoreEnemies(){
        var easy = EnemyWavePlan.forDifficulty(1);
        var brutal = EnemyWavePlan.forDifficulty(4);
        assertEquals(easy.size(), brutal.size());
        for(int i = 0; i < easy.size(); i++){
            assertTrue(brutal.get(i).begin() < easy.get(i).begin());
            assertTrue(brutal.get(i).max() > easy.get(i).max());
            assertTrue(brutal.get(i).scaling() < easy.get(i).scaling());
            assertTrue(brutal.get(i).begin() >= 0);
        }
        assertEquals(EnemyWavePlan.forDifficulty(4), EnemyWavePlan.forDifficulty(99), "valeur bornée");
    }
}
