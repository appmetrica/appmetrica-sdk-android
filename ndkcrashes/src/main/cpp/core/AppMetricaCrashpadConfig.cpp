#include "../utils/JNIUtils.h"
#include "AppMetricaCrashpadConfig.h"

namespace appmetrica {

    AppMetricaCrashpadConfig::AppMetricaCrashpadConfig(JNIEnv *jniEnv, jobject jConfig) : jniEnv(jniEnv),
                                                                                          jObject(jConfig) {
        jclass jClass = jniEnv->FindClass("io/appmetrica/analytics/ndkcrashes/jni/core/AppMetricaCrashpadConfig");
        getApkPathID = jniEnv->GetMethodID(jClass, "getApkPath", "()Ljava/lang/String;");
        getAppMetricaMetadataID = jniEnv->GetMethodID(jClass, "getAppMetricaMetadata", "()Ljava/lang/String;");
        getCrashFolderID = jniEnv->GetMethodID(jClass, "getCrashFolder", "()Ljava/lang/String;");
        getDataDirID = jniEnv->GetMethodID(jClass, "getDataDir", "()Ljava/lang/String;");
        getHandlerPathID = jniEnv->GetMethodID(jClass, "getHandlerPath", "()Ljava/lang/String;");
        getJavaHandlerClassNameID = jniEnv->GetMethodID(jClass, "getJavaHandlerClassName", "()Ljava/lang/String;");
        getLibPathID = jniEnv->GetMethodID(jClass, "getLibPath", "()Ljava/lang/String;");
        getSocketNameID = jniEnv->GetMethodID(jClass, "getSocketName", "()Ljava/lang/String;");
        is64bitID = jniEnv->GetMethodID(jClass, "is64bit", "()Z");
    }

    std::string AppMetricaCrashpadConfig::getApkPath() {
        return JNIUtils::toStdString(jniEnv, (jstring) jniEnv->CallObjectMethod(jObject, getApkPathID));
    }

    std::string AppMetricaCrashpadConfig::getAppMetricaMetadata() {
        return JNIUtils::toStdString(jniEnv, (jstring) jniEnv->CallObjectMethod(jObject, getAppMetricaMetadataID));
    }

    std::string AppMetricaCrashpadConfig::getCrashFolder() {
        return JNIUtils::toStdString(jniEnv, (jstring) jniEnv->CallObjectMethod(jObject, getCrashFolderID));
    }

    std::string AppMetricaCrashpadConfig::getDataDir() {
        return JNIUtils::toStdString(jniEnv, (jstring) jniEnv->CallObjectMethod(jObject, getDataDirID));
    }

    std::string AppMetricaCrashpadConfig::getHandlerPath() {
        return JNIUtils::toStdString(jniEnv, (jstring) jniEnv->CallObjectMethod(jObject, getHandlerPathID));
    }

    std::string AppMetricaCrashpadConfig::getJavaHandlerClassName() {
        return JNIUtils::toStdString(jniEnv, (jstring) jniEnv->CallObjectMethod(jObject, getJavaHandlerClassNameID));
    }

    std::string AppMetricaCrashpadConfig::getLibPath() {
        return JNIUtils::toStdString(jniEnv, (jstring) jniEnv->CallObjectMethod(jObject, getLibPathID));
    }

    std::string AppMetricaCrashpadConfig::getSocketName() {
        return JNIUtils::toStdString(jniEnv, (jstring) jniEnv->CallObjectMethod(jObject, getSocketNameID));
    }

    bool AppMetricaCrashpadConfig::is64bit() {
        return jniEnv->CallBooleanMethod(jObject, is64bitID);
    }

} // appmetrica
