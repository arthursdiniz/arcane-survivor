package com.arthurdiniz.arcanesurvivor.weapon;

import com.arthurdiniz.arcanesurvivor.model.*;
import java.util.List;

public final class PiercingArrowWeapon extends Weapon {
    public PiercingArrowWeapon() { super("Piercing Arrow"); }
    public float cooldown() { return 1.8f * (1 - .08f * (level - 1)); }
    public void fire(Player player, List<Enemy> enemies, List<Projectile> projectiles) {
        Enemy target = nearest(player, enemies, 620);
        shootFan(player, target, projectiles, 690, 25 * (1 + .28f * (level - 1)), 2 + level / 2);
    }
}
