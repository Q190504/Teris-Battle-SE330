package io.github.logic.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    public enum AudioType {
        MUSIC,
        SFX
    }

    public enum AudioCategory {
        MENU_MUSIC,
        GAME_MUSIC,
        UI_SFX,
        GAMEPLAY_SFX,
        SKILL_SFX
    }

    private static AudioManager instance;

    private Map<String, Music> musicMap;
    private Map<String, Sound> soundMap;
    private Map<String, AudioCategory> musicCategoryMap;
    private Map<String, AudioCategory> soundCategoryMap;

    private Music currentMusic;
    private String currentMusicKey;
    
    // Separate volume controls
    private float masterVolume = 1.0f;
    private float musicVolume = 1.0f;
    private float sfxVolume = 1.0f;
    
    // Separate mute controls
    private boolean masterMuted = false;
    private boolean musicMuted = false;
    private boolean sfxMuted = false;

    private AudioManager() {
        musicMap = new HashMap<>();
        soundMap = new HashMap<>();
        musicCategoryMap = new HashMap<>();
        soundCategoryMap = new HashMap<>();
        
        // Load settings from AudioSettings
        loadSettings();
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    private void loadSettings() {
        AudioSettings settings = AudioSettings.getInstance();
        
        masterVolume = settings.getMasterVolume();
        musicVolume = settings.getMusicVolume();
        sfxVolume = settings.getSfxVolume();
        
        masterMuted = settings.isMasterMuted();
        musicMuted = settings.isMusicMuted();
        sfxMuted = settings.isSfxMuted();
    }

    public void saveSettings() {
        AudioSettings settings = AudioSettings.getInstance();
        
        settings.setMasterVolume(masterVolume);
        settings.setMusicVolume(musicVolume);
        settings.setSfxVolume(sfxVolume);
        
        settings.setMasterMuted(masterMuted);
        settings.setMusicMuted(musicMuted);
        settings.setSfxMuted(sfxMuted);
        
        settings.saveSettings();
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
        if (masterMuted || musicMuted) return;
        
        AudioCategory category = musicCategoryMap.get(key);
        if (category == null) {
            Gdx.app.error("AudioManager", "Music category not found for key: " + key);
            return;
        }

        Music music = musicMap.get(key);
        if (music != null) {
            if (currentMusic != null) currentMusic.stop();
            currentMusic = music;
            currentMusicKey = key;
            music.setLooping(loop);
            music.setVolume(calculateMusicVolume(category));
            music.play();
        } else {
            Gdx.app.error("AudioManager", "Music key not found: " + key);
        }
    }

    public void playSound(String key) {
        playSound(key, 1.0f);
    }

    public void playSound(String key, float volumeModifier) {
        if (masterMuted || sfxMuted) return;
        
        AudioCategory category = soundCategoryMap.get(key);
        if (category == null) {
            Gdx.app.error("AudioManager", "Sound category not found for key: " + key);
            return;
        }

        Sound sound = soundMap.get(key);
        if (sound != null) {
            float finalVolume = calculateSfxVolume(category) * volumeModifier;
            sound.play(finalVolume);
        } else {
            Gdx.app.error("AudioManager", "Sound key not found: " + key);
        }
    }

    private float calculateMusicVolume(AudioCategory category) {
        float categoryVolume = AudioSettings.getInstance().getCategoryVolume(category);
        return masterVolume * musicVolume * categoryVolume;
    }

    private float calculateSfxVolume(AudioCategory category) {
        float categoryVolume = AudioSettings.getInstance().getCategoryVolume(category);
        return masterVolume * sfxVolume * categoryVolume;
    }

    public void updateCurrentMusicVolume() {
        if (currentMusic != null && currentMusicKey != null) {
            AudioCategory category = musicCategoryMap.get(currentMusicKey);
            if (category != null) {
                float newVolume = (masterMuted || musicMuted) ? 0 : calculateMusicVolume(category);
                currentMusic.setVolume(newVolume);
            }
        }
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
            currentMusicKey = null;
        }
    }

    public void pauseMusic() {
        if (currentMusic != null) currentMusic.pause();
    }

    public void resumeMusic() {
        if (currentMusic != null && !masterMuted && !musicMuted) {
            currentMusic.play();
        }
    }

    public void stopAllSounds() {
        for (Sound sound : soundMap.values()) {
            sound.stop();
        }
    }

    // Master volume controls
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0f, Math.min(1f, volume));
        updateCurrentMusicVolume();
        saveSettings();
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    public void setMasterMuted(boolean muted) {
        this.masterMuted = muted;
        updateCurrentMusicVolume();
        saveSettings();
    }

    public boolean isMasterMuted() {
        return masterMuted;
    }

    // Music volume controls
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0f, Math.min(1f, volume));
        updateCurrentMusicVolume();
        saveSettings();
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicMuted(boolean muted) {
        this.musicMuted = muted;
        updateCurrentMusicVolume();
        saveSettings();
    }

    public boolean isMusicMuted() {
        return musicMuted;
    }

    // SFX volume controls
    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0f, Math.min(1f, volume));
        saveSettings();
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxMuted(boolean muted) {
        this.sfxMuted = muted;
        saveSettings();
    }

    public boolean isSfxMuted() {
        return sfxMuted;
    }

    // Category-specific controls
    public void setCategoryVolume(AudioCategory category, float volume) {
        AudioSettings.getInstance().setCategoryVolume(category, volume);
        updateCurrentMusicVolume();
        saveSettings();
    }

    public float getCategoryVolume(AudioCategory category) {
        return AudioSettings.getInstance().getCategoryVolume(category);
    }

    public void setCategoryMuted(AudioCategory category, boolean muted) {
        AudioSettings.getInstance().setCategoryMuted(category, muted);
        updateCurrentMusicVolume();
        saveSettings();
    }

    public boolean isCategoryMuted(AudioCategory category) {
        return AudioSettings.getInstance().isCategoryMuted(category);
    }

    // Utility methods for quick sound effects
    public void playButtonClick() {
        playSound("button_click");
    }

    public void playNotification() {
        playSound("notification");
    }

    public void playWarning() {
        playSound("warning");
    }

    public void playPieceMove() {
        playSound("piece_move");
    }

    public void playPieceRotate() {
        playSound("piece_rotate");
    }

    public void playPieceDrop() {
        playSound("piece_drop");
    }

    public void playLineClear() {
        playSound("line_clear");
    }

    public void playSkillActivate() {
        playSound("skill_activate");
    }

    // Preload methods
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

    // Check if audio is effectively muted
    public boolean isEffectivelyMuted(AudioType type) {
        if (masterMuted) return true;
        
        switch (type) {
            case MUSIC:
                return musicMuted;
            case SFX:
                return sfxMuted;
            default:
                return false;
        }
    }

    public void dispose() {
        saveSettings();
        
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