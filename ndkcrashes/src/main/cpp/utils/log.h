#ifndef APPMETRICA_SDK_LOG_H
#define APPMETRICA_SDK_LOG_H

#include <android/log.h>

#ifdef APPMETRICA_DEBUG

#define TAG "AppMetricaNative"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#else

#define LOGD(...) 0
#define LOGI(...) 0
#define LOGW(...) 0
#define LOGE(...) 0

#endif

#endif //APPMETRICA_SDK_LOG_H
