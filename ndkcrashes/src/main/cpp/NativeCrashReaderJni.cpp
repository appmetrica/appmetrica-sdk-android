#include <jni.h>
#include "client/crashpad_client.h"
#include "client/crash_report_database.h"
#include <filesystem>
#include <sstream>
#include "AppmetricaCrashpadConfig.h"
#include "NativeCrashReader.h"

constexpr char kArgumentUuid[] = "arg_ui";
constexpr char kArgumentDumpFile[] = "arg_df";
constexpr char kArgumentCreationTime[] = "arg_ct";

using namespace appmetrica;
using namespace crashpad;
using namespace std;

static NativeCrashReader *crashesReader = nullptr;

void crashToBundle(BundleWrapper& bundle, const unique_ptr<const CrashReportDatabase::UploadReport>& uploadReport) {
    bundle.putString(kArgumentDumpFile, uploadReport->file_path.value());
    bundle.putLong(kArgumentCreationTime, uploadReport->creation_time);
    bundle.putString(kArgumentClientDescription, uploadReport->clientDescription);
    bundle.putString(kArgumentRuntimeConfig, uploadReport->runtimeConfig);
}

extern "C" {

    JNIEXPORT void JNICALL Java_io_appmetrica_analytics_impl_ac_CrashpadServiceHelper_setUpServiceHelper(
            JNIEnv *env, jclass obj, jstring folder
    ) {
        // Create descriptor for dumps.
        const char *path = env->GetStringUTFChars(folder, 0);
        string nativeCrashDir = string(path);

        crashesReader = new NativeCrashReader(nativeCrashDir);

        env->ReleaseStringUTFChars(folder, path);
    }

    JNIEXPORT void JNICALL
    Java_io_appmetrica_analytics_impl_ac_CrashpadServiceHelper_cancelSetUpServiceHelper(
            JNIEnv *env, jclass obj
    ) {
        delete crashesReader;
    }

    JNIEXPORT jobject JNICALL Java_io_appmetrica_analytics_impl_ac_CrashpadServiceHelper_readCrash(
            JNIEnv *env, jclass obj, jstring uuid
    ) {
        string uuidString = readString(env, uuid);

        std::unique_ptr<const CrashReportDatabase::UploadReport> uploadReport;

        if (crashesReader->lookUpCrashReport(uuidString, &uploadReport)) {
            auto bundle = BundleWrapper(env);
            crashToBundle(bundle, uploadReport);
            return bundle.object;
        } else {
            return nullptr;
        }
    }

    JNIEXPORT jobject JNICALL Java_io_appmetrica_analytics_impl_ac_CrashpadServiceHelper_readOldCrashes(
            JNIEnv *env, jclass obj
    ) {
        vector<unique_ptr<const CrashReportDatabase::UploadReport>> reports;
        crashesReader->lookUpCrashReports(reports);

        ArrayListWrapper list(env, reports.size());

        for (const auto& report: reports) {
            auto bundle = BundleWrapper(env);
            crashToBundle(bundle, report);
            bundle.putString(kArgumentUuid, report->uuid.ToString());
            list.add(bundle.object);
        }

        return list.object;
    }

    JNIEXPORT jboolean JNICALL
    Java_io_appmetrica_analytics_impl_ac_CrashpadServiceHelper_markCrashCompleted(
            JNIEnv *env, jclass obj, jstring uuid
    ) {
        CrashReportDatabase::Report report;
        const char *uuidCstr = env->GetStringUTFChars(uuid, 0);
        string uuidString = string(uuidCstr);

        jboolean result = crashesReader->markCrashCompleted(uuidString);

        env->ReleaseStringUTFChars(uuid, uuidCstr);
        return result;
    }

    JNIEXPORT jboolean JNICALL
    Java_io_appmetrica_analytics_impl_ac_CrashpadServiceHelper_deleteCompletedReports(
            JNIEnv *env, jclass obj
    ) {
        return crashesReader->deleteCompletedReports();
    }

}
