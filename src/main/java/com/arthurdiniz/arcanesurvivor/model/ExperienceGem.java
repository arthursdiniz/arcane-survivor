package com.arthurdiniz.arcanesurvivor.model;

import com.arthurdiniz.arcanesurvivor.GameRules;

public final class ExperienceGem {
    public float x, y;
    public final int value;
    public ExperienceGem(float x, float y, int value) { this.x = x; this.y = y; this.value = value; }

    public boolean update(Player player, float delta, int experienceMultiplier) {
        float dx = player.x - x, dy = player.y - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance < player.pickupRadius && distance > 0) {
            float step = Math.min(distance, (200 + (player.pickupRadius - distance) * 4) * delta);
            x += dx / distance * step; y += dy / distance * step;
        }
        if (GameRules.overlaps(x, y, 7, player.x, player.y, 17)) {
            player.addExperience(value * experienceMultiplier);
            return true;
        }
        return false;
    }
}
