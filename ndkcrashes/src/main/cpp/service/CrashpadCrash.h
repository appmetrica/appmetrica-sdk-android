#ifndef APPMETRICA_SDK_CRASHPADCRASH_H
#define APPMETRICA_SDK_CRASHPADCRASH_H

#include <jni.h>
#include <string>

namespace appmetrica {

    class CrashpadCrash {
    public:
        const jobject jObject;

        CrashpadCrash(JNIEnv *jniEnv, const std::string &uuid, const std::string &dumpFile,
                      const long long int creationTime, const std::string &appMetricaMetadata);

    private:
        JNIEnv *jniEnv;

        static jobject createNativeCrash(JNIEnv *jniEnv, const std::string &uuid, const std::string &dumpFile,
                                         const long long int creationTime, const std::string &appMetricaMetadata);
    };

} // appmetrica

#endif //APPMETRICA_SDK_CRASHPADCRASH_H
