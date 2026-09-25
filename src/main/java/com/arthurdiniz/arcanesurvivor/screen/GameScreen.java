package com.arthurdiniz.arcanesurvivor.screen;

import com.arthurdiniz.arcanesurvivor.*;
import com.arthurdiniz.arcanesurvivor.model.*;
import com.arthurdiniz.arcanesurvivor.system.*;
import com.arthurdiniz.arcanesurvivor.weapon.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.List;

public final class GameScreen implements Screen {
    private enum State { MENU, SETTINGS, PLAYING, PAUSED, LEVEL_UP, GAME_OVER, VICTORY }
    private final ArcaneSurvivorGame game;
    private final OrthographicCamera worldCamera = new OrthographicCamera();
    private final OrthographicCamera uiCamera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(GameRules.VIEW_WIDTH, GameRules.VIEW_HEIGHT, uiCamera);
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = createFont("font/Play-Regular.ttf");
    private final BitmapFont boldFont = createFont("font/Play-Bold.ttf");
    private final ArtAssets art = new ArtAssets();
    private final AudioManager audio = new AudioManager();
    private final Vector3 pointer = new Vector3();
    private GameSession session;
    private State state = State.MENU;
    private List<UpgradeManager.Kind> choices = List.of();
    private int previousKills, previousLevel, previousAttacks, previousBossAppearances;
    private float previousHealth;
    private boolean fullscreen;
    private float accumulator;

    private static final Color BACKGROUND = new Color(.055f, .085f, .12f, 1);
    private static final Color PANEL = new Color(.055f, .095f, .14f, .94f);
    private static final Color PANEL_EDGE = new Color(.29f, .46f, .50f, .90f);
    private static final Color CYAN = new Color(.35f, .9f, .94f, 1);
    private static final Color GOLD = new Color(1f, .78f, .36f, 1);
    private static final Color MUTED = new Color(.58f, .7f, .76f, 1);
    private static final Color MAP_TINT = new Color(.76f, .79f, .87f, 1);
    private static final Color GEM_GLOW = new Color(.42f, .96f, .18f, .78f);
    private static final Color GEM_CORE = new Color(.80f, 1f, .30f, 1);
    private static final Color BOLT_GLOW = new Color(.35f, .9f, .94f, .94f);
    private static final Color ARROW_GLOW = new Color(1f, .78f, .38f, .94f);
    private static final Color HOSTILE_GLOW = new Color(.9f, .26f, .84f, .94f);
    private static final Color ORB_GLOW = new Color(.76f, .46f, 1f, .9f);
    private static final Color BOLT_LIGHT = new Color(.28f, .75f, 1f, .40f);
    private static final Color ARROW_LIGHT = new Color(1f, .55f, .18f, .42f);
    private static final Color ORB_LIGHT = new Color(.65f, .35f, 1f, .38f);

