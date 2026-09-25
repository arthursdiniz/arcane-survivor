package com.arthurdiniz.arcanesurvivor;

import com.badlogic.gdx.Game;
import com.arthurdiniz.arcanesurvivor.screen.GameScreen;

public final class ArcaneSurvivorGame extends Game {
    @Override public void create() { setScreen(new GameScreen(this)); }
    @Override public void dispose() { if (getScreen() != null) getScreen().dispose(); }
}
