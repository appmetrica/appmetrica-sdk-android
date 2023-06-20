#ifndef APPMETRICA_SDK_APPMETRICACRASHPADCONFIG_H
#define APPMETRICA_SDK_APPMETRICACRASHPADCONFIG_H

#include <string>
#include <unordered_map>
#include "JniUtil.h"

namespace appmetrica {

    constexpr char kArgumentDumpDir[] = "arg_dd";
    constexpr char kArgumentHandlerPath[] = "arg_hp";
    constexpr char kArgumentUseLinker[] = "arg_ul";
    constexpr char kArgumentUseAppProcess[] = "arg_ap";
    constexpr char kArgumentIs64Bit[] = "arg_i64";
    constexpr char kArgumentSocketName[] = "arg_sn";

    constexpr char kArgumentClientDescription[] = "arg_cd";
    constexpr char kArgumentRuntimeConfig[] = "arg_rc";

    class AppmetricaCrashpadConfig {

    public:
        AppmetricaCrashpadConfig(BundleWrapper& bundle);

        const std::string clientDescription;
        const std::string handlerPath;
        const std::string dumpDirectory;
        const std::string socketName;
        const bool useLinker;
        const bool useAppProcess;
        const bool is64bit;
    };
}

#endif //APPMETRICA_SDK_APPMETRICACRASHPADCONFIG_H
