#include <jni.h>
#include <string>
#include <android/log.h>
#include <OpenSource/SuperpoweredAndroidAudioIO.h>
#include <Superpowered.h>
#include <SuperpoweredAdvancedAudioPlayer.h>
#include <SuperpoweredSimple.h>
#include <SuperpoweredCPU.h>
#include <malloc.h>
#include <SLES/OpenSLES_AndroidConfiguration.h>
#include <SLES/OpenSLES.h>

#define log_print __android_log_print

static SuperpoweredAndroidAudioIO *audioIO;
static Superpowered::AdvancedAudioPlayer *player;
int numFrames = 0;

// This is called periodically by the audio engine.
static bool audioProcessing (
        void * __unused clientdata, // custom pointer
        short int *audio,           // output buffer
        int numberOfFrames,         // number of frames to process
        int samplerate              // current sample rate in Hz
) {
    player->outputSamplerate = (unsigned int)samplerate;
    float playerOutput[numberOfFrames * 2];
    numFrames = numberOfFrames;
    if (player->processStereo(playerOutput, false, (unsigned int)numberOfFrames)) {
        Superpowered::FloatToShortInt(playerOutput, audio, (unsigned int)numberOfFrames);
        return true;
    } else return false;
}

// StartAudio - Start audio engine and initialize player.
extern "C" JNIEXPORT void
Java_com_superpowered_playerexample_audio_AudioPlayer_NativeInit(JNIEnv *env, jobject __unused obj, jint samplerate, jint buffersize, jstring tempPath) {
    Superpowered::Initialize(
            "ExampleLicenseKey-WillExpire-OnNextUpdate",
            false, // enableAudioAnalysis (using SuperpoweredAnalyzer, SuperpoweredLiveAnalyzer, SuperpoweredWaveform or SuperpoweredBandpassFilterbank)
            false, // enableFFTAndFrequencyDomain (using SuperpoweredFrequencyDomain, SuperpoweredFFTComplex, SuperpoweredFFTReal or SuperpoweredPolarFFT)
            false, // enableAudioTimeStretching (using SuperpoweredTimeStretching)
            false, // enableAudioEffects (using any SuperpoweredFX class)
            true,  // enableAudioPlayerAndDecoder (using SuperpoweredAdvancedAudioPlayer or SuperpoweredDecoder)
            false, // enableCryptographics (using Superpowered::RSAPublicKey, Superpowered::RSAPrivateKey, Superpowered::hasher or Superpowered::AES)
            false  // enableNetworking (using Superpowered::httpRequest)
    );

    // setting the temp folder for progressive downloads or HLS playback
    // not needed for local file playback
    const char *str = env->GetStringUTFChars(tempPath, 0);
    Superpowered::AdvancedAudioPlayer::setTempFolder(str);
    env->ReleaseStringUTFChars(tempPath, str);

    // creating the player
    player = new Superpowered::AdvancedAudioPlayer((unsigned int)samplerate, 10);

    log_print(ANDROID_LOG_ERROR, "PlayerExample", "Sync To Bpm");

    audioIO = new SuperpoweredAndroidAudioIO (
            samplerate,                     // device native sampling rate
            buffersize,                     // device native buffer size
            false,                          // enableInput
            true,                           // enableOutput
            audioProcessing,                // process callback function
            NULL,                           // clientData
            -1,                             // inputStreamType (-1 = default)
            SL_ANDROID_STREAM_MEDIA         // outputStreamType (-1 = default)
    );
}

// OpenFile - Open file in player, specifying offset and length.
extern "C" JNIEXPORT void
Java_com_superpowered_playerexample_audio_AudioPlayer_OpenFileFromAPK (
        JNIEnv *env,
        jobject __unused obj,
        jstring path,       // path to APK file
        jint offset,        // offset of audio file
        jint length         // length of audio file
) {
    const char *str = env->GetStringUTFChars(path, 0);
    player->open(str, offset, length, 0, true);
    /*player->cachePosition(0,255);*/
    env->ReleaseStringUTFChars(path, str);

    bool buf = player->isWaitingForBuffering();

    if(buf == true)
        log_print(ANDROID_LOG_ERROR, "PlayerExample", "true");
    else
        log_print(ANDROID_LOG_ERROR, "PlayerExample", "false");

    // open file from any path: player->open("file system path to file");
    // open file from network (progressive download): player->open("http://example.com/music.mp3");
    // open HLS stream: player->openHLS("http://example.com/stream");
}

