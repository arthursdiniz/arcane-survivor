package com.arthurdiniz.arcanesurvivor.system;

import com.arthurdiniz.arcanesurvivor.GameRules;
import com.arthurdiniz.arcanesurvivor.model.*;
import com.arthurdiniz.arcanesurvivor.weapon.*;
import java.util.*;

public final class GameSession {
    public final Player player = new Player();
    public final List<Enemy> enemies = new ArrayList<>();
    public final List<Projectile> projectiles = new ArrayList<>();
    public final List<ExperienceGem> gems = new ArrayList<>();
    public final List<Weapon> weapons = new ArrayList<>();
    public final UpgradeManager upgrades;
    private final EnemySpawner spawner;
    public float elapsed, cameraX, cameraY, hitFlash;
    public int kills, bossesDefeated, attacks, bossAppearances;
    public boolean victory, fivefoldExperience, doubleSpawns;

    public GameSession() { this(new Random()); }
    public GameSession(Random random) {
        spawner = new EnemySpawner(random);
        upgrades = new UpgradeManager(random);
        weapons.add(new MagicBoltWeapon());
    }

    public void update(float delta, float horizontal, float vertical) {
        elapsed += delta;
        player.move(horizontal, vertical, delta);
        player.update(delta);
        hitFlash = Math.max(0, hitFlash - delta);
        cameraX += (player.x - cameraX) * Math.min(1, delta * 5);
        cameraY += (player.y - cameraY) * Math.min(1, delta * 5);
        int previousEnemyCount = enemies.size();
        spawner.update(delta, elapsed, player, enemies, doubleSpawns);
        for (int i = previousEnemyCount; i < enemies.size(); i++)
            if (enemies.get(i).type == EnemyType.MINI_BOSS || enemies.get(i).type == EnemyType.FINAL_BOSS) bossAppearances++;
        List<Enemy> summoned = new ArrayList<>();
        for (Enemy enemy : enemies) {
            enemy.update(delta, player.x, player.y);
            if (GameRules.overlaps(player.x, player.y, 17, enemy.x, enemy.y, enemy.radius) && player.takeDamage(enemy.damage)) hitFlash = .22f;
            if (enemy.type == EnemyType.FINAL_BOSS) bossAttacks(enemy, summoned);
        }
        enemies.addAll(summoned);
        int previousProjectileCount = projectiles.size();
        for (Weapon weapon : weapons) weapon.update(delta, player, enemies, projectiles);
        if (projectiles.size() > previousProjectileCount) attacks++;
        updateProjectiles(delta);
        for (Iterator<Enemy> it = enemies.iterator(); it.hasNext();) {
            Enemy enemy = it.next();
            if (enemy.health <= 0) {
                gems.add(new ExperienceGem(enemy.x, enemy.y, enemy.type.xp));
                kills++;
                if (enemy.type == EnemyType.MINI_BOSS || enemy.type == EnemyType.FINAL_BOSS) bossesDefeated++;
                if (enemy.type == EnemyType.FINAL_BOSS) victory = true;
                it.remove();
            }
        }
        gems.removeIf(gem -> gem.update(player, delta, fivefoldExperience ? 5 : 1));
    }

    public void toggleFivefoldExperience() { fivefoldExperience = !fivefoldExperience; }
    public void toggleDoubleSpawns() { doubleSpawns = !doubleSpawns; }
    public void grantLevelNow() { player.grantLevel(); }
    public boolean summonFinalBoss() {
        if (!spawner.spawnFinalBossNow(player, enemies, elapsed)) return false;
        bossAppearances++;
        return true;
    }

    private void bossAttacks(Enemy boss, List<Enemy> summoned) {
        if (boss.attackClock >= 3.3f) {
            boss.attackClock = 0;
            for (int i = 0; i < 12; i++) {
                float angle = (float) (i * Math.PI * 2 / 12 + elapsed * .4);
                projectiles.add(new Projectile(boss.x, boss.y, (float) Math.cos(angle) * 210,
                        (float) Math.sin(angle) * 210, 14, 10, 3.7f, 1, true));
            }
        }
        if (boss.summonClock >= 7f) {
            boss.summonClock = 0;
            if (enemies.size() + summoned.size() < GameRules.MAX_ENEMIES - 4) for (int i = 0; i < 4; i++) {
                float angle = (float) (i * Math.PI / 2);
                summoned.add(new Enemy(EnemyType.SKELETON, boss.x + (float) Math.cos(angle) * 75,
                        boss.y + (float) Math.sin(angle) * 75, Difficulty.healthScale(elapsed)));
            }
        }
    }

    private void updateProjectiles(float delta) {
        for (Iterator<Projectile> it = projectiles.iterator(); it.hasNext();) {
            Projectile projectile = it.next();
            projectile.update(delta);
            boolean remove = projectile.life <= 0 || Math.abs(projectile.x - player.x) > 1000 || Math.abs(projectile.y - player.y) > 800;
            if (projectile.hostile) {
                if (GameRules.overlaps(projectile.x, projectile.y, projectile.radius, player.x, player.y, 17)) {
                    if (player.takeDamage(projectile.damage)) hitFlash = .22f;
                    remove = true;
                }
            } else for (Enemy enemy : enemies) {
                if (enemy.health > 0 && !projectile.hitIds.contains(enemy.id) &&
                        GameRules.overlaps(projectile.x, projectile.y, projectile.radius, enemy.x, enemy.y, enemy.radius)) {
                    enemy.hit(projectile.damage);
                    projectile.hitIds.add(enemy.id);
                    if (--projectile.pierce <= 0) { remove = true; break; }
                }
            }
            if (remove) it.remove();
        }
    }

    public void choose(UpgradeManager.Kind kind) {
        if (player.pendingLevels <= 0) return;
        UpgradeManager.apply(kind, player, weapons);
        player.pendingLevels--;
    }
}
