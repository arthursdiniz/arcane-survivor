package com.arthurdiniz.arcanesurvivor.system;

import com.arthurdiniz.arcanesurvivor.model.EnemyType;
import java.util.Random;

public final class Difficulty {
    private Difficulty() {}
    public static float healthScale(float seconds) { return 1 + Math.min(1.5f, seconds / 600f); }
    public static float spawnInterval(float seconds) { return Math.max(.34f, .95f - seconds / 1400f); }
    public static EnemyType choose(float seconds, Random random) {
        float roll = random.nextFloat();
        if (seconds < 120) return EnemyType.SLIME;
        if (seconds < 240) return roll < .65f ? EnemyType.SLIME : EnemyType.BAT;
        if (seconds < 420) return roll < .35f ? EnemyType.SLIME : roll < .65f ? EnemyType.BAT : EnemyType.SKELETON;
        return roll < .2f ? EnemyType.SLIME : roll < .4f ? EnemyType.BAT : roll < .77f ? EnemyType.SKELETON : EnemyType.TANK;
    }
}
