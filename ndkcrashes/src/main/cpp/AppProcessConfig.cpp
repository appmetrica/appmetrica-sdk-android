#include "AppProcessConfig.h"

appmetrica::AppProcessConfig::AppProcessConfig(appmetrica::BundleWrapper &bundle):
    mainClass(bundle.getString(kArgumentMainClass)), apkPath(bundle.getString(kArgumentApkPath)),
    libPath(bundle.getString(kArgumentLibPath)), dataDir(bundle.getString(kArgumentDataDirectory)) {}
