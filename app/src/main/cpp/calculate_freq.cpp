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
#include <stdio.h>
#include <complex.h>

#define TAG "From Native"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

fftw_plan plan;
fftw_plan plan_inverse;

extern "C"
JNIEXPORT double JNICALL
Java_com_example_licentachitara_MainActivity_FFTAnalyze(JNIEnv *env, jobject main,
                                                        jdoubleArray audioSamples, jint FFT_SIZE) {

    jdouble *audioData = env->GetDoubleArrayElements(audioSamples, NULL);
    fftw_complex *output = (fftw_complex*) fftw_malloc(sizeof(fftw_complex) * FFT_SIZE);
    fftw_complex *conjugatedData = (fftw_complex*) fftw_malloc(sizeof(fftw_complex) * FFT_SIZE);
    plan = fftw_plan_dft_r2c_1d(FFT_SIZE, audioData, output, FFTW_MEASURE);
    fftw_execute(plan);
    jdoubleArray frequencyData = env->NewDoubleArray(FFT_SIZE);
    jdouble *magnitude = env->GetDoubleArrayElements(frequencyData, NULL);

    for (int j = 0; j<FFT_SIZE;j++)
    {
        conjugatedData[j][0] = output[j][0] * output[j][0] + output[j][1] * output[j][1];
        conjugatedData[j][1] = 0.0;
    }
    jdoubleArray peakArray = env->NewDoubleArray(FFT_SIZE);
    jdoubleArray autoCorrelationArray = env->NewDoubleArray(FFT_SIZE);
    jdouble *autoCorrelationFunction = env->GetDoubleArrayElements(autoCorrelationArray, NULL);
    jdouble *peakValues = env->GetDoubleArrayElements(peakArray, NULL);

    plan_inverse = fftw_plan_dft_c2r_1d(FFT_SIZE, conjugatedData, autoCorrelationFunction, FFTW_MEASURE);
    fftw_execute(plan_inverse);
    double maxACF = -INFINITY;
    for (int m=0;m < FFT_SIZE / 2 + 1;m++) {
        autoCorrelationFunction[m] = autoCorrelationFunction[m]/FFT_SIZE;
        if (autoCorrelationFunction[m] > maxACF) {
            maxACF = autoCorrelationFunction[m];
        }
    }

    for (int v=0;v < FFT_SIZE / 2 + 1;v++) {
        autoCorrelationFunction[v] = autoCorrelationFunction[v] / maxACF;
       // LOGI("AutoCorrelation values: %f\n", autoCorrelationFunction[v]);
    }

    double maxValueF = -INFINITY;
    int positionMax = -1;
    for (int s=1; s< FFT_SIZE / 2 + 1; s++) {

        if (autoCorrelationFunction[s] > maxValueF && autoCorrelationFunction[s] > 0) {
            maxValueF = autoCorrelationFunction[s];
            //LOGI("NEW MAX VALUE: %f\n", maxValueF);
            positionMax = s;
            //LOGI("NEW POSITION MAX: %d\n", positionMax);
        }

        if (autoCorrelationFunction[s] < 0) {
            peakValues[positionMax] = maxValueF;
            maxValueF = -INFINITY;
            positionMax = -1;
        }
    }

// calcul mai rapid la perioada asemanator ca valoare
//    double valueOfRelevantPeak = 0.5;
//    int ok = 1;
//    double actual = 1;
//    int period;
//
//    for (int s=1;s < FFT_SIZE / 2 + 1;s++)
//    {
//        double pred = actual;
//        actual = autoCorrelationFunction[s];
//
//        if (ok == 2 && actual - pred <= 0) {
//            period = s;
//            ok = 3;
//        }
//        if (ok == 1 && actual > valueOfRelevantPeak && actual - pred > 0) {
//            ok = 2;
//        }
//    }
//
//    LOGI("PERIOD IS: %d", period);


    double periodOfTheSound = 0;
    double peakMaxValue = -INFINITY;

    for (int t = 2; t < FFT_SIZE / 2 + 1; t++) {
        if (peakValues[t] != 0) {
            if (peakValues[t] > peakMaxValue) {
                peakMaxValue = peakValues[t];
            }
        }
    }

    LOGI("MAX VALUE FROM ALL PEAKS: %f", peakMaxValue);

    double threshhold = peakMaxValue * 0.9;

    for (int test = 2; test < FFT_SIZE / 2 + 1; test++) {
        if (peakValues[test] != 0 && peakValues[test] > threshhold) {
            LOGI("THE PEAK VALUE:  %f  AND THE POSITION  %d", peakValues[test], test);
            periodOfTheSound = test;
            break;
        }
    }


// calcul media perioadelor si afisare frecventa in functie de medie => acuratete slaba rezultate eronate
//    double meanOfPeriods;
//    double maxPeriodT = 0;
//    double sumOfPeriods = 0;
//    int pred = 0;
//    double k = 0;
//    for (int val = 1; val < FFT_SIZE / 2 + 1; val++)
//    {
//        if (peakValues[val] != 0)
//        {
//            //LOGI("Peak value at position  %d  with value  %f\n", val, peakValues[val]);
//
//            if (pred != 0 && val - pred > 0 && maxPeriodT < (val - pred))
//            {
//                maxPeriodT = val - pred;
//               // LOGI("THE MAX PERIOD RIGHT NOW %f\n", maxPeriodT);
//            }
//
//            pred = val;
//           // LOGI("THE PREDECESOR RIGHT NOW %d\n", pred);
//            sumOfPeriods = sumOfPeriods + maxPeriodT;
//           // LOGI("THE SUM OF PERIODS %f\n", sumOfPeriods);
//            k++;
//        }
//    }
//
//    LOGI("TOTAL SUM OF PERIODS: %f", sumOfPeriods);
//    LOGI("MAX PERIOD   %f", maxPeriodT);
//    LOGI("K value   %f", k);
//    meanOfPeriods = sumOfPeriods / k;
//    LOGI("THE MEAN OF PERIODS %.5f", meanOfPeriods);

    //double freq = magnitudeIndex * 44100 / FFT_SIZE;

    double sampleRate = 44100;
    double freq = sampleRate / periodOfTheSound;

    int har = 2;
    while (freq > 350)
    {
        freq = freq / har;
        har++;
    }

    if (freq < 63) {
        freq = 0;
    }



    fftw_free(output);
    fftw_free(conjugatedData);


    // Release the JNI array
    env->ReleaseDoubleArrayElements(audioSamples, audioData, 0);
    env->ReleaseDoubleArrayElements(frequencyData, magnitude, 0);
    env->ReleaseDoubleArrayElements(autoCorrelationArray,autoCorrelationFunction,0);
    env->ReleaseDoubleArrayElements(peakArray,peakValues,0);

    LOGI("FREQ VALUE %.4f", freq);

    return freq;

}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_licentachitara_MainActivity_destroyPlanFFT(JNIEnv *env, jobject thiz) {
    if (plan != nullptr) {
        fftw_destroy_plan(plan);
        plan = nullptr;
        LOGI("PLAN NORMAL DESTROYED");
    }
    if (plan_inverse != nullptr) {
        fftw_destroy_plan(plan_inverse);
        plan_inverse = nullptr;
        LOGI("PLAN INVERSE DESTROYED");
    }
}