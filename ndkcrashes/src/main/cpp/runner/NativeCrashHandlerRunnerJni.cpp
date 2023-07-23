#include <android/log.h>
#include <dlfcn.h>
#include <jni.h>
#include <string>

#include "../utils/JNIUtils.h"
#include "../utils/log.h"

namespace crashpad {

    extern "C"
    typedef int (*CrashpadHandlerMain)(int argc, char *argv[]);

}

extern "C"
JNIEXPORT jint JNICALL
Java_io_appmetrica_analytics_ndkcrashes_jni_runner_NativeCrashHandlerRunnerJni_runHandler(
        JNIEnv *env, jobject thiz, jobjectArray jArgs) {
    LOGD("runHandler");
    jsize argsSize = env->GetArrayLength(jArgs);
    char *handlerArgs[argsSize - 1];

    int modifier = 1;
    std::string dllName;
    const std::string handlerLibArgName("--handler-lib=");
    for (int argIdx = 0; argIdx < argsSize; ++argIdx) {
        auto jArgItem = reinterpret_cast<jstring>(env->GetObjectArrayElement(jArgs, argIdx));
        auto argItem = appmetrica::JNIUtils::toStdString(env, jArgItem);
        auto namePos = argItem.find(handlerLibArgName);
        if (namePos != std::string::npos) {
            dllName = argItem.substr(namePos + handlerLibArgName.length());
            modifier = -1;
            handlerArgs[0] = strdup(dllName.c_str());
        } else {
            handlerArgs[argIdx + modifier] = strdup(argItem.c_str());
        }
        env->DeleteLocalRef(jArgItem);
    }

    if (dllName.empty()) {
        LOGE("Not found handler lib name");
        return EXIT_FAILURE;
    }
    auto dll = dlopen(dllName.c_str(), RTLD_GLOBAL);
    if (!dll) {
        LOGE("Failed to open dll: %s", dlerror());
        return EXIT_FAILURE;
    }
    auto crashpadHandlerMainFunc = dlsym(dll, "CrashpadHandlerMain");
    if (!crashpadHandlerMainFunc) {
        LOGE("Not found CrashpadHandlerMain: %s", dlerror());
        return EXIT_FAILURE;
    }
    int result = ((crashpad::CrashpadHandlerMain) crashpadHandlerMainFunc)(argsSize - 1, handlerArgs);
    LOGD("runHandler return %d", result);
    return result;
}

extern "C"
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    return JNI_VERSION_1_4;
}
