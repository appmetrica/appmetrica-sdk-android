#include <jni.h>

#include "../utils/ArrayList.h"
#include "../utils/JNIUtils.h"
#include "../utils/log.h"
#include "CrashpadCrash.h"
#include "NativeCrashDatabase.h"

static appmetrica::NativeCrashDatabase *database = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_io_appmetrica_analytics_ndkcrashes_jni_service_NativeCrashServiceJni_init(
        JNIEnv *env, jobject thiz, jstring jCrashFolder) {
    LOGD("NativeCrashServiceJni.init");
    database = new appmetrica::NativeCrashDatabase(appmetrica::JNIUtils::toStdString(env, jCrashFolder));
}

extern "C"
JNIEXPORT jobject JNICALL
Java_io_appmetrica_analytics_ndkcrashes_jni_service_NativeCrashServiceJni_readCrash(
        JNIEnv *env, jobject thiz, jstring jUuid) {
    LOGD("NativeCrashServiceJni.readCrash");
    std::string uuid = appmetrica::JNIUtils::toStdString(env, jUuid);

    appmetrica::CrashpadUploadReport uploadReport;
    if (database && database->lookUpCrashReport(uuid, &uploadReport)) {
        appmetrica::CrashpadCrash crash(env, uploadReport->uuid.ToString(), uploadReport->file_path.value(),
                                        uploadReport->creation_time, uploadReport->appMetricaMetadata);
        LOGD("NativeCrashServiceJni.readCrash return crash %s", uuid.c_str());
        return crash.jObject;
    } else {
        auto hasDatabase = database != nullptr ? "true" : "false";
        LOGD("NativeCrashServiceJni.readCrash not found crash; has database = %s", hasDatabase);
        return nullptr;
    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_io_appmetrica_analytics_ndkcrashes_jni_service_NativeCrashServiceJni_readAllCrashes(
        JNIEnv *env, jobject thiz) {
    LOGD("NativeCrashServiceJni.readAllCrashes");
    std::vector<appmetrica::CrashpadUploadReport> reports;

    if (database) {
        database->lookUpCrashReports(reports);

        appmetrica::ArrayList crashes(env, (int) reports.size());

        for (const auto &report: reports) {
            appmetrica::CrashpadCrash crash(env, report->uuid.ToString(), report->file_path.value(),
                                            report->creation_time, report->appMetricaMetadata);
            crashes.add(crash.jObject);
        }

        LOGD("NativeCrashServiceJni.readAllCrashes return %d crashes", reports.size());
        return crashes.jObject;
    } else {
        LOGD("NativeCrashServiceJni.readAllCrashes return 0 crashes. Database is not initialized");
        return appmetrica::ArrayList(env, 0).jObject;
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_appmetrica_analytics_ndkcrashes_jni_service_NativeCrashServiceJni_markCrashCompleted(
        JNIEnv *env, jobject thiz, jstring jUuid) {
    LOGD("NativeCrashServiceJni.markCrashCompleted");
    std::string uuid = appmetrica::JNIUtils::toStdString(env, jUuid);
    bool result = database && database->markCrashCompleted(uuid);
    LOGD("NativeCrashServiceJni.markCrashCompleted return %s", result ? "true" : "false");
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_appmetrica_analytics_ndkcrashes_jni_service_NativeCrashServiceJni_deleteCompletedCrashes(
        JNIEnv *env, jobject thiz) {
    LOGD("NativeCrashServiceJni.deleteCompletedCrashes");
    bool result = database && database->deleteCompletedReports();
    LOGD("NativeCrashServiceJni.deleteCompletedCrashes return %s", result ? "true" : "false");
    return result ? JNI_TRUE : JNI_FALSE;
}
