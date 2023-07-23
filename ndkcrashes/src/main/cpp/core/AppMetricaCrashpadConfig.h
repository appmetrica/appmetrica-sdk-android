#ifndef APPMETRICA_SDK_APPMETRICACRASHPADCONFIG_H
#define APPMETRICA_SDK_APPMETRICACRASHPADCONFIG_H

#include <jni.h>
#include <string>

namespace appmetrica {

    class AppMetricaCrashpadConfig {
    public:
        AppMetricaCrashpadConfig(JNIEnv *jniEnv, jobject jConfig);

        std::string getApkPath();

        std::string getAppMetricaMetadata();

        std::string getCrashFolder();

        std::string getDataDir();

        std::string getHandlerPath();

        std::string getJavaHandlerClassName();

        std::string getLibPath();

        std::string getSocketName();

        bool is64bit();

    private:
        JNIEnv *jniEnv;
        const jobject jObject;

        jmethodID getApkPathID;
        jmethodID getAppMetricaMetadataID;
        jmethodID getCrashFolderID;
        jmethodID getDataDirID;
        jmethodID getHandlerPathID;
        jmethodID getJavaHandlerClassNameID;
        jmethodID getLibPathID;
        jmethodID getSocketNameID;
        jmethodID is64bitID;
    };

} // appmetrica

#endif //APPMETRICA_SDK_APPMETRICACRASHPADCONFIG_H
