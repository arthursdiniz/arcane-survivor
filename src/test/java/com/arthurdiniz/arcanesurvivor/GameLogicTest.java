package com.arthurdiniz.arcanesurvivor;

import com.arthurdiniz.arcanesurvivor.model.*;
import com.arthurdiniz.arcanesurvivor.system.*;
import com.arthurdiniz.arcanesurvivor.weapon.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class GameLogicTest {
    @Test void movementIsNormalizedAndFrameIndependent() {
        Player straight = new Player(), diagonal = new Player(), split = new Player();
        straight.move(1, 0, 1);
        diagonal.move(1, 1, 1);
        split.move(1, 0, .5f); split.move(1, 0, .5f);
        assertEquals(straight.x, Math.hypot(diagonal.x, diagonal.y), .001);
        assertEquals(straight.x, split.x, .001);
    }

    @Test void playerKeepsFacingWhenIdleAndCyclesDirectionsWhileMoving() {
        Player player = new Player();
        assertEquals(Facing.DOWN, player.facing);
        player.move(0, 1, .2f);
        assertEquals(Facing.UP, player.facing);
        assertTrue(player.moving);
        assertEquals(.2f, player.animationTime, .0001f);
        player.move(0, 0, .1f);
        assertEquals(Facing.UP, player.facing);
        assertFalse(player.moving);
        assertEquals(.1f, player.animationTime, .0001f);
        player.move(-1, 0, .1f);
        assertEquals(Facing.LEFT, player.facing);
        player.move(1, 0, .1f);
        assertEquals(Facing.RIGHT, player.facing);
        player.move(0, -1, .1f);
        assertEquals(Facing.DOWN, player.facing);
    }

    @Test void enemiesAdvanceTheirAnimationOnlyWithSimulation() {
        Enemy enemy = new Enemy(EnemyType.SKELETON, 100, 0, 1);
        enemy.update(.25f, 0, 0);
        assertEquals(Facing.LEFT, enemy.facing);
        assertTrue(enemy.moving);
        assertEquals(.25f, enemy.animationTime, .0001f);
        enemy.update(.25f, enemy.x, enemy.y);
        assertFalse(enemy.moving);
        assertEquals(.25f, enemy.animationTime, .0001f);
    }

    @Test void experienceCanGrantMultipleLevelsAndKeepsRemainder() {
        Player player = new Player();
        player.addExperience(12);
        assertEquals(2, player.level);
        assertEquals(1, player.pendingLevels);
        assertEquals(0, player.xp);
        assertEquals(20, player.xpToNext);
        player.addExperience(55);
        assertEquals(4, player.level);
        assertEquals(3, player.pendingLevels);
        assertTrue(player.xp > 0);
    }

    @Test void fivefoldExperienceAffectsPickupOnlyWhileEnabled() {
        GameSession session = new GameSession(new Random(10));
        session.toggleFivefoldExperience();
        session.gems.add(new ExperienceGem(0, 0, 5));
        session.update(.016f, 0, 0);
        assertEquals(2, session.player.level);
        assertEquals(13, session.player.xp);
        session.toggleFivefoldExperience();
        session.gems.add(new ExperienceGem(0, 0, 5));
        session.update(.016f, 0, 0);
        assertEquals(18, session.player.xp);
    }

    @Test void instantLevelGrantsOneUpgradeWithoutChangingExperienceProgress() {
        GameSession session = new GameSession(new Random(15));
        session.player.addExperience(5);
        session.grantLevelNow();
        assertEquals(2, session.player.level);
        assertEquals(5, session.player.xp);
        assertEquals(20, session.player.xpToNext);
        assertEquals(1, session.player.pendingLevels);
        session.choose(UpgradeManager.Kind.MAGIC_BOLT);
        assertEquals(2, session.weapons.getFirst().level);
        assertEquals(0, session.player.pendingLevels);
    }

    @Test void everyWeaponLevelAddsOneShotOrOrbAndExpandsTheAura() {
        Player player = new Player();
        List<Enemy> targets = List.of(new Enemy(EnemyType.SLIME, 250, 0, 1));
        MagicBoltWeapon bolt = new MagicBoltWeapon();
        PiercingArrowWeapon arrow = new PiercingArrowWeapon();
        OrbitingOrbWeapon orb = new OrbitingOrbWeapon();
        FireAuraWeapon aura = new FireAuraWeapon();
        for (int level = 1; level <= Weapon.MAX_LEVEL; level++) {
            List<Projectile> bolts = new ArrayList<>(), arrows = new ArrayList<>();
            bolt.fire(player, targets, bolts);
            arrow.fire(player, targets, arrows);
            assertEquals(level, bolts.size());
            assertEquals(level, arrows.size());
            assertEquals(level, orb.orbCount());
            assertEquals(94 + (level - 1) * 22, aura.range());
            if (level > 1) {
                assertEquals(0, bolts.getFirst().vy, .001f);
                assertEquals(0, arrows.getFirst().vy, .001f);
                assertNotEquals(bolts.getFirst().vy, bolts.get(1).vy);
                assertNotEquals(arrows.getFirst().vy, arrows.get(1).vy);
            }
            bolt.levelUp(); arrow.levelUp(); orb.levelUp(); aura.levelUp();
        }
    }

    @Test void doubleSpawnsDoublesRegularEnemiesAndRespectsTheCap() {
        Player player = new Player();
        List<Enemy> normal = new ArrayList<>(), doubled = new ArrayList<>();
        new EnemySpawner(new Random(11)).update(1f, 0, player, normal, false);
        new EnemySpawner(new Random(11)).update(1f, 0, player, doubled, true);
        assertEquals(1, normal.size());
        assertEquals(2, doubled.size());
        EnemySpawner capped = new EnemySpawner(new Random(12));
        while (doubled.size() < GameRules.MAX_ENEMIES - 1)
            doubled.add(new Enemy(EnemyType.SLIME, 800, 0, 1));
        capped.update(1f, 0, player, doubled, true);
        assertEquals(GameRules.MAX_ENEMIES, doubled.size());
    }

    @Test void summonedFinalBossDoesNotRespawnAtTwelveMinutes() {
        GameSession session = new GameSession(new Random(13));
        assertTrue(session.summonFinalBoss());
        assertFalse(session.summonFinalBoss());
        assertEquals(1, session.bossAppearances);
        session.elapsed = GameRules.FINAL_BOSS_TIME;
        session.update(.016f, 0, 0);
        assertEquals(1, session.enemies.stream().filter(e -> e.type == EnemyType.FINAL_BOSS).count());
        assertEquals(2, session.bossAppearances);
    }

    @Test void armorAndInvulnerabilityPreventExcessDamage() {
        Player player = new Player(); player.armor = 3;
        assertTrue(player.takeDamage(8));
        assertEquals(95, player.health);
        assertFalse(player.takeDamage(8));
        player.update(.55f);
        assertTrue(player.takeDamage(1));
        assertEquals(94, player.health);
    }

    @Test void difficultyAndEnemyPoolProgressOverTime() {
        assertEquals(EnemyType.SLIME, Difficulty.choose(0, new Random(1)));
        assertTrue(Difficulty.healthScale(600) > Difficulty.healthScale(0));
        assertTrue(Difficulty.spawnInterval(600) < Difficulty.spawnInterval(0));
        for (int i = 0; i < 100; i++) assertNotEquals(EnemyType.TANK, Difficulty.choose(239, new Random(i)));
        assertTrue(Difficulty.spawnInterval(99999) >= .34f);
    }

    @Test void upgradeChoicesExcludeMaxedWeaponAndApplyingUpgradesWorks() {
        Player player = new Player();
        List<Weapon> weapons = new ArrayList<>();
        MagicBoltWeapon bolt = new MagicBoltWeapon(); weapons.add(bolt);
        float cooldown = bolt.cooldown();
        UpgradeManager.apply(UpgradeManager.Kind.MAGIC_BOLT, player, weapons);
        assertEquals(2, bolt.level);
        assertTrue(bolt.cooldown() < cooldown);
        UpgradeManager.apply(UpgradeManager.Kind.HEALTH, player, weapons);
        assertEquals(120, player.maxHealth);
        assertEquals(120, player.health);
        UpgradeManager.apply(UpgradeManager.Kind.FIRE_AURA, player, weapons);
        assertEquals(2, weapons.size());
        while (bolt.level < Weapon.MAX_LEVEL) bolt.levelUp();
        assertFalse(bolt.levelUp());
        UpgradeManager manager = new UpgradeManager(new Random(3));
        for (int i = 0; i < 50; i++) assertFalse(manager.choices(weapons).contains(UpgradeManager.Kind.MAGIC_BOLT));
    }

    @Test void spawningStaysAwayAndBossesAppearAtMilestones() {
        EnemySpawner spawner = new EnemySpawner(new Random(2));
        Player player = new Player();
        List<Enemy> enemies = new ArrayList<>();
        spawner.update(.1f, 0, player, enemies);
        spawner.update(.1f, GameRules.MINI_BOSS_TIME, player, enemies);
        assertTrue(enemies.stream().anyMatch(e -> e.type == EnemyType.MINI_BOSS));
        spawner.update(.1f, GameRules.FINAL_BOSS_TIME, player, enemies);
        assertTrue(enemies.stream().anyMatch(e -> e.type == EnemyType.FINAL_BOSS));
        for (Enemy e : enemies) assertTrue(Math.abs(e.x - player.x) > 640 || Math.abs(e.y - player.y) > 360);
        player.x = 2050; player.y = 2050;
        enemies.clear();
        spawner.spawn(EnemyType.SLIME, player, enemies, 0);
        assertTrue(Math.abs(enemies.getFirst().x - player.x) > 710 || Math.abs(enemies.getFirst().y - player.y) > 410);
    }

    @Test void timedWaveAddsAGroup() {
        EnemySpawner spawner = new EnemySpawner(new Random(5));
        List<Enemy> enemies = new ArrayList<>();
        spawner.update(.01f, 45, new Player(), enemies);
        assertTrue(enemies.size() >= 4);
    }

    @Test void projectileDamageAndBossVictoryResolveInSession() {
        GameSession session = new GameSession(new Random(4));
        Enemy boss = new Enemy(EnemyType.FINAL_BOSS, 200, 0, 1);
        session.enemies.add(boss);
        session.projectiles.add(new Projectile(200, 0, 0, 0, boss.health + 1, 8, 1, 1, false));
        session.update(.016f, 0, 0);
        assertTrue(session.victory);
        assertEquals(1, session.bossesDefeated);
        assertEquals(1, session.kills);
        assertEquals(1, session.gems.size());
    }

    @Test void finalBossUsesBothAttackPatternsWithoutMutatingEnemyIteration() {
        GameSession session = new GameSession(new Random(7));
        Enemy boss = new Enemy(EnemyType.FINAL_BOSS, 280, 0, 1);
        boss.attackClock = 3.3f;
        boss.summonClock = 7f;
        session.enemies.add(boss);
        session.update(.016f, 0, 0);
        assertEquals(12, session.projectiles.stream().filter(p -> p.hostile).count());
        assertEquals(4, session.enemies.stream().filter(e -> e.type == EnemyType.SKELETON).count());
    }

    @Test void runStopsAtZeroHealthAndLevelChoiceConsumesPendingLevel() {
        GameSession session = new GameSession(new Random(8));
        session.player.addExperience(12);
        session.choose(UpgradeManager.Kind.ARMOR);
        assertEquals(0, session.player.pendingLevels);
        assertEquals(1, session.player.armor);
        session.player.takeDamage(200);
        assertEquals(0, session.player.health);
    }
}