    public GameScreen(ArcaneSurvivorGame game) {
        this.game = game;
        worldCamera.setToOrtho(false, GameRules.VIEW_WIDTH, GameRules.VIEW_HEIGHT);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    private static BitmapFont createFont(String path) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(path));
        FreeTypeFontGenerator.FreeTypeFontParameter parameters = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameters.size = 24;
        parameters.minFilter = Texture.TextureFilter.Linear;
        parameters.magFilter = Texture.TextureFilter.Linear;
        BitmapFont result = generator.generateFont(parameters);
        generator.dispose();
        return result;
    }

    private void newGame() {
        session = new GameSession();
        previousHealth = session.player.health;
        previousKills = previousLevel = previousAttacks = previousBossAppearances = 0;
        state = State.PLAYING;
    }

    @Override public void render(float delta) {
        readInput();
        if (state == State.PLAYING) {
            float x = (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT) ? 1 : 0)
                    - (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT) ? 1 : 0);
            float y = (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP) ? 1 : 0)
                    - (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN) ? 1 : 0);
            accumulator = Math.min(.25f, accumulator + Math.min(delta, .25f));
            while (accumulator >= 1f / 60f) {
                session.update(1f / 60f, x, y);
                accumulator -= 1f / 60f;
                if (session.player.health <= 0 || session.victory || session.player.pendingLevels > 0) break;
            }
            playFeedback();
            if (session.player.health <= 0) state = State.GAME_OVER;
            else if (session.victory) state = State.VICTORY;
            else if (session.player.pendingLevels > 0) openUpgrades();
        } else accumulator = 0;
        audio.update();
        Gdx.gl.glClearColor(BACKGROUND.r, BACKGROUND.g, BACKGROUND.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        if (session != null && state != State.MENU && state != State.SETTINGS) {
            drawWorld();
            drawHud();
        }
        drawOverlay();
    }

    private void readInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) toggleFullscreen();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (state == State.PLAYING) state = State.PAUSED;
            else if (state == State.PAUSED) state = State.PLAYING;
            else if (state == State.SETTINGS) state = State.MENU;
        }
        if (state == State.LEVEL_UP) for (int i = 0; i < choices.size(); i++)
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) selectUpgrade(i);
        if (state == State.PLAYING) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.L)) session.toggleFivefoldExperience();
            if (Gdx.input.isKeyJustPressed(Input.Keys.K)) session.toggleDoubleSpawns();
            if (Gdx.input.isKeyJustPressed(Input.Keys.J)) session.summonFinalBoss();
            if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                session.grantLevelNow();
                audio.effect("level");
                previousLevel = session.player.level;
                openUpgrades();
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (state == State.MENU) newGame();
            else if (state == State.PAUSED) state = State.PLAYING;
        }
        pointer.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(pointer);
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;
        float x = pointer.x, y = pointer.y;
        switch (state) {
            case MENU -> {
                if (inside(x, y, 490, 322, 300, 54)) newGame();
                else if (inside(x, y, 490, 249, 300, 54)) state = State.SETTINGS;
                else if (inside(x, y, 490, 176, 300, 54)) Gdx.app.exit();
            }
            case SETTINGS -> {
                if (inside(x, y, 490, 413, 300, 44)) audio.master = cycle(audio.master);
                else if (inside(x, y, 490, 352, 300, 44)) audio.musicVolume = cycle(audio.musicVolume);
                else if (inside(x, y, 490, 291, 300, 44)) audio.effectsVolume = cycle(audio.effectsVolume);
                else if (inside(x, y, 490, 230, 300, 44)) toggleFullscreen();
                else if (inside(x, y, 490, 151, 300, 44)) state = State.MENU;
            }
            case PAUSED -> {
                if (inside(x, y, 490, 342, 300, 54)) state = State.PLAYING;
                else if (inside(x, y, 490, 269, 300, 54)) newGame();
                else if (inside(x, y, 490, 196, 300, 54)) state = State.MENU;
            }
            case LEVEL_UP -> {
                for (int i = 0; i < choices.size(); i++)
                    if (inside(x, y, 345, 386 - i * 83, 590, 66)) selectUpgrade(i);
            }
            case GAME_OVER, VICTORY -> {
                if (inside(x, y, 490, 186, 300, 54)) newGame();
                else if (inside(x, y, 490, 117, 300, 54)) state = State.MENU;
            }
            default -> { }
        }
    }

    private void playFeedback() {
        if (session.player.health < previousHealth) audio.effect("hurt");
        if (session.kills > previousKills) audio.effect("kill");
        if (session.attacks > previousAttacks) audio.effect("attack");
        if (session.player.level > previousLevel && previousLevel > 0) audio.effect("level");
        if (session.bossAppearances > previousBossAppearances) audio.effect("boss");
        previousHealth = session.player.health;
        previousKills = session.kills;
        previousLevel = session.player.level;
        previousAttacks = session.attacks;
        previousBossAppearances = session.bossAppearances;
    }

    private void openUpgrades() {
        choices = session.upgrades.choices(session.weapons);
        state = State.LEVEL_UP;
    }
    private void selectUpgrade(int index) {
        if (index < 0 || index >= choices.size()) return;
        session.choose(choices.get(index));
        if (session.player.pendingLevels > 0) openUpgrades();
        else state = State.PLAYING;
    }
    private float cycle(float value) { return value >= .99f ? 0 : Math.min(1, value + .25f); }
    private boolean inside(float x, float y, float bx, float by, float width, float height) {
        return x >= bx && x <= bx + width && y >= by && y <= by + height;
    }
    private void toggleFullscreen() {
        fullscreen = !fullscreen;
        if (fullscreen) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        else Gdx.graphics.setWindowedMode(1280, 720);
    }

    private void drawWorld() {
        worldCamera.position.set(session.cameraX, session.cameraY, 0);
        worldCamera.update();
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        batch.setColor(MAP_TINT);
        art.floor(batch, session.cameraX, session.cameraY);
        batch.setColor(Color.WHITE);
        batch.end();
        enableBlend();
        shapes.setProjectionMatrix(worldCamera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0, .015f, .025f, .36f);
        for (Enemy enemy : session.enemies) shapes.ellipse(enemy.x - enemy.radius * 1.1f, enemy.y - enemy.radius * .8f,
                enemy.radius * 2.2f, enemy.radius * .9f, 18);
        shapes.ellipse(session.player.x - 24, session.player.y - 17, 48, 20, 18);
        for (Weapon weapon : session.weapons) {
            if (weapon instanceof FireAuraWeapon aura) {
                shapes.setColor(.90f, .27f, .09f, .10f);
                shapes.circle(session.player.x, session.player.y, aura.range(), 48);
                shapes.setColor(1f, .50f, .15f, .10f);
                shapes.circle(session.player.x, session.player.y, aura.range() * .75f, 48);
            }
        }
        shapes.end();
        batch.begin();
        for (ExperienceGem gem : session.gems) {
            art.glow(batch, gem.x, gem.y, 26, 26, GEM_GLOW);
            art.glow(batch, gem.x, gem.y, 11, 11, GEM_CORE);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (Projectile projectile : session.projectiles) {
            if (projectile.hostile) continue;
            Color light = projectile.piercingArrow ? ARROW_LIGHT : BOLT_LIGHT;
            Color tint = projectile.piercingArrow ? ARROW_GLOW : BOLT_GLOW;
            art.glow(batch, projectile.x - projectile.vx * .026f, projectile.y - projectile.vy * .026f,
                    projectile.radius * 5, projectile.radius * 5, light);
            art.glow(batch, projectile.x, projectile.y, projectile.radius * 9, projectile.radius * 9, light);
            art.glow(batch, projectile.x, projectile.y, projectile.radius * 3, projectile.radius * 3, tint);
        }
        for (Weapon weapon : session.weapons) if (weapon instanceof OrbitingOrbWeapon orb)
            for (int i = 0; i < orb.orbCount(); i++) {
                art.glow(batch, orb.orbX(session.player, i), orb.orbY(session.player, i), 88, 88, ORB_LIGHT);
                art.glow(batch, orb.orbX(session.player, i), orb.orbY(session.player, i), 34, 34, ORB_GLOW);
            }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        for (Projectile projectile : session.projectiles) {
            if (projectile.hostile)
                art.glow(batch, projectile.x, projectile.y, projectile.radius * 5, projectile.radius * 5, HOSTILE_GLOW);
            art.glow(batch, projectile.x, projectile.y, projectile.radius * 1.7f, projectile.radius * 1.7f, Color.WHITE);
        }
        for (Weapon weapon : session.weapons) if (weapon instanceof OrbitingOrbWeapon orb)
            for (int i = 0; i < orb.orbCount(); i++)
                art.glow(batch, orb.orbX(session.player, i), orb.orbY(session.player, i), 16, 16, Color.WHITE);
        for (Enemy enemy : session.enemies) art.sprite(batch, enemy);
        art.player(batch, session.player);
        batch.end();
        enableBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy enemy : session.enemies) if (enemy.type == EnemyType.MINI_BOSS || enemy.type == EnemyType.FINAL_BOSS) {
            float width = enemy.type == EnemyType.FINAL_BOSS ? 104 : 78;
            float y = enemy.y + enemy.radius + 23;
            shapes.setColor(.04f, .07f, .1f, .86f); shapes.rect(enemy.x - width / 2 - 2, y - 2, width + 4, 9);
            shapes.setColor(enemy.type == EnemyType.FINAL_BOSS ? .80f : 1f, .35f, .36f, 1);
            shapes.rect(enemy.x - width / 2, y, width * Math.max(0, enemy.health / enemy.maxHealth), 5);
        }
        if (session.hitFlash > 0) {
            shapes.setColor(1, .2f, .2f, session.hitFlash * .7f);
            shapes.rect(session.cameraX - 640, session.cameraY - 360, 1280, 720);
        }
        shapes.end();
    }

    private void drawHud() {
        shapes.setProjectionMatrix(uiCamera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        panel(20, 584, 350, 116);
        shapes.setColor(.10f, .20f, .27f, 1); shapes.circle(71, 642, 39, 36);
        shapes.setColor(.25f, .71f, .76f, 1); shapes.circle(71, 642, 34, 36);
        shapes.setColor(.06f, .12f, .18f, 1); shapes.circle(71, 642, 30, 36);
        track(115, 649, 236, 12, session.player.health / session.player.maxHealth, new Color(.97f, .32f, .40f, 1));
        track(115, 610, 236, 8, (float) session.player.xp / session.player.xpToNext, CYAN);

        panel(978, 620, 282, 80);
        panel(393, 659, 560, 41);
        Enemy boss = visibleBoss();
        if (boss == null) track(410, 668, 526, 8, Math.min(1, session.elapsed / GameRules.FINAL_BOSS_TIME), GOLD);
        else track(410, 668, 526, 8, Math.max(0, boss.health / boss.maxHealth), new Color(.83f, .36f, .76f, 1));

        for (int i = 0; i < session.weapons.size(); i++) {
            int x = 20 + i * 154;
            panel(x, 18, 146, 64);
            shapes.setColor(i == 0 ? CYAN : i == 1 ? new Color(.78f, .54f, 1f, 1) :
                    i == 2 ? new Color(1, .49f, .29f, 1) : GOLD);
            shapes.circle(x + 27, 50, 16, 24);
            shapes.setColor(.055f, .095f, .14f, 1); shapes.circle(x + 27, 50, 11, 24);
        }
        panel(816, 18, 444, 64);
        shapes.end();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        label("LV", 55, 656, CYAN, .8f);
        label(String.format("%02d", session.player.level), 53, 638, Color.WHITE, 1.15f);
        label("VITALS", 115, 682, MUTED, .69f);
        label((int) session.player.health + " / " + (int) session.player.maxHealth, 254, 682, Color.WHITE, .78f);
        label("EXPERIENCE", 115, 633, MUTED, .65f);
        label(session.player.xp + " / " + session.player.xpToNext, 283, 633, CYAN, .65f);

        label("SURVIVAL TIME", 996, 681, MUTED, .67f);
        label(time(session.elapsed), 1135, 679, Color.WHITE, 1.18f);
        label("WAVE " + (1 + (int) (session.elapsed / 45)) + "    KILLS " + session.kills + "    BOSSES " + session.bossesDefeated,
                996, 644, Color.WHITE, .72f);
        label(boss == null ? "FINAL BOSS ARRIVES AT 12:00" :
                boss.type == EnemyType.FINAL_BOSS ? "NECROMANCER" : "ELITE GUARDIAN", 412, 686,
                boss == null ? MUTED : GOLD, .75f);
        for (int i = 0; i < session.weapons.size(); i++) {
            Weapon weapon = session.weapons.get(i);
            int x = 20 + i * 154;
            label(weapon.name, x + 48, 58, Color.WHITE, .65f);
            label("LEVEL " + weapon.level, x + 48, 38, MUTED, .62f);
        }
        label("DEV  L XP x5: " + (session.fivefoldExperience ? "ON" : "OFF") +
                "   K ENEMIES x2: " + (session.doubleSpawns ? "ON" : "OFF"), 835, 57, MUTED, .62f);
        label(boss != null && boss.type == EnemyType.FINAL_BOSS ? "P +1 LEVEL   J BOSS: SUMMONED" :
                "P +1 LEVEL   J SUMMON BOSS", 835, 34, MUTED, .62f);
        label("ESC  PAUSE", 1148, 32, MUTED, .62f);
        batch.end();
    }

    private Enemy visibleBoss() {
        Enemy mini = null;
        for (Enemy enemy : session.enemies) {
            if (enemy.type == EnemyType.FINAL_BOSS) return enemy;
            if (enemy.type == EnemyType.MINI_BOSS) mini = enemy;
        }
        return mini;
    }

    private void panel(float x, float y, float width, float height) {
        shapes.setColor(0, .015f, .03f, .32f); shapes.rect(x + 4, y - 5, width, height);
        shapes.setColor(PANEL_EDGE); shapes.rect(x, y, width, height);
        shapes.setColor(PANEL); shapes.rect(x + 1, y + 1, width - 2, height - 2);
        shapes.setColor(.36f, .82f, .83f, .35f); shapes.rect(x + 12, y + height - 3, width - 24, 2);
    }

    private void track(float x, float y, float width, float height, float progress, Color fill) {
        shapes.setColor(.018f, .035f, .065f, 1); shapes.rect(x, y, width, height);
        shapes.setColor(fill); shapes.rect(x + 1, y + 1, (width - 2) * GameRules.clamp(progress, 0, 1), height - 2);
        shapes.setColor(1, 1, 1, .20f);
        shapes.rect(x + 1, y + height - 3, (width - 2) * GameRules.clamp(progress, 0, 1), 2);
    }

    private void drawOverlay() {
        if (state == State.PLAYING) return;
        if (state == State.MENU || state == State.SETTINGS) {
            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
            batch.setColor(.38f, .52f, .55f, 1);
            art.floor(batch, 640, 360);
            batch.setColor(Color.WHITE);
            batch.end();
        }
        enableBlend();
        shapes.setProjectionMatrix(uiCamera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0, .018f, .035f, state == State.MENU || state == State.SETTINGS ? .70f : .77f);
        shapes.rect(0, 0, 1280, 720);
        panel(300, state == State.LEVEL_UP ? 160 : 99, 680, state == State.LEVEL_UP ? 420 : 520);
        if (state == State.MENU) {
            button(490, 322, ""); button(490, 249, ""); button(490, 176, "");
        } else if (state == State.SETTINGS) {
            for (int i = 0; i < 4; i++) button(490, 413 - i * 61, "");
            button(490, 151, "");
        } else if (state == State.PAUSED) {
            button(490, 342, ""); button(490, 269, ""); button(490, 196, "");
        } else if (state == State.LEVEL_UP) {
            for (int i = 0; i < choices.size(); i++) panel(345, 386 - i * 83, 590, 66);
        } else {
            button(490, 186, ""); button(490, 117, "");
        }
        shapes.end();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        if (state == State.MENU) art.portrait(batch, 18, 120, 315, 428);
        switch (state) {
            case MENU -> {
                label("ARCANE SURVIVOR", 418, 543, CYAN, 2.1f);
                label("One mage. One arena. Twelve minutes to the final foe.", 430, 488, MUTED, .8f);
                label("PLAY", 608, 356, Color.WHITE, 1.1f);
                label("SETTINGS", 585, 283, Color.WHITE, 1.1f);
                label("EXIT", 610, 210, Color.WHITE, 1.1f);
                label("WASD / ARROWS  |  ESC PAUSE  |  F11 FULLSCREEN", 433, 133, MUTED, .72f);
            }
            case SETTINGS -> {
                label("SETTINGS", 552, 554, CYAN, 1.7f);
                label("MASTER VOLUME   " + percent(audio.master), 515, 441, Color.WHITE, .9f);
                label("MUSIC VOLUME    " + percent(audio.musicVolume), 515, 380, Color.WHITE, .9f);
                label("SFX VOLUME      " + percent(audio.effectsVolume), 515, 319, Color.WHITE, .9f);
                label("FULLSCREEN      " + (fullscreen ? "ON" : "OFF"), 515, 258, Color.WHITE, .9f);
                label("BACK", 611, 179, Color.WHITE, 1);
                label("Click a setting to change it", 533, 121, MUTED, .75f);
            }
            case PAUSED -> {
                label("PAUSED", 565, 525, CYAN, 2);
                label("RESUME", 596, 376, Color.WHITE, 1);
                label("RESTART", 590, 303, Color.WHITE, 1);
                label("MAIN MENU", 571, 230, Color.WHITE, 1);
            }
            case LEVEL_UP -> {
                label("LEVEL " + session.player.level + "   -   CHOOSE AN UPGRADE", 410, 532, GOLD, 1.4f);
                for (int i = 0; i < choices.size(); i++)
                    label((i + 1) + "   " + UpgradeManager.title(choices.get(i), session.weapons), 368, 427 - i * 83, Color.WHITE, .97f);
                label("Click a card or press 1, 2, 3", 510, 192, MUTED, .75f);
            }
            case GAME_OVER, VICTORY -> {
                label(state == State.VICTORY ? "VICTORY" : "GAME OVER", state == State.VICTORY ? 554 : 535, 546,
                        state == State.VICTORY ? GOLD : Color.SCARLET, 2);
                label("Time survived     " + time(session.elapsed), 505, 449, Color.WHITE, 1);
                label("Level             " + session.player.level, 505, 409, Color.WHITE, 1);
                label("Enemies defeated  " + session.kills, 505, 369, Color.WHITE, 1);
                label("Bosses defeated   " + session.bossesDefeated, 505, 329, Color.WHITE, 1);
                label("PLAY AGAIN", 579, 220, Color.WHITE, 1);
                label("MAIN MENU", 582, 151, Color.WHITE, 1);
            }
            default -> { }
        }
        batch.end();
    }

    private void button(float x, float y, String ignored) {
        float height = y == 151 || y == 230 || y == 291 || y == 352 || y == 413 ? 44 : 54;
        boolean hovered = inside(pointer.x, pointer.y, x, y, 300, height);
        shapes.setColor(hovered ? .48f : .25f, hovered ? .81f : .47f, hovered ? .82f : .53f, 1);
        shapes.rect(x, y, 300, height);
        shapes.setColor(hovered ? .15f : .10f, hovered ? .30f : .21f, hovered ? .36f : .28f, 1);
        shapes.rect(x + 1, y + 1, 298, height - 2);
        shapes.setColor(hovered ? GOLD : CYAN);
        shapes.rect(x + 1, y + 1, 4, height - 2);
    }
    private String percent(float value) { return Math.round(value * 100) + "%"; }
    private String time(float seconds) { int whole = (int) seconds; return String.format("%02d:%02d", whole / 60, whole % 60); }
    private void enableBlend() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
    private void label(String text, float x, float y, Color color, float scale) {
        BitmapFont active = scale >= 1.1f ? boldFont : font;
        active.setColor(color); active.getData().setScale(scale); active.draw(batch, text, x, y);
    }
    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void show() { }
    @Override public void pause() { if (state == State.PLAYING) state = State.PAUSED; }
    @Override public void resume() { }
    @Override public void hide() { }
    @Override public void dispose() { audio.close(); art.close(); font.dispose(); boldFont.dispose(); batch.dispose(); shapes.dispose(); }
}
