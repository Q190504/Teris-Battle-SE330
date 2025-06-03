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

    private static AudioManager instance;

    private Map<String, Music> musicMap;
    private Map<String, Sound> soundMap;

    private Music currentMusic;
    private String currentMusicKey;
    
    // Volume controls
    private float masterVolume = 1.0f;
    private float musicVolume = 1.0f;
    private float sfxVolume = 1.0f;
    
    // Mute controls
    private boolean masterMuted = false;
    private boolean musicMuted = false;
    private boolean sfxMuted = false;

    private AudioManager() {
        musicMap = new HashMap<>();
        soundMap = new HashMap<>();
        
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

    public void loadMusic(String key, String filePath) {
        FileHandle file = Gdx.files.internal(filePath);
        if (file.exists()) {
            Music music = Gdx.audio.newMusic(file);
            musicMap.put(key, music);
        } else {
            Gdx.app.error("AudioManager", "Music file not found: " + filePath);
        }
    }

    public void loadSound(String key, String filePath) {
        FileHandle file = Gdx.files.internal(filePath);
        if (file.exists()) {
            Sound sound = Gdx.audio.newSound(file);
            soundMap.put(key, sound);
        } else {
            Gdx.app.error("AudioManager", "Sound file not found: " + filePath);
        }
    }

    public void playMusic(String key, boolean loop) {
        if (masterMuted || musicMuted) return;

        Music music = musicMap.get(key);
        if (music != null) {
            if (currentMusic != null) currentMusic.stop();
            currentMusic = music;
            currentMusicKey = key;
            music.setLooping(loop);
            music.setVolume(calculateMusicVolume());
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

        Sound sound = soundMap.get(key);
        if (sound != null) {
            float finalVolume = calculateSfxVolume() * volumeModifier;
            sound.play(finalVolume);
        } else {
            Gdx.app.error("AudioManager", "Sound key not found: " + key);
        }
    }

    private float calculateMusicVolume() {
        return masterVolume * musicVolume;
    }

    private float calculateSfxVolume() {
        return masterVolume * sfxVolume;
    }

    public void updateCurrentMusicVolume() {
        if (currentMusic != null) {
            float newVolume = (masterMuted || musicMuted) ? 0 : calculateMusicVolume();
            currentMusic.setVolume(newVolume);
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

    // Quick sound effect methods
    public void playButtonClick() {
        playSound("button_click");
    }

    public void playPieceMove() {
        playSound("piece_move");
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

    // Music control methods
    public void playMenuMusic() {
        playMusic("menu_bg", true);
    }

    public void playGameMusic() {
        playMusic("game_bg", true);
    }

    public void playVictoryMusic() {
        playMusic("victory", false);
    }

    public void playDefeatMusic() {
        playMusic("defeat", false);
    }

    // Preload methods
    public void preloadAllAudio() {
        // Load music files
        loadMusic("menu_bg", "audio/menu_background.mp3");
        loadMusic("game_bg", "audio/game_background.mp3");
        loadMusic("victory", "audio/victory.mp3");
        loadMusic("defeat", "audio/defeat.mp3");
        
        // Load sound effects (using one sound for piece move/rotate)
        loadSound("piece_move", "audio/piece_move.mp3");
        loadSound("piece_drop", "audio/piece_drop.mp3");
        loadSound("line_clear", "audio/line_clear.mp3");
        loadSound("skill_activate", "audio/skill_activate.mp3");
        loadSound("button_click", "audio/button_click.mp3");
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
        currentMusic = null;
        currentMusicKey = null;
    }
}