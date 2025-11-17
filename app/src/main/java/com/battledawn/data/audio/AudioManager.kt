package com.battledawn.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Audio Manager for game sounds and music
 * Manages sound effects and background music playback
 */
@Singleton
class AudioManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var soundPool: SoundPool? = null
    private var musicPlayer: MediaPlayer? = null

    private val soundCache = mutableMapOf<SoundEffect, Int>()

    var soundEnabled = true
    var musicEnabled = true
    var soundVolume = 0.8f
    var musicVolume = 0.6f

    init {
        initializeSoundPool()
    }

    /**
     * Initialize SoundPool for sound effects
     */
    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        // Preload common sound effects
        // TODO: Add actual sound resource files to res/raw/
        // loadSound(SoundEffect.BUTTON_CLICK, R.raw.button_click)
        // loadSound(SoundEffect.BUILD_COMPLETE, R.raw.build_complete)
        // etc.

        Timber.d("AudioManager initialized")
    }

    /**
     * Load a sound effect
     */
    private fun loadSound(effect: SoundEffect, resourceId: Int) {
        soundPool?.let { pool ->
            val soundId = pool.load(context, resourceId, 1)
            soundCache[effect] = soundId
            Timber.d("Loaded sound: ${effect.name}")
        }
    }

    /**
     * Play a sound effect
     */
    fun playSound(effect: SoundEffect) {
        if (!soundEnabled) return

        soundPool?.let { pool ->
            soundCache[effect]?.let { soundId ->
                pool.play(
                    soundId,
                    soundVolume,
                    soundVolume,
                    1,
                    0,
                    1.0f
                )
                Timber.v("Playing sound: ${effect.name}")
            } ?: run {
                Timber.w("Sound not loaded: ${effect.name}")
            }
        }
    }

    /**
     * Play background music
     */
    fun playMusic(music: Music) {
        if (!musicEnabled) return

        try {
            stopMusic()

            // TODO: Add actual music resource files
            // musicPlayer = MediaPlayer.create(context, music.resourceId)
            musicPlayer?.apply {
                isLooping = true
                setVolume(musicVolume, musicVolume)
                start()
                Timber.d("Playing music: ${music.name}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to play music")
        }
    }

    /**
     * Stop background music
     */
    fun stopMusic() {
        musicPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
            musicPlayer = null
            Timber.d("Music stopped")
        }
    }

    /**
     * Pause music
     */
    fun pauseMusic() {
        musicPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                Timber.d("Music paused")
            }
        }
    }

    /**
     * Resume music
     */
    fun resumeMusic() {
        if (!musicEnabled) return

        musicPlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
                Timber.d("Music resumed")
            }
        }
    }

    /**
     * Set sound enabled/disabled
     */
    fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
        Timber.d("Sound ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Set music enabled/disabled
     */
    fun setMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        if (!enabled) {
            stopMusic()
        }
        Timber.d("Music ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Set sound volume (0.0 to 1.0)
     */
    fun setSoundVolume(volume: Float) {
        soundVolume = volume.coerceIn(0f, 1f)
        Timber.d("Sound volume: $soundVolume")
    }

    /**
     * Set music volume (0.0 to 1.0)
     */
    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        musicPlayer?.setVolume(musicVolume, musicVolume)
        Timber.d("Music volume: $musicVolume")
    }

    /**
     * Release resources
     */
    fun release() {
        stopMusic()
        soundPool?.release()
        soundPool = null
        soundCache.clear()
        Timber.d("AudioManager released")
    }
}

/**
 * Sound effects enum
 * TODO: Add corresponding audio files to res/raw/
 */
enum class SoundEffect {
    // UI Sounds
    BUTTON_CLICK,
    TAB_SWITCH,
    DIALOG_OPEN,
    DIALOG_CLOSE,
    ERROR,
    SUCCESS,

    // Game Sounds
    BUILD_START,
    BUILD_COMPLETE,
    UPGRADE_COMPLETE,
    RESEARCH_COMPLETE,
    UNIT_TRAINED,

    // Combat Sounds
    ATTACK_LAUNCH,
    BATTLE_START,
    BATTLE_VICTORY,
    BATTLE_DEFEAT,
    EXPLOSION,

    // Resource Sounds
    RESOURCE_COLLECTED,
    RESOURCE_FULL,

    // Social Sounds
    MESSAGE_RECEIVED,
    ACHIEVEMENT_UNLOCKED,
    REWARD_CLAIMED,
    ALLIANCE_JOINED,

    // Alert Sounds
    ATTACK_INCOMING,
    WARNING,
    NOTIFICATION
}

/**
 * Background music enum
 * TODO: Add corresponding music files to res/raw/
 */
enum class Music {
    MAIN_MENU,
    GAMEPLAY,
    BATTLE,
    VICTORY,
    DEFEAT
}
