#ifndef APPMETRICA_SDK_JNIUTILS_H
#define APPMETRICA_SDK_JNIUTILS_H

#include <jni.h>
#include <string>

namespace appmetrica {

    class JNIUtils {
    public:
        static std::string toStdString(JNIEnv *jniEnv, jstring jString);
    };

} // appmetrica

#endif //APPMETRICA_SDK_JNIUTILS_H
