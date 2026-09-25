package com.arthurdiniz.arcanesurvivor.weapon;

import com.arthurdiniz.arcanesurvivor.model.Enemy;
import com.arthurdiniz.arcanesurvivor.model.Player;
import com.arthurdiniz.arcanesurvivor.model.Projectile;
import java.util.List;

public abstract class Weapon {
    public static final int MAX_LEVEL = 5;
    public final String name;
    public int level = 1;
    protected float clock;

    protected Weapon(String name) { this.name = name; }
    public abstract float cooldown();
    public abstract void fire(Player player, List<Enemy> enemies, List<Projectile> projectiles);

    public void update(float delta, Player player, List<Enemy> enemies, List<Projectile> projectiles) {
        clock += delta * player.attackSpeedMultiplier;
        if (clock >= cooldown()) { clock -= cooldown(); fire(player, enemies, projectiles); }
        clock = Math.min(clock, cooldown());
    }

    public boolean levelUp() {
        if (level >= MAX_LEVEL) return false;
        level++;
        return true;
    }

    protected Enemy nearest(Player player, List<Enemy> enemies, float range) {
        Enemy target = null;
        float closest = range * range;
        for (Enemy enemy : enemies) {
            float dx = enemy.x - player.x, dy = enemy.y - player.y, distance = dx * dx + dy * dy;
            if (enemy.health > 0 && distance < closest) { target = enemy; closest = distance; }
        }
        return target;
    }

    protected void shootFan(Player player, Enemy target, List<Projectile> projectiles,
                            float speed, float damage, int pierce) {
        if (target == null) return;
        float dx = target.x - player.x, dy = target.y - player.y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length == 0) return;
        float center = (float) Math.atan2(dy, dx);
        for (int i = 0; i < level; i++) {
            float offset = i == 0 ? 0 : ((i & 1) == 1 ? 1 : -1) * ((i + 1) / 2) * .16f;
            float angle = center + offset;
            projectiles.add(new Projectile(player.x, player.y,
                    (float) Math.cos(angle) * speed, (float) Math.sin(angle) * speed,
                    damage * player.damageMultiplier, 7, 1.8f, pierce, false));
        }
    }
}
