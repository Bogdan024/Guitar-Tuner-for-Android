#include <jni.h>

// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("licentachitara");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("licentachitara")
//      }
//    }

#include <jni.h>
#include <fftw3.h>
#include <math.h>
#include <android/log.h>

//#define FFT_SIZE 3584
#define TAG "From Native"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)



extern "C"
JNIEXPORT double JNICALL
Java_com_example_licentachitara_MainActivity_FFTAnalyze(JNIEnv *env, jobject thiz,
                                                        jdoubleArray audioSamples, jint FFT_SIZE) {
    // TODO: implement FFTAnalyze()

    jdouble *audioData = env->GetDoubleArrayElements(audioSamples, NULL);

    LOGI("FFT SIZE THE BUFFERSIZE %d", FFT_SIZE);

    //fftw_complex *input = (fftw_complex*) fftw_malloc(sizeof(fftw_complex) * FFT_SIZE);
    fftw_complex *output = (fftw_complex*) fftw_malloc(sizeof(fftw_complex) * FFT_SIZE);
   // fftw_plan plan = fftw_plan_dft_1d(FFT_SIZE, input, output, FFTW_FORWARD, FFTW_ESTIMATE);

    fftw_plan plan = fftw_plan_dft_r2c_1d(FFT_SIZE, audioData, output, FFTW_ESTIMATE);

    LOGI("fftwplan working");


    fftw_execute(plan);

    LOGI("fftw execute working");


    // Get the frequency data from the FFTW output array
    jdoubleArray frequencyData = env->NewDoubleArray(FFT_SIZE / 2);
    jdouble *magnitude = env->GetDoubleArrayElements(frequencyData, NULL);

    double maxMagnitude = -INFINITY;
    int magnitudeIndex = -1;

    for (int i = 0; i < FFT_SIZE / 2 - 1; i++) {
        magnitude[i] = sqrt(output[i][0] * output[i][0] + output[i][1] * output[i][1]);
        if (magnitude[i] > maxMagnitude)
        {
            maxMagnitude = magnitude[i];
            magnitudeIndex = i;
        }
    }

    double freq = magnitudeIndex * 44100 / FFT_SIZE;


    // Release the FFTW resources
    //fftw_free(input);
    fftw_free(output);
   // fftw_destroy_plan(plan);

    // Release the JNI array
    env->ReleaseDoubleArrayElements(audioSamples, audioData, 0);
    env->ReleaseDoubleArrayElements(frequencyData, magnitude, 0);

    return freq;

}