#ifndef APPMETRICA_SDK_APPPROCESSCONFIG_H
#define APPMETRICA_SDK_APPPROCESSCONFIG_H

#include <string>

#include "JniUtil.h"

namespace appmetrica {

    constexpr char kArgumentMainClass[] = "arg_mc";
    constexpr char kArgumentApkPath[] = "arg_akp";
    constexpr char kArgumentLibPath[] = "arg_lp";
    constexpr char kArgumentDataDirectory[] = "arg_dp";

    class AppProcessConfig {

        public:
            const std::string mainClass;
            const std::string apkPath;
            const std::string libPath;
            const std::string dataDir;

            AppProcessConfig(BundleWrapper& bundle);

    };
}


#endif //APPMETRICA_SDK_APPPROCESSCONFIG_H
