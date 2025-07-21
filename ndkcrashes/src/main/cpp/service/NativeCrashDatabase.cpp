#include "../utils/log.h"
#include "NativeCrashDatabase.h"

namespace appmetrica {

    NativeCrashDatabase::NativeCrashDatabase(const std::string &crashFolder) {
        LOGD("Init database in %s", crashFolder.c_str());
        database = crashpad::CrashReportDatabase::Initialize(base::FilePath(crashFolder));
    }

    bool NativeCrashDatabase::deleteCompletedReports() {
        std::vector<crashpad::CrashReportDatabase::Report> reports;
        crashpad::CrashReportDatabase::OperationStatus readResult = database->GetCompletedReports(&reports);
        bool noError = readResult == crashpad::CrashReportDatabase::OperationStatus::kNoError;
        if (noError) {
            LOGD("Found %zu completed reports for delete", reports.size());
            for (const auto &report: reports) {
                LOGD("Delete completed report with uuid %s", report.uuid.ToString().c_str());
                bool deleteResult = database->DeleteReport(report.uuid);
                noError &= deleteResult == crashpad::CrashReportDatabase::OperationStatus::kNoError;
            }
        }
        return noError;
    }

    bool NativeCrashDatabase::lookUpCrashReport(std::string &uuidStr, CrashpadUploadReport *uploadReport) {
        LOGD("Read native crash for %s", uuidStr.c_str());
        auto uuid = crashpad::UUID();
        uuid.InitializeFromString(base::StringPiece(uuidStr));

        crashpad::CrashReportDatabase::Report report;
        crashpad::CrashReportDatabase::OperationStatus status = database->LookUpCrashReport(uuid, &report);

        LOGD("Found native crash report for %s with error %d", uuidStr.c_str(), status);
        if (status == crashpad::CrashReportDatabase::OperationStatus::kNoError) {
            database->GetReportForUploading(uuid, uploadReport);
            return true;
        } else {
            return false;
        }
    }

    bool NativeCrashDatabase::lookUpCrashReports(std::vector<CrashpadUploadReport> &reports) {
        std::vector<crashpad::CrashReportDatabase::Report> localReports;
        if (database->GetPendingReports(&localReports) == crashpad::CrashReportDatabase::OperationStatus::kNoError) {
            LOGD("Found %zu native crashes", localReports.size());
            for (const auto &report: localReports) {
                CrashpadUploadReport uploadReport;
                LOGD("Read native crash for %s", report.uuid.ToString().c_str());
                bool readResult = database->GetReportForUploading(report.uuid, &uploadReport);
                if (readResult == crashpad::CrashReportDatabase::kNoError) {
                    reports.push_back(std::move(uploadReport));
                }
            }
        }
        return !reports.empty();
    }

    bool NativeCrashDatabase::markCrashCompleted(std::string &uuidStr) {
        auto uuid = crashpad::UUID();
        uuid.InitializeFromString(base::StringPiece(uuidStr));

        CrashpadUploadReport uploadReport;
        database->GetReportForUploading(uuid, &uploadReport);

        bool status = database->RecordUploadComplete(std::move(uploadReport), uuidStr);
        LOGD("Mark native crash %s with error %d", uuidStr.c_str(), status);
        return status == crashpad::CrashReportDatabase::OperationStatus::kNoError;
    }

} // appmetrica
