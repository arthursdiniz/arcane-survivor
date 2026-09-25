package com.arthurdiniz.arcanesurvivor.model;

public enum EnemyType {
    SLIME(26, 77, 7, 16, 3), BAT(19, 145, 6, 13, 4),
    SKELETON(52, 97, 12, 19, 7), TANK(145, 55, 17, 28, 16),
    MINI_BOSS(1000, 72, 24, 46, 100), FINAL_BOSS(3200, 82, 29, 55, 300);

    public final float health, speed, damage, radius;
    public final int xp;
    EnemyType(float health, float speed, float damage, float radius, int xp) {
        this.health = health; this.speed = speed; this.damage = damage; this.radius = radius; this.xp = xp;
    }
}
