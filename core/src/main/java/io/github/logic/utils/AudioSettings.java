package io.github.logic.utils;

import io.github.logic.utils.AudioManager.AudioCategory;
import io.github.logic.utils.AudioManager.AudioConfig;

import java.util.EnumMap;

public class AudioSettings {
    public static EnumMap<AudioCategory, AudioConfig> getDefaultConfig() {
        EnumMap<AudioCategory, AudioConfig> config = new EnumMap<>(AudioCategory.class);

        config.put(AudioCategory.MENU_MUSIC, new AudioConfig(0.6f, false));
        config.put(AudioCategory.GAME_MUSIC, new AudioConfig(0.7f, false));
        config.put(AudioCategory.UI_SFX, new AudioConfig(1.0f, false));
        config.put(AudioCategory.GAMEPLAY_SFX, new AudioConfig(0.8f, false));
        config.put(AudioCategory.SKILL_SFX, new AudioConfig(1.0f, false));

        return config;
    }
}
