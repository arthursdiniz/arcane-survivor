package com.arthurdiniz.arcanesurvivor.model;

import java.util.HashSet;
import java.util.Set;

public final class Projectile {
    public float x, y, vx, vy, damage, radius, life;
    public int pierce;
    public final boolean hostile;
    public final boolean piercingArrow;
    public final Set<Integer> hitIds = new HashSet<>();

    public Projectile(float x, float y, float vx, float vy, float damage, float radius, float life, int pierce, boolean hostile) {
        this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.damage = damage;
        this.radius = radius; this.life = life; this.pierce = pierce; this.hostile = hostile;
        this.piercingArrow = !hostile && pierce > 1;
    }

    public void update(float delta) { x += vx * delta; y += vy * delta; life -= delta; }
}
