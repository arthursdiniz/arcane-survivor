package com.arthurdiniz.arcanesurvivor.weapon;

import com.arthurdiniz.arcanesurvivor.GameRules;
import com.arthurdiniz.arcanesurvivor.model.*;
import java.util.List;

public final class FireAuraWeapon extends Weapon {
    public FireAuraWeapon() { super("Fire Aura"); }
    public float cooldown() { return .8f * (1 - .07f * (level - 1)); }
    public float range() { return 94 + (level - 1) * 22; }
    public void fire(Player player, List<Enemy> enemies, List<Projectile> projectiles) {
        for (Enemy enemy : enemies) if (enemy.health > 0 && GameRules.overlaps(player.x, player.y, range(), enemy.x, enemy.y, enemy.radius))
            enemy.hit((11 + 5 * (level - 1)) * player.damageMultiplier);
    }
}
