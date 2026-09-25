package com.arthurdiniz.arcanesurvivor.model;

import com.arthurdiniz.arcanesurvivor.GameRules;

public final class Enemy {
    private static int nextId;
    public final int id = ++nextId;
    public final EnemyType type;
    public float x, y, health, maxHealth, speed, damage, radius, flash;
    public float attackClock, summonClock;
    public Facing facing = Facing.DOWN;
    public boolean moving;
    public float animationTime;

    public Enemy(EnemyType type, float x, float y, float difficulty) {
        this.type = type; this.x = x; this.y = y;
        float scale = type == EnemyType.FINAL_BOSS || type == EnemyType.MINI_BOSS ? 1 : difficulty;
        health = maxHealth = type.health * scale;
        speed = type.speed * (1 + (scale - 1) * .2f);
        damage = type.damage * (1 + (scale - 1) * .35f);
        radius = type.radius;
    }

    public void update(float delta, float playerX, float playerY) {
        flash = Math.max(0, flash - delta);
        float dx = playerX - x, dy = playerY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        Facing nextFacing = Facing.fromMovement(dx, dy, facing);
        if (nextFacing != facing) animationTime = 0;
        facing = nextFacing;
        float oldX = x, oldY = y;
        if (distance > radius + 10) {
            float direction = type == EnemyType.FINAL_BOSS && distance < 230 ? -.45f : 1;
            x = GameRules.clamp(x + dx / distance * speed * delta * direction, -GameRules.ARENA_HALF, GameRules.ARENA_HALF);
            y = GameRules.clamp(y + dy / distance * speed * delta * direction, -GameRules.ARENA_HALF, GameRules.ARENA_HALF);
        }
        boolean nextMoving = Math.abs(x - oldX) > .0001f || Math.abs(y - oldY) > .0001f;
        if (nextMoving != moving) animationTime = 0;
        moving = nextMoving;
        animationTime += delta;
        attackClock += delta;
        summonClock += delta;
    }

    public boolean hit(float amount) {
        health -= amount;
        flash = .12f;
        return health <= 0;
    }
}
