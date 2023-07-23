#include "JNIUtils.h"

namespace appmetrica {

    std::string JNIUtils::toStdString(JNIEnv *jniEnv, jstring jString) {
        const char *cStr = jniEnv->GetStringUTFChars(jString, nullptr);
        std::string stdString(cStr);
        jniEnv->ReleaseStringUTFChars(jString, cStr);
        return stdString;
    }

} // appmetrica
