package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class AppProcessConfigProvider {

    @Nullable
    public AppProcessConfig provideAppConfig(@NonNull Context context, @Nullable String abi) {
        try {
            String[] paths = PackagePaths.makePackagePaths(context, abi);

            return new AppProcessConfig(paths[0], paths[1], Environment.getDataDirectory().getAbsolutePath() );
        } catch (Throwable ignored) {
            return null;
        }
    }

}
