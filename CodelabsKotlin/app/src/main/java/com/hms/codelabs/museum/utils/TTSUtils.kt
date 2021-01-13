/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hms.codelabs.museum.utils

import android.os.Bundle
import android.util.Pair
import com.hms.codelabs.museum.models.Constant
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.tts.*

class TTSUtils(api_key: String?) {
    private var mlTtsConfig: MLTtsConfig
    private var mlTtsEngine: MLTtsEngine

    /**
     * Starts TTS reading
     *
     * @param text
     */
    fun startTTS(text: String) {
        mlTtsEngine.let {
            it.stop()
            val sentences = text.split("\n|\\.(?!\\d)|(?<!\\d)\\.").toTypedArray()
            for (sentence in sentences)
                it.speak(sentence, MLTtsEngine.QUEUE_APPEND)
        }
    }

    /**
     * Stops TTS reading
     */
    fun stopTTS() {
        mlTtsEngine.stop()
    }

    /**
     * Shutdowns TTS
     */
    fun destroyTTS() {
        mlTtsEngine.shutdown()
    }

    /**
     * Text to speech service initialization
     */
    init {
        MLApplication.getInstance().apiKey = api_key
        mlTtsConfig = MLTtsConfig()
                .setLanguage(MLTtsConstants.TTS_EN_US)
                .setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_EN)
                .setSpeed(Constant.TTS_SPEED)
                .setVolume(Constant.TTS_VOLUME)
        mlTtsEngine = MLTtsEngine(mlTtsConfig)
        mlTtsEngine.updateConfig(mlTtsConfig)
        val callback: MLTtsCallback = object : MLTtsCallback {
            override fun onError(s: String, mlTtsError: MLTtsError) {
                //onError Implementation
            }

            override fun onWarn(s: String, mlTtsWarn: MLTtsWarn) {
                //No need for onWarn
            }

            override fun onRangeStart(s: String, i: Int, i1: Int) {
                //No need for onRangeStart
            }

            override fun onAudioAvailable(s: String, mlTtsAudioFragment: MLTtsAudioFragment, i: Int, pair: Pair<Int, Int>, bundle: Bundle) {
                //No need for onAudioAvailable
            }

            override fun onEvent(s: String, i: Int, bundle: Bundle) {
                when (i) {
                    MLTtsConstants.EVENT_PLAY_START -> {
                    }
                    MLTtsConstants.EVENT_PLAY_STOP ->
                        // Called when playback stops.
                        bundle.getBoolean(MLTtsConstants.EVENT_PLAY_STOP_INTERRUPTED)
                    MLTtsConstants.EVENT_PLAY_RESUME -> {
                    }
                    MLTtsConstants.EVENT_PLAY_PAUSE -> {
                    }
                    else -> {
                    }
                }
            }
        }
        mlTtsEngine.setTtsCallback(callback)
    }
}