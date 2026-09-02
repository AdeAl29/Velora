package com.example.finora.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.finora.R
import com.example.finora.core.datastore.FinoraDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundEffectManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: FinoraDataStore
) {
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<SoundType, Int>()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isLoaded = false

    enum class SoundType {
        CHACHING, WHOOSH, SUCCESS, TAP
    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                isLoaded = true
            }
        }

        try {
            soundMap[SoundType.CHACHING] = soundPool.load(context, R.raw.sfx_chaching, 1)
            soundMap[SoundType.WHOOSH] = soundPool.load(context, R.raw.sfx_whoosh, 1)
            soundMap[SoundType.SUCCESS] = soundPool.load(context, R.raw.sfx_success, 1)
            soundMap[SoundType.TAP] = soundPool.load(context, R.raw.sfx_tap, 1)
        } catch (_: Exception) {
        }
    }

    fun play(soundType: SoundType, volume: Float = 0.85f) {
        scope.launch {
            try {
                val soundEnabled = dataStore.isSoundEnabled.first()
                if (soundEnabled) {
                    val soundId = soundMap[soundType] ?: return@launch
                    soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun playChaChing() = play(SoundType.CHACHING, 0.9f)
    fun playWhoosh() = play(SoundType.WHOOSH, 0.8f)
    fun playSuccess() = play(SoundType.SUCCESS, 0.85f)
    fun playTap() = play(SoundType.TAP, 0.5f)
}
