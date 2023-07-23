#include <jni.h>
#include <dlfcn.h>
#include <string>

#include "../utils/log.h"

extern "C"
JNIEXPORT jstring JNICALL
Java_io_appmetrica_analytics_ndkcrashes_jni_core_NativeCrashUtilsJni_getLibDirInsideApk(
        JNIEnv *env, jclass thiz) {
    LOGD("getLibDirInsideApk");
    Dl_info dlInfo;
    auto thisMethod = reinterpret_cast<void *>(&Java_io_appmetrica_analytics_ndkcrashes_jni_core_NativeCrashUtilsJni_getLibDirInsideApk);
    if (dladdr(thisMethod, &dlInfo) == 0) {
        return nullptr;
    }
    std::string thisLib(dlInfo.dli_fname);
    size_t end = thisLib.rfind('/');
    if (end == std::string::npos) {
        return nullptr;
    }
    std::string libDir(thisLib, 0, end + 1);
    LOGD("getLibDirInsideApk return %s", libDir.c_str());
    return env->NewStringUTF(libDir.c_str());
}