// onUserInterfaceUpdate - Called periodically. Check and react to player events. This can be done in any thread.
extern "C" JNIEXPORT jboolean
Java_com_superpowered_playerexample_audio_AudioPlayer_onUserInterfaceUpdate(JNIEnv * __unused env, jobject __unused obj) {
    switch (player->getLatestEvent()) {
        case Superpowered::PlayerEvent_None:
        case Superpowered::PlayerEvent_Opening: break; // do nothing
        case Superpowered::PlayerEvent_Opened: player->playSynchronized(); break;
        case Superpowered::PlayerEvent_OpenFailed:
        {
            int openError = player->getOpenErrorCode();
            log_print(ANDROID_LOG_ERROR, "PlayerExample", "Open error %i: %s", openError, Superpowered::AdvancedAudioPlayer::statusCodeToString(openError));
        }
            break;
        case Superpowered::PlayerEvent_ConnectionLost:
            log_print(ANDROID_LOG_ERROR, "PlayerExample", "Network download failed."); break;
        case Superpowered::PlayerEvent_ProgressiveDownloadFinished:
            log_print(ANDROID_LOG_ERROR, "PlayerExample", "Download finished. Path: %s", player->getFullyDownloadedFilePath()); break;
    }

    if (player->eofRecently()) player->setPosition(0, false, false);
    return (jboolean)player->isPlaying();
}

// TogglePlayback - Toggle Play/Pause state of the player.
extern "C" JNIEXPORT void
Java_com_superpowered_playerexample_audio_AudioPlayer_TogglePlayback(JNIEnv * __unused env, jobject __unused obj) {
    player->togglePlayback();
    Superpowered::CPU::setSustainedPerformanceMode(player->isPlaying()); // prevent dropouts
}

// onBackground - Put audio processing to sleep if no audio is playing.
extern "C" JNIEXPORT void
Java_com_superpowered_playerexample_audio_AudioPlayer_onBackground(JNIEnv * __unused env, jobject __unused obj) {
    audioIO->onBackground();
}

// onForeground - Resume audio processing.
extern "C" JNIEXPORT void
Java_com_superpowered_playerexample_audio_AudioPlayer_onForeground(JNIEnv * __unused env, jobject __unused obj) {
    audioIO->onForeground();
}

// Cleanup - Free resources.
extern "C" JNIEXPORT void
Java_com_superpowered_playerexample_audio_AudioPlayer_Cleanup(JNIEnv * __unused env, jobject __unused obj) {
    delete audioIO;
    delete player;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_superpowered_playerexample_audio_AudioPlayer_syncToMsElapsedSinceLastBeat(JNIEnv *env, jobject thiz,jint ms) {
   player->syncToMsElapsedSinceLastBeat = ms;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_superpowered_playerexample_audio_AudioPlayer_loop(JNIEnv *env, jobject thiz, jdouble start_ms, jdouble length_ms) {
    float playerOutput[numFrames*2];
    player->processStereo(playerOutput, false, (unsigned int)numFrames, 1.0f);
    player->loop(start_ms,length_ms,true,255,false,0,false,false);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_superpowered_playerexample_audio_AudioPlayer_pause(JNIEnv *env, jobject thiz) {
    player->pause(0,0);
    player->exitLoop(false);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_superpowered_playerexample_audio_AudioPlayer_silentLoop(JNIEnv *env, jobject thiz, jint start, jint length) {
    player->play();
    player->loop(start,length,true,255,true,0,false,true);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_superpowered_playerexample_audio_AudioPlayer_setPosition(JNIEnv *env, jobject thiz, jint mill) {
    player->setPosition(mill,false,false,false,false);
}extern "C"
JNIEXPORT jdouble JNICALL
Java_com_superpowered_playerexample_audio_AudioPlayer_getLatency(JNIEnv *env, jobject thiz) {
    return player->getPositionMs();
}extern "C"
JNIEXPORT void JNICALL
Java_com_superpowered_playerexample_audio_AudioPlayer_play(JNIEnv *env, jobject thiz) {
   player->play();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_superpowered_playerexample_audio_AudioPlayer_restartAudio(JNIEnv *env, jobject thiz) {
    player->setPosition(0, false, false);
}extern "C"
JNIEXPORT jboolean JNICALL
Java_com_superpowered_playerexample_audio_AudioPlayer_getBuffering(JNIEnv *env, jobject thiz) {
    return player->isWaitingForBuffering();
}extern "C"
JNIEXPORT void JNICALL
Java_com_superpowered_playerexample_audio_AudioPlayer_cachePosition(JNIEnv *env, jobject thiz,
                                                               jint milli) {
    player->cachePosition(milli,1);
}extern "C"
JNIEXPORT jdouble JNICALL
Java_com_superpowered_playerexample_audio_AudioPlayer_getAudioStartMs(JNIEnv *env, jobject thiz) {
    player->getAudioStartMs();
}extern "C"
JNIEXPORT jdouble JNICALL
Java_com_superpowered_playerexample_audio_AudioPlayer_getDurationMs(JNIEnv *env, jobject thiz) {
    return player->getDurationMs();
}extern "C"
JNIEXPORT jboolean JNICALL
Java_com_superpowered_playerexample_audio_AudioPlayer_EOF(JNIEnv *env, jobject thiz) {
    return player->eofRecently();
}