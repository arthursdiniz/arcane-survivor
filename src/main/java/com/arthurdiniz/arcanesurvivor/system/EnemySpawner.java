package com.arthurdiniz.arcanesurvivor.system;

import com.arthurdiniz.arcanesurvivor.GameRules;
import com.arthurdiniz.arcanesurvivor.model.*;
import java.util.List;
import java.util.Random;

public final class EnemySpawner {
    private final Random random;
    private float clock;
    private int lastWave;
    private boolean miniSpawned, finalSpawned;
    public EnemySpawner(Random random) { this.random = random; }

    public void update(float delta, float elapsed, Player player, List<Enemy> enemies) {
        update(delta, elapsed, player, enemies, false);
    }

    public void update(float delta, float elapsed, Player player, List<Enemy> enemies, boolean doubleSpawns) {
        if (!miniSpawned && elapsed >= GameRules.MINI_BOSS_TIME) {
            spawn(EnemyType.MINI_BOSS, player, enemies, elapsed); miniSpawned = true;
        }
        if (elapsed >= GameRules.FINAL_BOSS_TIME) spawnFinalBossNow(player, enemies, elapsed);
        clock += delta;
        float interval = Difficulty.spawnInterval(elapsed);
        while (clock >= interval) {
            clock -= interval;
            spawnRegularGroup(doubleSpawns ? 2 : 1, player, enemies, elapsed);
        }
        int wave = (int) (elapsed / 45);
        if (wave > lastWave) {
            lastWave = wave;
            spawnRegularGroup(Math.min(8, 3 + wave) * (doubleSpawns ? 2 : 1), player, enemies, elapsed);
        }
    }

    private void spawnRegularGroup(int count, Player player, List<Enemy> enemies, float elapsed) {
        for (int i = 0; i < count && enemies.size() < GameRules.MAX_ENEMIES; i++)
            spawn(Difficulty.choose(elapsed, random), player, enemies, elapsed);
    }

    public boolean spawnFinalBossNow(Player player, List<Enemy> enemies, float elapsed) {
        if (finalSpawned) return false;
        spawn(EnemyType.FINAL_BOSS, player, enemies, elapsed);
        finalSpawned = true;
        return true;
    }

    public void spawn(EnemyType type, Player player, List<Enemy> enemies, float elapsed) {
        float x = 0, y = 0;
        boolean outsideView = false;
        for (int attempt = 0; attempt < 24; attempt++) {
            float angle = random.nextFloat() * (float) (Math.PI * 2);
            float distance = 800 + random.nextFloat() * 100;
            x = GameRules.clamp(player.x + (float) Math.cos(angle) * distance, -GameRules.ARENA_HALF + 40, GameRules.ARENA_HALF - 40);
            y = GameRules.clamp(player.y + (float) Math.sin(angle) * distance, -GameRules.ARENA_HALF + 40, GameRules.ARENA_HALF - 40);
            if (Math.abs(x - player.x) > 710 || Math.abs(y - player.y) > 410) {
                outsideView = true;
                break;
            }
        }
        if (!outsideView) {
            x = player.x + (player.x > 0 ? -780 : 780);
            y = player.y;
        }
        enemies.add(new Enemy(type, x, y, Difficulty.healthScale(elapsed)));
    }
}
