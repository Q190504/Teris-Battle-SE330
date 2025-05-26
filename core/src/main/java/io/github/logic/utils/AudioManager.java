package io.github.logic.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import io.github.logic.utils.AudioSettings;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    public enum AudioCategory {
        MENU_MUSIC,
        GAME_MUSIC,
        UI_SFX,
        GAMEPLAY_SFX,
        SKILL_SFX
    }

    public static class AudioConfig {
        public float volume;
        public boolean muted;

        public AudioConfig(float volume, boolean muted) {
            this.volume = volume;
            this.muted = muted;
        }
    }

    private static AudioManager instance;

    private Map<String, Music> musicMap;
    private Map<String, Sound> soundMap;
    private Map<String, AudioCategory> musicCategoryMap;
    private Map<String, AudioCategory> soundCategoryMap;
    private EnumMap<AudioCategory, AudioConfig> configMap;

    private Music currentMusic;
    private String currentMusicKey;

    private AudioManager() {
        musicMap = new HashMap<>();
        soundMap = new HashMap<>();
        musicCategoryMap = new HashMap<>();
        soundCategoryMap = new HashMap<>();

        // Load config from separate file
        configMap = AudioSettings.getDefaultConfig();
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public void loadMusic(String key, String filePath, AudioCategory category) {
        FileHandle file = Gdx.files.internal(filePath);
        if (file.exists()) {
            Music music = Gdx.audio.newMusic(file);
            musicMap.put(key, music);
            musicCategoryMap.put(key, category);
        } else {
            Gdx.app.error("AudioManager", "Music file not found: " + filePath);
        }
    }

    public void loadSound(String key, String filePath, AudioCategory category) {
        FileHandle file = Gdx.files.internal(filePath);
        if (file.exists()) {
            Sound sound = Gdx.audio.newSound(file);
            soundMap.put(key, sound);
            soundCategoryMap.put(key, category);
        } else {
            Gdx.app.error("AudioManager", "Sound file not found: " + filePath);
        }
    }

    public void playMusic(String key, boolean loop) {
        AudioCategory category = musicCategoryMap.get(key);
        if (category == null) {
            Gdx.app.error("AudioManager", "Music category not found for key: " + key);
            return;
        }

        AudioConfig config = configMap.get(category);
        if (config == null || config.muted) return;

        Music music = musicMap.get(key);
        if (music != null) {
            if (currentMusic != null) currentMusic.stop();
            currentMusic = music;
            currentMusicKey = key;
            music.setLooping(loop);
            music.setVolume(config.volume);
            music.play();
        } else {
            Gdx.app.error("AudioManager", "Music key not found: " + key);
        }
    }

    public void playSound(String key) {
        AudioCategory category = soundCategoryMap.get(key);
        if (category == null) {
            Gdx.app.error("AudioManager", "Sound category not found for key: " + key);
            return;
        }

        AudioConfig config = configMap.get(category);
        if (config == null || config.muted) return;

        Sound sound = soundMap.get(key);
        if (sound != null) {
            sound.play(config.volume);
        } else {
            Gdx.app.error("AudioManager", "Sound key not found: " + key);
        }
    }

    public void stopMusic() {
        if (currentMusic != null) currentMusic.stop();
    }

    public void stopAllSounds() {
        for (Sound sound : soundMap.values()) {
            sound.stop();
        }
    }

    public void setCategoryMuted(AudioCategory category, boolean muted) {
        AudioConfig config = configMap.get(category);
        if (config != null) config.muted = muted;
    }

    public void setCategoryVolume(AudioCategory category, float volume) {
        AudioConfig config = configMap.get(category);
        if (config != null) config.volume = Math.max(0f, Math.min(1f, volume));
    }

    public boolean isMuted(AudioCategory category) {
        AudioConfig config = configMap.get(category);
        return config != null && config.muted;
    }

    public float getVolume(AudioCategory category) {
        AudioConfig config = configMap.get(category);
        return config != null ? config.volume : 1f;
    }

    public void preloadMenuMusic() {
        loadMusic("menu_bg", "audio/menu_background.mp3", AudioCategory.MENU_MUSIC);
    }

    public void preloadGameMusic() {
        loadMusic("game_bg", "audio/game_background.mp3", AudioCategory.GAME_MUSIC);
        loadMusic("victory", "audio/victory.mp3", AudioCategory.GAME_MUSIC);
        loadMusic("defeat", "audio/defeat.mp3", AudioCategory.GAME_MUSIC);
    }

    public void preloadGameplaySfx() {
        loadSound("piece_move", "audio/piece_move.mp3", AudioCategory.GAMEPLAY_SFX);
        loadSound("piece_rotate", "audio/piece_rotate.mp3", AudioCategory.GAMEPLAY_SFX);
        loadSound("piece_drop", "audio/piece_drop.mp3", AudioCategory.GAMEPLAY_SFX);
        loadSound("line_clear", "audio/line_clear.mp3", AudioCategory.GAMEPLAY_SFX);
    }

    public void preloadSkillSfx() {
        loadSound("skill_activate", "audio/skill_activate.mp3", AudioCategory.SKILL_SFX);
    }

    public void preloadUiSfx() {
        loadSound("button_click", "audio/button_click.mp3", AudioCategory.UI_SFX);
        loadSound("notification", "audio/notification.mp3", AudioCategory.UI_SFX);
        loadSound("warning", "audio/warning.mp3", AudioCategory.UI_SFX);
    }

    public void preloadAllAudio() {
        preloadMenuMusic();
        preloadGameMusic();
        preloadGameplaySfx();
        preloadSkillSfx();
        preloadUiSfx();
    }

    public void dispose() {
        for (Music music : musicMap.values()) {
            music.dispose();
        }
        for (Sound sound : soundMap.values()) {
            sound.dispose();
        }
        musicMap.clear();
        soundMap.clear();
        musicCategoryMap.clear();
        soundCategoryMap.clear();
        currentMusic = null;
        currentMusicKey = null;
    }
}
