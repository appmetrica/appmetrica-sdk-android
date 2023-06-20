#include <jni.h>
#include "client/crashpad_client.h"
#include <android/log.h>
#include <dlfcn.h>

#include "AppmetricaCrashpadConfig.h"
#include "AppProcessConfig.h"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

#define TAG "AppMetricaDebug"

// Debuggable or not.
static bool DEBUG = false;

using namespace appmetrica;
using namespace base;
using namespace crashpad;
using namespace std;

// Native crash handler.
static CrashpadClient *exceptionHandler = nullptr;

extern "C" {

    // Disables or enables logs.
    JNIEXPORT void JNICALL
    Java_io_appmetrica_analytics_impl_ac_CrashpadHelper_logsEnabled(JNIEnv *env, jclass obj, jboolean enabled) {
        DEBUG = enabled ? true : false;
    }

    JNIEXPORT jstring JNICALL
    Java_io_appmetrica_analytics_impl_ac_CrashpadHelper_getLibraryVersion(JNIEnv *env, jclass obj) {
        return env->NewStringUTF(LIBRARY_VERSION_NAME);
    }

    JNIEXPORT jstring JNICALL
    Java_io_appmetrica_analytics_impl_ac_CrashpadHelper_getLibDirInsideApk(JNIEnv *env, jclass obj) {
        Dl_info dlInfo;
        if (dladdr(reinterpret_cast<void*>(&Java_io_appmetrica_analytics_impl_ac_CrashpadHelper_getLibDirInsideApk), &dlInfo) == 0) {
            return nullptr;
        }
        string thisLib(dlInfo.dli_fname);
        size_t end = thisLib.rfind('/');
        if (end == std::string::npos) {
            return nullptr;
        }
        string libDir = string(thisLib, 0, end + 1);
        return env->NewStringUTF(libDir.c_str());
    }

    // Sets up the native handler.
    JNIEXPORT void JNICALL
    Java_io_appmetrica_analytics_impl_ac_CrashpadHelper_setUpNativeUncaughtExceptionHandler(
            JNIEnv *env, jclass obj, jobject bundleArg
    ) {
        if (DEBUG) LOGI("Set up for native crashes");

        // Create descriptor for dumps.
        auto bundle = BundleWrapper(env, bundleArg);
        auto config = AppmetricaCrashpadConfig(bundle);
        exceptionHandler = new CrashpadClient();
        auto handlerLib = FilePath(config.handlerPath);
        FilePath crashFolder(config.dumpDirectory);
        FilePath empty("");
        map<string, string> annotations;
        unordered_map<string, string> meta;

        vector<string> arguments = vector<string>();
        arguments.push_back("--client-description=" + bundle.getString(kArgumentClientDescription));

        if (config.useLinker) {
            bool result = exceptionHandler->StartHandlerWithLinkerAtCrash(
                    handlerLib.value(), config.is64bit, nullptr, crashFolder, empty,
                    config.socketName, annotations, arguments,
                    bundle.getString(kArgumentRuntimeConfig)
            );
        } else if (config.useAppProcess) {
            vector<string> envList;
            AppProcessConfig appProcessConfig(bundle);
//            envList.push_back("CLASSPATH=" + string(getenv("CLASSPATH")) + ":" + bundle.getString("arg_apk_path"));
//            envList.push_back("LD_LIBRARY_PATH=" + string(getenv("LD_LIBRARY_PATH")) + ":" + bundle.getString("arg_native_lib"));
            envList.push_back("CLASSPATH=" + appProcessConfig.apkPath);
            envList.push_back("LD_LIBRARY_PATH=" + appProcessConfig.libPath);
            envList.push_back("ANDROID_DATA=" + appProcessConfig.dataDir);
            arguments.push_back("--handler-lib="+config.handlerPath);
            bool result = exceptionHandler->StartJavaHandlerAtCrash(
                    appProcessConfig.mainClass, &envList,
                    crashFolder, empty, config.socketName, annotations, arguments,
                    bundle.getString(kArgumentRuntimeConfig)
            );
        } else {
            bool result = exceptionHandler->StartHandlerAtCrash(
                    handlerLib, crashFolder, empty,
                    config.socketName, annotations, arguments, {},
                    bundle.getString(kArgumentRuntimeConfig)
            );
        }
    }

    JNIEXPORT void JNICALL
    Java_io_appmetrica_analytics_impl_ac_CrashpadHelper_updateRuntimeConfig(
            JNIEnv *env, jclass obj, jstring runtimeConfigArg
    ) {
        string config = readString(env, runtimeConfigArg);
        exceptionHandler->UpdateRuntimeConfig(config);
    }

    // Cancels setup of the native handler.
    JNIEXPORT void JNICALL
    Java_io_appmetrica_analytics_impl_ac_CrashpadHelper_cancelSetUpNativeUncaughtExceptionHandler(
            JNIEnv *env, jclass obj) {
        if (DEBUG) LOGI("Cancel setup for native crashes");

        delete exceptionHandler;
    }

}
