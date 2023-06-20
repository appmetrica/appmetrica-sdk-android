package io.appmetrica.analytics.impl.utils;

import android.content.Context;
import android.os.Build;
import android.os.UserManager;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.backport.FunctionWithThrowable;
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils;

public class UnlockedUserStateProvider {

    public boolean isUserUnlocked(@NonNull Context context) {
        boolean unlocked = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            UserManager userManager = context.getSystemService(UserManager.class);
            unlocked = SystemServiceUtils.accessSystemServiceSafelyOrDefault(
                userManager,
                "detect unlocked user state",
                "User manager",
                true,
                new FunctionWithThrowable<UserManager, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull UserManager input) throws Throwable {
                        return input.isUserUnlocked();
                    }
                }
            );
        }
        return unlocked;
    }

}
