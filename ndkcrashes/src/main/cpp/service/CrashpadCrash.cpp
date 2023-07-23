#include "CrashpadCrash.h"

namespace appmetrica {

    CrashpadCrash::CrashpadCrash(JNIEnv *jniEnv, const std::string &uuid, const std::string &dumpFile,
                                 const long long int creationTime, const std::string &appMetricaMetadata) :
            jniEnv(jniEnv),
            jObject(createNativeCrash(jniEnv, uuid, dumpFile, creationTime, appMetricaMetadata)) {}

    jobject CrashpadCrash::createNativeCrash(JNIEnv *jniEnv, const std::string &uuid, const std::string &dumpFile,
                                             const long long int creationTime, const std::string &appMetricaMetadata) {
        jclass jClass = jniEnv->FindClass("io/appmetrica/analytics/ndkcrashes/jni/service/CrashpadCrash");
        jmethodID jConstructor = jniEnv->GetMethodID(
                jClass, "<init>", "(Ljava/lang/String;Ljava/lang/String;JLjava/lang/String;)V");
        jstring jUuid = jniEnv->NewStringUTF(uuid.c_str());
        jstring jDumpFile = jniEnv->NewStringUTF(dumpFile.c_str());
        jstring jAppMetricaMetadata = jniEnv->NewStringUTF(appMetricaMetadata.c_str());
        jobject jNativeCrash = jniEnv->NewObject(
                jClass, jConstructor, jUuid, jDumpFile, creationTime, jAppMetricaMetadata);
        jniEnv->DeleteLocalRef(jUuid);
        jniEnv->DeleteLocalRef(jDumpFile);
        jniEnv->DeleteLocalRef(jAppMetricaMetadata);
        return jNativeCrash;
    }

} // appmetrica
