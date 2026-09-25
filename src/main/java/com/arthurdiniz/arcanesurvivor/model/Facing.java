package com.arthurdiniz.arcanesurvivor.model;

public enum Facing {
    DOWN(0), LEFT(1), RIGHT(2), UP(3);

    public final int spriteRow;
    Facing(int spriteRow) { this.spriteRow = spriteRow; }

    public static Facing fromMovement(float horizontal, float vertical, Facing previous) {
        float x = Math.abs(horizontal), y = Math.abs(vertical);
        if (x < .001f && y < .001f) return previous;
        if (x == y) {
            if (previous == LEFT && horizontal < 0 || previous == RIGHT && horizontal > 0 ||
                    previous == UP && vertical > 0 || previous == DOWN && vertical < 0) return previous;
        }
        if (x >= y) return horizontal < 0 ? LEFT : RIGHT;
        return vertical < 0 ? DOWN : UP;
    }
}
