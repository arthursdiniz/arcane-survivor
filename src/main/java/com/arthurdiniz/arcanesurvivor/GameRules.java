package com.arthurdiniz.arcanesurvivor;

public final class GameRules {
    public static final float ARENA_HALF = 2100f;
    public static final float VIEW_WIDTH = 1280f;
    public static final float VIEW_HEIGHT = 720f;
    public static final float MINI_BOSS_TIME = 360f;
    public static final float FINAL_BOSS_TIME = 720f;
    public static final int MAX_ENEMIES = 320;
    private GameRules() {}

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static boolean overlaps(float ax, float ay, float ar, float bx, float by, float br) {
        float dx = ax - bx, dy = ay - by, radius = ar + br;
        return dx * dx + dy * dy <= radius * radius;
    }
}
