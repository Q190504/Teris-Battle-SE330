package io.github.logic.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class AudioSettings {
    private static AudioSettings instance;
    private Preferences prefs;
    
    // Default values
    private static final float DEFAULT_MASTER_VOLUME = 1.0f;
    private static final float DEFAULT_MUSIC_VOLUME = 1.0f;
    private static final float DEFAULT_SFX_VOLUME = 1.0f;
    private static final boolean DEFAULT_MUTED = false;
    
    // Preference keys
    private static final String PREF_NAME = "audio_settings";
    private static final String KEY_MASTER_VOLUME = "master_volume";
    private static final String KEY_MUSIC_VOLUME = "music_volume";
    private static final String KEY_SFX_VOLUME = "sfx_volume";
    private static final String KEY_MASTER_MUTED = "master_muted";
    private static final String KEY_MUSIC_MUTED = "music_muted";
    private static final String KEY_SFX_MUTED = "sfx_muted";

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

    // Utility methods
    public void resetToDefaults() {
        prefs.clear();
        saveSettings();
    }

    // Save settings to disk
    public void saveSettings() {
        prefs.flush();
    }

    // Debug methods
    public void printCurrentSettings() {
        System.out.println("=== Audio Settings ===");
        System.out.println("Master: " + getMasterVolume() + " (muted: " + isMasterMuted() + ")");
        System.out.println("Music: " + getMusicVolume() + " (muted: " + isMusicMuted() + ")");
        System.out.println("SFX: " + getSfxVolume() + " (muted: " + isSfxMuted() + ")");
        System.out.println("=====================");
    }
}