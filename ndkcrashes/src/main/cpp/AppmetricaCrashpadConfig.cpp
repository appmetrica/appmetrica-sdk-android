#include "AppmetricaCrashpadConfig.h"

using namespace std;

namespace appmetrica {

    AppmetricaCrashpadConfig::AppmetricaCrashpadConfig(BundleWrapper& bundle):
            handlerPath(bundle.getString(kArgumentHandlerPath)), dumpDirectory(bundle.getString(kArgumentDumpDir)),
            socketName(bundle.getString(kArgumentSocketName)),
            clientDescription(bundle.getString(kArgumentClientDescription)),
            useLinker(bundle.getBoolean(kArgumentUseLinker, false)),
            useAppProcess(bundle.getBoolean(kArgumentUseAppProcess, false)),
            is64bit(bundle.getBoolean(kArgumentIs64Bit, false)) {}
}
