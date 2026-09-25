package com.arthurdiniz.arcanesurvivor.model;

import com.arthurdiniz.arcanesurvivor.GameRules;

public final class Player {
    public float x, y;
    public float maxHealth = 100, health = 100, speed = 235, damageMultiplier = 1, attackSpeedMultiplier = 1;
    public float pickupRadius = 105, armor = 0, invulnerability;
    public int level = 1, xp, xpToNext = 12, pendingLevels;
    public Facing facing = Facing.DOWN;
    public boolean moving;
    public float animationTime;

    public void move(float horizontal, float vertical, float delta) {
        Facing nextFacing = Facing.fromMovement(horizontal, vertical, facing);
        if (nextFacing != facing) animationTime = 0;
        facing = nextFacing;
        float length = (float) Math.sqrt(horizontal * horizontal + vertical * vertical);
        if (length > 1) { horizontal /= length; vertical /= length; }
        float oldX = x, oldY = y;
        x = GameRules.clamp(x + horizontal * speed * delta, -GameRules.ARENA_HALF + 16, GameRules.ARENA_HALF - 16);
        y = GameRules.clamp(y + vertical * speed * delta, -GameRules.ARENA_HALF + 16, GameRules.ARENA_HALF - 16);
        boolean nextMoving = Math.abs(x - oldX) > .0001f || Math.abs(y - oldY) > .0001f;
        if (nextMoving != moving) animationTime = 0;
        moving = nextMoving;
        animationTime += delta;
    }

    public void update(float delta) { invulnerability = Math.max(0, invulnerability - delta); }

    public boolean takeDamage(float amount) {
        if (invulnerability > 0 || health <= 0) return false;
        health = Math.max(0, health - Math.max(1, amount - armor));
        invulnerability = .55f;
        return true;
    }

    public void addExperience(int amount) {
        xp += Math.max(0, amount);
        while (xp >= xpToNext) {
            xp -= xpToNext;
            grantLevel();
        }
    }

    public void grantLevel() {
        level++;
        pendingLevels++;
        xpToNext = (int) Math.ceil(xpToNext * 1.22 + 5);
    }

    public void healAndIncreaseMax(float amount) {
        maxHealth += amount;
        health = Math.min(maxHealth, health + amount);
    }
}
