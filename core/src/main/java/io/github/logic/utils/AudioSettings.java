package io.github.logic.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import io.github.logic.utils.AudioManager.AudioCategory;

import java.util.EnumMap;

public class AudioSettings {
    private static AudioSettings instance;
    private Preferences prefs;
    
    // Default values
    private static final float DEFAULT_MASTER_VOLUME = 1.0f;
    private static final float DEFAULT_MUSIC_VOLUME = 0.7f;
    private static final float DEFAULT_SFX_VOLUME = 0.8f;
    private static final boolean DEFAULT_MUTED = false;
    
    // Category default volumes
    private static final float DEFAULT_MENU_MUSIC_VOLUME = 0.6f;
    private static final float DEFAULT_GAME_MUSIC_VOLUME = 0.7f;
    private static final float DEFAULT_UI_SFX_VOLUME = 1.0f;
    private static final float DEFAULT_GAMEPLAY_SFX_VOLUME = 0.8f;
    private static final float DEFAULT_SKILL_SFX_VOLUME = 1.0f;
    
    // Preference keys
    private static final String PREF_NAME = "audio_settings";
    private static final String KEY_MASTER_VOLUME = "master_volume";
    private static final String KEY_MUSIC_VOLUME = "music_volume";
    private static final String KEY_SFX_VOLUME = "sfx_volume";
    private static final String KEY_MASTER_MUTED = "master_muted";
    private static final String KEY_MUSIC_MUTED = "music_muted";
    private static final String KEY_SFX_MUTED = "sfx_muted";
    
    // Category preference keys
    private static final String KEY_MENU_MUSIC_VOLUME = "menu_music_volume";
    private static final String KEY_GAME_MUSIC_VOLUME = "game_music_volume";
    private static final String KEY_UI_SFX_VOLUME = "ui_sfx_volume";
    private static final String KEY_GAMEPLAY_SFX_VOLUME = "gameplay_sfx_volume";
    private static final String KEY_SKILL_SFX_VOLUME = "skill_sfx_volume";
    
    private static final String KEY_MENU_MUSIC_MUTED = "menu_music_muted";
    private static final String KEY_GAME_MUSIC_MUTED = "game_music_muted";
    private static final String KEY_UI_SFX_MUTED = "ui_sfx_muted";
    private static final String KEY_GAMEPLAY_SFX_MUTED = "gameplay_sfx_muted";
    private static final String KEY_SKILL_SFX_MUTED = "skill_sfx_muted";

    private AudioSettings() {
        prefs = Gdx.app.getPreferences(PREF_NAME);
    }

    public static AudioSettings getInstance() {
        if (instance == null) {
            instance = new AudioSettings();
        }
        return instance;
    }

    // Master controls
    public float getMasterVolume() {
        return prefs.getFloat(KEY_MASTER_VOLUME, DEFAULT_MASTER_VOLUME);
    }

    public void setMasterVolume(float volume) {
        prefs.putFloat(KEY_MASTER_VOLUME, Math.max(0f, Math.min(1f, volume)));
    }

    public boolean isMasterMuted() {
        return prefs.getBoolean(KEY_MASTER_MUTED, DEFAULT_MUTED);
    }

    public void setMasterMuted(boolean muted) {
        prefs.putBoolean(KEY_MASTER_MUTED, muted);
    }

    // Music controls
    public float getMusicVolume() {
        return prefs.getFloat(KEY_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME);
    }

    public void setMusicVolume(float volume) {
        prefs.putFloat(KEY_MUSIC_VOLUME, Math.max(0f, Math.min(1f, volume)));
    }

    public boolean isMusicMuted() {
        return prefs.getBoolean(KEY_MUSIC_MUTED, DEFAULT_MUTED);
    }

    public void setMusicMuted(boolean muted) {
        prefs.putBoolean(KEY_MUSIC_MUTED, muted);
    }

    // SFX controls
    public float getSfxVolume() {
        return prefs.getFloat(KEY_SFX_VOLUME, DEFAULT_SFX_VOLUME);
    }

    public void setSfxVolume(float volume) {
        prefs.putFloat(KEY_SFX_VOLUME, Math.max(0f, Math.min(1f, volume)));
    }

    public boolean isSfxMuted() {
        return prefs.getBoolean(KEY_SFX_MUTED, DEFAULT_MUTED);
    }

    public void setSfxMuted(boolean muted) {
        prefs.putBoolean(KEY_SFX_MUTED, muted);
    }

    // Category-specific controls
    public float getCategoryVolume(AudioCategory category) {
        switch (category) {
            case MENU_MUSIC:
                return prefs.getFloat(KEY_MENU_MUSIC_VOLUME, DEFAULT_MENU_MUSIC_VOLUME);
            case GAME_MUSIC:
                return prefs.getFloat(KEY_GAME_MUSIC_VOLUME, DEFAULT_GAME_MUSIC_VOLUME);
            case UI_SFX:
                return prefs.getFloat(KEY_UI_SFX_VOLUME, DEFAULT_UI_SFX_VOLUME);
            case GAMEPLAY_SFX:
                return prefs.getFloat(KEY_GAMEPLAY_SFX_VOLUME, DEFAULT_GAMEPLAY_SFX_VOLUME);
            case SKILL_SFX:
                return prefs.getFloat(KEY_SKILL_SFX_VOLUME, DEFAULT_SKILL_SFX_VOLUME);
            default:
                return 1.0f;
        }
    }

