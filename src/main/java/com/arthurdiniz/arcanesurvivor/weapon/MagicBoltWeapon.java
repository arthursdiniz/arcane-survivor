package com.arthurdiniz.arcanesurvivor.weapon;

import com.arthurdiniz.arcanesurvivor.model.*;
import java.util.List;

public final class MagicBoltWeapon extends Weapon {
    public MagicBoltWeapon() { super("Magic Bolt"); }
    public float cooldown() { return 1f * (1 - .09f * (level - 1)); }
    public void fire(Player player, List<Enemy> enemies, List<Projectile> projectiles) {
        Enemy target = nearest(player, enemies, 500);
        shootFan(player, target, projectiles, 520, 28 * (1 + .3f * (level - 1)), 1);
    }
}
