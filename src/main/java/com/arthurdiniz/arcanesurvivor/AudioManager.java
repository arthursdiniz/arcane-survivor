package com.arthurdiniz.arcanesurvivor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;
import java.util.Map;

/** Optional local audio files in assets/audio; missing files are silent. */
public final class AudioManager implements AutoCloseable {
    private final Map<String, Sound> effects = new HashMap<>();
    private Music music;
    public float master = .7f, musicVolume = .5f, effectsVolume = .7f;

    public AudioManager() {
        for (String name : new String[]{"attack", "kill", "level", "hurt", "boss"}) {
            String path = "audio/" + name + ".wav";
            if (Gdx.files.internal(path).exists()) effects.put(name, Gdx.audio.newSound(Gdx.files.internal(path)));
        }
        if (Gdx.files.internal("audio/music.ogg").exists()) {
            music = Gdx.audio.newMusic(Gdx.files.internal("audio/music.ogg"));
            music.setLooping(true);
            music.play();
        }
    }

    public void effect(String name) {
        Sound sound = effects.get(name);
        if (sound != null) sound.play(master * effectsVolume);
    }
    public void update() { if (music != null) music.setVolume(master * musicVolume); }
    @Override public void close() {
        effects.values().forEach(Sound::dispose);
        if (music != null) music.dispose();
    }
}