    public void setCategoryVolume(AudioCategory category, float volume) {
        volume = Math.max(0f, Math.min(1f, volume));
        
        switch (category) {
            case MENU_MUSIC:
                prefs.putFloat(KEY_MENU_MUSIC_VOLUME, volume);
                break;
            case GAME_MUSIC:
                prefs.putFloat(KEY_GAME_MUSIC_VOLUME, volume);
                break;
            case UI_SFX:
                prefs.putFloat(KEY_UI_SFX_VOLUME, volume);
                break;
            case GAMEPLAY_SFX:
                prefs.putFloat(KEY_GAMEPLAY_SFX_VOLUME, volume);
                break;
            case SKILL_SFX:
                prefs.putFloat(KEY_SKILL_SFX_VOLUME, volume);
                break;
        }
    }

    public boolean isCategoryMuted(AudioCategory category) {
        switch (category) {
            case MENU_MUSIC:
                return prefs.getBoolean(KEY_MENU_MUSIC_MUTED, DEFAULT_MUTED);
            case GAME_MUSIC:
                return prefs.getBoolean(KEY_GAME_MUSIC_MUTED, DEFAULT_MUTED);
            case UI_SFX:
                return prefs.getBoolean(KEY_UI_SFX_MUTED, DEFAULT_MUTED);
            case GAMEPLAY_SFX:
                return prefs.getBoolean(KEY_GAMEPLAY_SFX_MUTED, DEFAULT_MUTED);
            case SKILL_SFX:
                return prefs.getBoolean(KEY_SKILL_SFX_MUTED, DEFAULT_MUTED);
            default:
                return false;
        }
    }

    public void setCategoryMuted(AudioCategory category, boolean muted) {
        switch (category) {
            case MENU_MUSIC:
                prefs.putBoolean(KEY_MENU_MUSIC_MUTED, muted);
                break;
            case GAME_MUSIC:
                prefs.putBoolean(KEY_GAME_MUSIC_MUTED, muted);
                break;
            case UI_SFX:
                prefs.putBoolean(KEY_UI_SFX_MUTED, muted);
                break;
            case GAMEPLAY_SFX:
                prefs.putBoolean(KEY_GAMEPLAY_SFX_MUTED, muted);
                break;
            case SKILL_SFX:
                prefs.putBoolean(KEY_SKILL_SFX_MUTED, muted);
                break;
        }
    }

    // Utility methods
    public void resetToDefaults() {
        prefs.clear();
        saveSettings();
    }

    public void resetCategoryToDefault(AudioCategory category) {
        switch (category) {
            case MENU_MUSIC:
                prefs.remove(KEY_MENU_MUSIC_VOLUME);
                prefs.remove(KEY_MENU_MUSIC_MUTED);
                break;
            case GAME_MUSIC:
                prefs.remove(KEY_GAME_MUSIC_VOLUME);
                prefs.remove(KEY_GAME_MUSIC_MUTED);
                break;
            case UI_SFX:
                prefs.remove(KEY_UI_SFX_VOLUME);
                prefs.remove(KEY_UI_SFX_MUTED);
                break;
            case GAMEPLAY_SFX:
                prefs.remove(KEY_GAMEPLAY_SFX_VOLUME);
                prefs.remove(KEY_GAMEPLAY_SFX_MUTED);
                break;
            case SKILL_SFX:
                prefs.remove(KEY_SKILL_SFX_VOLUME);
                prefs.remove(KEY_SKILL_SFX_MUTED);
                break;
        }
        saveSettings();
    }

    // Save settings to disk
    public void saveSettings() {
        prefs.flush();
    }

    // Get configuration for compatibility with old system
    @Deprecated
    public static EnumMap<AudioCategory, AudioConfig> getDefaultConfig() {
        EnumMap<AudioCategory, AudioConfig> config = new EnumMap<>(AudioCategory.class);
        AudioSettings settings = getInstance();

        config.put(AudioCategory.MENU_MUSIC, 
            new AudioConfig(settings.getCategoryVolume(AudioCategory.MENU_MUSIC), 
                           settings.isCategoryMuted(AudioCategory.MENU_MUSIC)));
        config.put(AudioCategory.GAME_MUSIC, 
            new AudioConfig(settings.getCategoryVolume(AudioCategory.GAME_MUSIC), 
                           settings.isCategoryMuted(AudioCategory.GAME_MUSIC)));
        config.put(AudioCategory.UI_SFX, 
            new AudioConfig(settings.getCategoryVolume(AudioCategory.UI_SFX), 
                           settings.isCategoryMuted(AudioCategory.UI_SFX)));
        config.put(AudioCategory.GAMEPLAY_SFX, 
            new AudioConfig(settings.getCategoryVolume(AudioCategory.GAMEPLAY_SFX), 
                           settings.isCategoryMuted(AudioCategory.GAMEPLAY_SFX)));
        config.put(AudioCategory.SKILL_SFX, 
            new AudioConfig(settings.getCategoryVolume(AudioCategory.SKILL_SFX), 
                           settings.isCategoryMuted(AudioCategory.SKILL_SFX)));

        return config;
    }

    // Backward compatibility class
    @Deprecated
    public static class AudioConfig {
        public float volume;
        public boolean muted;

        public AudioConfig(float volume, boolean muted) {
            this.volume = volume;
            this.muted = muted;
        }
    }

    // Debug methods
    public void printCurrentSettings() {
        System.out.println("=== Audio Settings ===");
        System.out.println("Master: " + getMasterVolume() + " (muted: " + isMasterMuted() + ")");
        System.out.println("Music: " + getMusicVolume() + " (muted: " + isMusicMuted() + ")");
        System.out.println("SFX: " + getSfxVolume() + " (muted: " + isSfxMuted() + ")");
        
        for (AudioCategory category : AudioCategory.values()) {
            System.out.println(category.name() + ": " + getCategoryVolume(category) + 
                             " (muted: " + isCategoryMuted(category) + ")");
        }
        System.out.println("=====================");
    }
}