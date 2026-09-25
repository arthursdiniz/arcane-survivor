package com.arthurdiniz.arcanesurvivor.screen;

import com.arthurdiniz.arcanesurvivor.GameRules;
import com.arthurdiniz.arcanesurvivor.model.EnemyType;
import com.arthurdiniz.arcanesurvivor.model.Enemy;
import com.arthurdiniz.arcanesurvivor.model.Facing;
import com.arthurdiniz.arcanesurvivor.model.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import java.util.EnumMap;

/** Owns the illustrated textures and the small, reusable glow texture. */
final class ArtAssets implements AutoCloseable {
    private static final float TILE_SIZE = 512;
    // The upward pose starts above the sheet's regular row boundary. The right
    // pose has already ended here, so the staff belongs to the upward frame.
    private static final int RIGHT_UP_SPLIT_Y = 905;
    private static final Color STAFF_GLOW = new Color(.25f, .91f, 1f, .43f);
    private final Texture floor = load("art/ruins-floor.png");
    private final Texture characters = load("art/characters.png");
    private final Texture mageDirections = load("art/mage-directions.png");
    private final Texture necromancer = load("art/necromancer.png");
    private final Texture portrait = load("art/mage-portrait.png");
    private final Texture glow = createGlow();
    private final TextureRegion[] floorVariants = new TextureRegion[4];
    private final TextureRegion[] sprites = new TextureRegion[6];
    private final TextureRegion[][] playerFrames = new TextureRegion[4][4];
    private final EnumMap<Facing, Animation<TextureRegion>> walking = new EnumMap<>(Facing.class);

    ArtAssets() {
        for (int i = 0; i < 4; i++) {
            floorVariants[i] = new TextureRegion(floor);
            floorVariants[i].flip((i & 1) != 0, (i & 2) != 0);
        }
        for (int i = 0; i < sprites.length; i++)
            sprites[i] = new TextureRegion(characters, (i % 3) * 512, (i / 3) * 512, 512, 512);
        for (Facing direction : Facing.values()) {
            int row = direction.spriteRow;
            for (int column = 0; column < 4; column++) {
                int x = column * mageDirections.getWidth() / 4;
                int nominalStart = row * mageDirections.getHeight() / 4;
                int nominalEnd = (row + 1) * mageDirections.getHeight() / 4;
                int y = direction == Facing.UP ? RIGHT_UP_SPLIT_Y : nominalStart;
                int end = direction == Facing.RIGHT ? RIGHT_UP_SPLIT_Y : nominalEnd;
                int width = (column + 1) * mageDirections.getWidth() / 4 - x;
                int height = end - y;
                // The next row has a few stray staff pixels inside the left row's bottom edge.
                if (direction == Facing.LEFT) height -= 17;
                playerFrames[row][column] = new TextureRegion(mageDirections, x, y, width, height);
            }
            Animation<TextureRegion> walk = new Animation<>(.14f,
                    playerFrames[row][1], playerFrames[row][2], playerFrames[row][3], playerFrames[row][2]);
            walk.setPlayMode(Animation.PlayMode.LOOP);
            walking.put(direction, walk);
        }
    }

    private static Texture load(String path) {
        Texture texture = new Texture(Gdx.files.internal(path), true);
        texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        return texture;
    }

