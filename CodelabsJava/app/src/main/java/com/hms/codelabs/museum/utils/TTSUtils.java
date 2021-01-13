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
package com.hms.codelabs.museum.utils;

import android.os.Bundle;
import android.util.Pair;

import com.hms.codelabs.museum.models.Constant;
import com.huawei.hms.mlsdk.common.MLApplication;
import com.huawei.hms.mlsdk.tts.MLTtsAudioFragment;
import com.huawei.hms.mlsdk.tts.MLTtsCallback;
import com.huawei.hms.mlsdk.tts.MLTtsConfig;
import com.huawei.hms.mlsdk.tts.MLTtsConstants;
import com.huawei.hms.mlsdk.tts.MLTtsEngine;
import com.huawei.hms.mlsdk.tts.MLTtsError;
import com.huawei.hms.mlsdk.tts.MLTtsWarn;

public class TTSUtils {
    MLTtsConfig mlTtsConfig;
    MLTtsEngine mlTtsEngine;

    /**
     * Text to speech service initialization
     */
    public TTSUtils(String api_key) {
        MLApplication.getInstance().setApiKey(api_key);

        mlTtsConfig = new MLTtsConfig()
                .setLanguage(MLTtsConstants.TTS_EN_US)
                .setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_EN)
                .setSpeed(Constant.TTS_SPEED)
                .setVolume(Constant.TTS_VOLUME);
        mlTtsEngine = new MLTtsEngine(mlTtsConfig);
        mlTtsEngine.updateConfig(mlTtsConfig);

        MLTtsCallback callback = new MLTtsCallback() {
            @Override
            public void onError(String s, MLTtsError mlTtsError) {
                //onError Implementation
            }

            @Override
            public void onWarn(String s, MLTtsWarn mlTtsWarn) {
                //No need for onWarn
            }

            @Override
            public void onRangeStart(String s, int i, int i1) {
                //No need for onRangeStart
            }

            @Override
            public void onAudioAvailable(String s, MLTtsAudioFragment mlTtsAudioFragment, int i, Pair<Integer, Integer> pair, Bundle bundle) {
                //No need for onAudioAvailable
            }

            @Override
            public void onEvent(String s, int i, Bundle bundle) {
                switch (i) {
                    case MLTtsConstants.EVENT_PLAY_START:
                        // Called when playback starts.
                        break;
                    case MLTtsConstants.EVENT_PLAY_STOP:
                        // Called when playback stops.
                        bundle.getBoolean(MLTtsConstants.EVENT_PLAY_STOP_INTERRUPTED);
                        break;
                    case MLTtsConstants.EVENT_PLAY_RESUME:
                        // Called when playback resumes.
                        break;
                    case MLTtsConstants.EVENT_PLAY_PAUSE:
                        // Called when playback pauses.
                        break;
                    default:
                        break;
                }
            }
        };
        mlTtsEngine.setTtsCallback(callback);
    }

    /**
     * Starts TTS reading
     *
     * @param text
     */
    public void startTTS(String text) {
        if (mlTtsEngine != null) {
            mlTtsEngine.stop();
            String[] sentences = text.split("\n|\\.(?!\\d)|(?<!\\d)\\.");

            for (String sentence : sentences)
                mlTtsEngine.speak(sentence, MLTtsEngine.QUEUE_APPEND);
        }
    }

    /**
     * Stops TTS reading
     */
    public void stopTTS() {
        if (mlTtsEngine != null) {
            mlTtsEngine.stop();
        }
    }

    /**
     * Shutdowns TTS
     */
    public void destroyTTS() {
        if (mlTtsEngine != null) {
            mlTtsEngine.shutdown();
        }
    }
}
