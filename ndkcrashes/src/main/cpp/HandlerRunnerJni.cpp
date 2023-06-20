#include <dlfcn.h>
#include <jni.h>

#include "JniUtil.h"

using namespace appmetrica;
using namespace std;

namespace crashpad {

    extern "C" {
        typedef int (*CrashpadHandlerMain)(int argc, char* argv[]);
    }
}
using namespace crashpad;

extern "C" {

    JNIEXPORT void JNICALL
    Java_io_appmetrica_analytics_impl_ac_HandlerRunner_runHandler(
            JNIEnv *env, jclass obj, jobjectArray args
    ) {
        auto arrSize = env->GetArrayLength(args);
        auto argsSize = arrSize;
        auto realArgc = argsSize-1;
        char* cArgs[realArgc];
        int modifier = 0;
        string dlName;
        const string argumentName("--handler-lib=");
        for (int i = 1; i < argsSize; i++) {
            auto jArgvItem = reinterpret_cast<jstring>(env->GetObjectArrayElement(args, i));
            auto jStringArg = readString(env, jArgvItem);
            auto namePos = jStringArg.find(argumentName);
            if (namePos != string::npos) {
                dlName = jStringArg.substr(namePos + argumentName.length());
                modifier = -1;
                cArgs[0] = strdup(dlName.c_str());
            } else {
                cArgs[i + modifier] = strdup(jStringArg.c_str());
            }
            env->DeleteLocalRef(jArgvItem);
        }
        if (!dlName.empty()) {
            auto dlNameCstr = dlName.c_str();
            auto dll = dlopen(dlNameCstr, RTLD_GLOBAL);
            if (dll == nullptr) {
                return;
            }
            auto func = dlsym(dll, "CrashpadHandlerMain");
            if (!func) {
                return;
            }
            ((CrashpadHandlerMain)func)(realArgc, cArgs);
        }
    }

    JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
        return JNI_VERSION_1_4;
    }
}
