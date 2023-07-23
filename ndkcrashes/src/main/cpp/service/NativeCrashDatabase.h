#ifndef APPMETRICA_SDK_NATIVECRASHDATABASE_H
#define APPMETRICA_SDK_NATIVECRASHDATABASE_H

#include <string>

#include "client/crash_report_database.h"

namespace appmetrica {

    typedef std::unique_ptr<const crashpad::CrashReportDatabase::UploadReport> CrashpadUploadReport;

    class NativeCrashDatabase {
    public:
        NativeCrashDatabase(const std::string &crashFolder);

        bool deleteCompletedReports();

        bool lookUpCrashReport(std::string &uuid, CrashpadUploadReport *uploadReport);

        bool lookUpCrashReports(std::vector<CrashpadUploadReport> &reports);

        bool markCrashCompleted(std::string &uuid);

    private:
        std::unique_ptr<crashpad::CrashReportDatabase> database;
    };

} // appmetrica

#endif //APPMETRICA_SDK_NATIVECRASHDATABASE_H
