#ifndef APPMETRICA_SDK_NATIVECRASHREADER_H
#define APPMETRICA_SDK_NATIVECRASHREADER_H

#include <string>
#include <unordered_set>
#include <vector>

#include "client/crash_report_database.h"

namespace appmetrica {

    class NativeCrashReader {

    public:
        NativeCrashReader(std::string& path);

        bool lookUpCrashReport(std::string& uuid, std::unique_ptr<const crashpad::CrashReportDatabase::UploadReport>* uploadReport);
        bool lookUpCrashReports(std::vector<std::unique_ptr<const crashpad::CrashReportDatabase::UploadReport>>& reports);
        bool markCrashCompleted(std::string& uuid);
        bool deleteCompletedReports();

    private:
        std::unique_ptr<crashpad::CrashReportDatabase> database;
    };

}

#endif //APPMETRICA_SDK_NATIVECRASHREADER_H
