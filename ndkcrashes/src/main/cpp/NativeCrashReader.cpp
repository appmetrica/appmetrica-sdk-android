#include <jni.h>
#include "NativeCrashReader.h"

using namespace base;
using namespace crashpad;
using namespace std;

namespace appmetrica {

    NativeCrashReader::NativeCrashReader(string &path) {
        base::FilePath dbPath = base::FilePath(path);
        database = CrashReportDatabase::Initialize(dbPath);
    }

    bool NativeCrashReader::lookUpCrashReport(string& uuid, unique_ptr<const CrashReportDatabase::UploadReport>* uploadReport) {
        UUID id = UUID();
        id.InitializeFromString(StringPiece(uuid));

        CrashReportDatabase::Report report;
        CrashReportDatabase::OperationStatus status = database->LookUpCrashReport(id, &report);

        if (status == CrashReportDatabase::OperationStatus::kNoError) {
            database->GetReportForUploading(id, uploadReport);
            return true;
        } else {
            return false;
        }
    }

    bool NativeCrashReader::markCrashCompleted(string& uuid) {
        UUID id = UUID();
        id.InitializeFromString(StringPiece(uuid));
        std::unique_ptr<const CrashReportDatabase::UploadReport> uploadReport;
        database->GetReportForUploading(id, &uploadReport);
        return database->RecordUploadComplete(std::move(uploadReport), uuid) ==
               CrashReportDatabase::OperationStatus::kNoError;
    }

    bool NativeCrashReader::deleteCompletedReports() {
        vector<CrashReportDatabase::Report> reports;
        CrashReportDatabase::OperationStatus readResult = database->GetCompletedReports(&reports);
        bool noError = readResult == CrashReportDatabase::OperationStatus::kNoError;
        if (noError) {
            for (const auto &report: reports) {
                noError &= database->DeleteReport(report.uuid) ==
                           CrashReportDatabase::OperationStatus::kNoError;
            }
        }
        return noError;
    }

    bool NativeCrashReader::lookUpCrashReports(
            vector<unique_ptr<const CrashReportDatabase::UploadReport>>& reports
    ) {
        vector<CrashReportDatabase::Report> localReports;
        if (database->GetPendingReports(&localReports) == CrashReportDatabase::OperationStatus::kNoError) {
            for (const auto &rep: localReports) {
                unique_ptr<const CrashReportDatabase::UploadReport> report;
                if (database->GetReportForUploading(rep.uuid, &report) ==
                    crashpad::CrashReportDatabase::kNoError) {
                    reports.push_back(move(report));
                }
            }
        }
        return !reports.empty();
    }
}
