#include <jni.h>

#include "../utils/JNIUtils.h"
#include "../utils/log.h"
#include "AppMetricaCrashpadConfig.h"
#include "client/crashpad_client.h"

static crashpad::CrashpadClient *exceptionHandler = nullptr;

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_appmetrica_analytics_ndkcrashes_jni_core_NativeCrashCoreJni_startHandlerWithLinkerAtCrash(
        JNIEnv *env, jobject thiz, jobject jConfig) {
    LOGD("startHandlerWithLinkerAtCrash");
    appmetrica::AppMetricaCrashpadConfig config(env, jConfig);
    base::FilePath crashFolder(config.getCrashFolder());
    std::map<std::string, std::string> annotations;
    std::vector<std::string> arguments;

    exceptionHandler = new crashpad::CrashpadClient();
    bool result = exceptionHandler->StartHandlerWithLinkerAtCrash(
            std::string(), // handler_library
            config.getHandlerPath(),
            config.is64bit(),
            nullptr, // env
            crashFolder,
            base::FilePath(), // metrics_dir
            config.getSocketName(),
            annotations,
            arguments,
            config.getAppMetricaMetadata()
    );
    LOGD("startHandlerWithLinkerAtCrash return %s", result ? "true" : "false");
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_appmetrica_analytics_ndkcrashes_jni_core_NativeCrashCoreJni_startJavaHandlerAtCrash(
        JNIEnv *env, jobject thiz, jobject jConfig) {
    LOGD("startJavaHandlerAtCrash");
    appmetrica::AppMetricaCrashpadConfig config(env, jConfig);
    base::FilePath crashFolder(config.getCrashFolder());
    std::map<std::string, std::string> annotations;
    std::vector<std::string> envList;
    std::vector<std::string> arguments;

    envList.push_back("CLASSPATH=" + config.getApkPath());
    envList.push_back("LD_LIBRARY_PATH=" + config.getLibPath());
    envList.push_back("ANDROID_DATA=" + config.getDataDir());

    arguments.push_back("--handler-lib=" + config.getHandlerPath());

    exceptionHandler = new crashpad::CrashpadClient();
    bool result = exceptionHandler->StartJavaHandlerAtCrash(
            config.getJavaHandlerClassName(),
            &envList,
            crashFolder,
            base::FilePath(), // metrics_dir
            config.getSocketName(),
            annotations,
            arguments,
            config.getAppMetricaMetadata()
    );
    LOGD("startJavaHandlerAtCrash return %s", result ? "true" : "false");
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_appmetrica_analytics_ndkcrashes_jni_core_NativeCrashCoreJni_startHandlerAtCrash(
        JNIEnv *env, jobject thiz, jobject jConfig) {
    LOGD("startHandlerAtCrash");
    appmetrica::AppMetricaCrashpadConfig config(env, jConfig);
    base::FilePath crashFolder(config.getCrashFolder());
    std::map<std::string, std::string> annotations;
    std::vector<base::FilePath> attachments;
    std::vector<std::string> arguments;

    exceptionHandler = new crashpad::CrashpadClient();
    bool result = exceptionHandler->StartHandlerAtCrash(
            base::FilePath(config.getHandlerPath()),
            crashFolder,
            base::FilePath(), // metrics_dir
            config.getSocketName(),
            annotations,
            arguments,
            attachments,
            config.getAppMetricaMetadata()
    );
    LOGD("startHandlerAtCrash return %s", result ? "true" : "false");
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_appmetrica_analytics_ndkcrashes_jni_core_NativeCrashCoreJni_updateAppMetricaMetadataJni(
        JNIEnv *env, jobject thiz, jstring jMetadata) {
    LOGD("updateAppMetricaMetadata");
    if (exceptionHandler != nullptr) {
        std::string metadata = appmetrica::JNIUtils::toStdString(env, jMetadata);
        LOGI("use new appmetrica metadata %s", metadata.c_str());
        exceptionHandler->UpdateAppMetricaMetadata(appmetrica::JNIUtils::toStdString(env, jMetadata));
    }
}
