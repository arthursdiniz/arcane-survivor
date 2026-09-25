package com.arthurdiniz.arcanesurvivor.weapon;

import com.arthurdiniz.arcanesurvivor.GameRules;
import com.arthurdiniz.arcanesurvivor.model.*;
import java.util.List;

public final class OrbitingOrbWeapon extends Weapon {
    public float angle;
    public OrbitingOrbWeapon() { super("Orbiting Orb"); }
    public float cooldown() { return .45f * (1 - .06f * (level - 1)); }
    public int orbCount() { return level; }
    public float orbX(Player player, int index) { return player.x + (float) Math.cos(angle + index * Math.PI * 2 / orbCount()) * 78; }
    public float orbY(Player player, int index) { return player.y + (float) Math.sin(angle + index * Math.PI * 2 / orbCount()) * 78; }
    @Override public void update(float delta, Player player, List<Enemy> enemies, List<Projectile> projectiles) {
        angle += delta * 2.7f;
        super.update(delta, player, enemies, projectiles);
    }
    public void fire(Player player, List<Enemy> enemies, List<Projectile> projectiles) {
        for (int i = 0; i < orbCount(); i++) for (Enemy enemy : enemies)
            if (enemy.health > 0 && GameRules.overlaps(orbX(player, i), orbY(player, i), 13, enemy.x, enemy.y, enemy.radius))
                enemy.hit((15 + 6 * (level - 1)) * player.damageMultiplier);
    }
}