    private static Texture createGlow() {
        Pixmap pixels = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++) {
            float dx = (x - 31.5f) / 31.5f, dy = (y - 31.5f) / 31.5f;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            float alpha = distance >= 1 ? 0 : (float) Math.pow(1 - distance, 1.8);
            pixels.drawPixel(x, y, Color.rgba8888(1, 1, 1, alpha));
        }
        Texture texture = new Texture(pixels);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixels.dispose();
        return texture;
    }

    void floor(SpriteBatch batch, float cameraX, float cameraY) {
        int left = (int) Math.floor((cameraX - 700 + TILE_SIZE / 2) / TILE_SIZE);
        int right = (int) Math.floor((cameraX + 700 + TILE_SIZE / 2) / TILE_SIZE);
        int bottom = (int) Math.floor((cameraY - 420 + TILE_SIZE / 2) / TILE_SIZE);
        int top = (int) Math.floor((cameraY + 420 + TILE_SIZE / 2) / TILE_SIZE);
        for (int tx = left; tx <= right; tx++) for (int ty = bottom; ty <= top; ty++) {
            float x = tx * TILE_SIZE - TILE_SIZE / 2, y = ty * TILE_SIZE - TILE_SIZE / 2;
            if (x >= GameRules.ARENA_HALF || y >= GameRules.ARENA_HALF ||
                    x + TILE_SIZE <= -GameRules.ARENA_HALF || y + TILE_SIZE <= -GameRules.ARENA_HALF) continue;
            batch.draw(floorVariants[Math.floorMod(tx, 2) + Math.floorMod(ty, 2) * 2], x, y, TILE_SIZE, TILE_SIZE);
        }
    }

    void sprite(SpriteBatch batch, Enemy enemy) {
        EnemyType type = enemy.type;
        float x = enemy.x, y = enemy.y;
        float phase = enemy.animationTime + enemy.id * .37f;
        if (type == EnemyType.FINAL_BOSS) {
            float bob = (float) Math.sin(phase * 2.7f) * 4;
            batch.setColor(enemy.flash > 0 ? Color.WHITE : Color.LIGHT_GRAY);
            batch.draw(necromancer, x - 85, y - 79 + bob, 85, 79, 170, 170, 1, 1,
                    (float) Math.sin(phase * 1.7f) * 1.5f, 0, 0, necromancer.getWidth(), necromancer.getHeight(), false, false);
        } else {
            int index = switch (type) {
                case SLIME -> 1; case BAT -> 2; case SKELETON -> 3;
                case TANK -> 4; case MINI_BOSS -> 5; default -> 0;
            };
            float size = switch (type) {
                case SLIME -> 60; case BAT -> 71; case SKELETON -> 68;
                case TANK -> 91; case MINI_BOSS -> 132; default -> 70;
            };
            float swing = (float) Math.sin(phase * (type == EnemyType.BAT ? 13 : type == EnemyType.SLIME ? 7 : 9));
            float activity = enemy.moving ? 1 : .35f;
            float bob = type == EnemyType.BAT ? swing * 4 : Math.abs(swing) * activity * 2;
            float stretchX = type == EnemyType.SLIME ? 1 + swing * .08f :
                    type == EnemyType.BAT ? 1 + swing * .16f : 1 + swing * .025f * activity;
            float stretchY = type == EnemyType.SLIME ? 1 - swing * .09f :
                    type == EnemyType.BAT ? 1 - swing * .08f : 1 - swing * .025f * activity;
            float rotation = type == EnemyType.SLIME || type == EnemyType.BAT ? 0 : swing * activity *
                    (type == EnemyType.TANK || type == EnemyType.MINI_BOSS ? 2 : 4);
            float flip = enemy.facing == Facing.LEFT ? -1 : 1;
            batch.setColor(enemy.flash > 0 ? Color.WHITE : Color.LIGHT_GRAY);
            batch.draw(sprites[index], x - size / 2, y - size * .43f + bob,
                    size / 2, size * .43f, size, size, stretchX * flip, stretchY, rotation);
        }
        batch.setColor(Color.WHITE);
    }

    void player(SpriteBatch batch, Player player) {
        float x = player.x, y = player.y;
        TextureRegion frame = player.moving ? walking.get(player.facing).getKeyFrame(player.animationTime) :
                playerFrames[player.facing.spriteRow][0];
        float pulse = 28 + (float) Math.sin(player.animationTime * (player.moving ? 9 : 3)) * 5;
        float staffOffset = player.facing == Facing.DOWN || player.facing == Facing.LEFT ? -22 : 22;
        glow(batch, x + staffOffset, y + 19, pulse, pulse, STAFF_GLOW);
        batch.setColor(player.invulnerability > 0 && (int) (player.invulnerability * 16) % 2 == 0 ? .55f : 1,
                1, 1, 1);
        float bob = player.moving ? (float) Math.abs(Math.sin(player.animationTime * 11)) * 2.5f :
                (float) Math.sin(player.animationTime * 2.8f) * .8f;
        float breath = player.moving ? 1 : 1 + (float) Math.sin(player.animationTime * 2.8f) * .025f;
        float lean = player.moving ? (float) Math.sin(player.animationTime * 11) * 1.5f : 0;
        float scale = 88f / (mageDirections.getHeight() / 4f);
        int nominalEnd = (player.facing.spriteRow + 1) * mageDirections.getHeight() / 4;
        int frameEnd = frame.getRegionY() + frame.getRegionHeight();
        float drawY = y - 39 + bob + (nominalEnd - frameEnd) * scale;
        float drawHeight = frame.getRegionHeight() * scale;
        batch.draw(frame, x - 44, drawY,
                44, y + bob - drawY, 88, drawHeight, 1, breath, lean);
        batch.setColor(Color.WHITE);
    }

    void glow(SpriteBatch batch, float x, float y, float width, float height, Color tint) {
        batch.setColor(tint);
        batch.draw(glow, x - width / 2, y - height / 2, width, height);
        batch.setColor(Color.WHITE);
    }

    void portrait(SpriteBatch batch, float x, float y, float width, float height) {
        batch.draw(portrait, x, y, width, height);
    }

    @Override public void close() {
        floor.dispose(); characters.dispose(); mageDirections.dispose(); necromancer.dispose(); portrait.dispose(); glow.dispose();
    }
}
