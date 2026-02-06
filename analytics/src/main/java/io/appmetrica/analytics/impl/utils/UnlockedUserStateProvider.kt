package io.appmetrica.analytics.impl.utils

import android.content.Context
import android.os.Build
import android.os.UserManager
import androidx.annotation.RequiresApi
import io.appmetrica.analytics.coreutils.internal.AndroidUtils.isApiAchieved
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils

internal class UnlockedUserStateProvider {
    @SuppressWarnings("NewApi")
    fun isUserUnlocked(context: Context): Boolean = if (isApiAchieved(Build.VERSION_CODES.N)) {
        isUserUnlockedStateSinceN(context)
    } else true

    @RequiresApi(Build.VERSION_CODES.N)
    fun isUserUnlockedStateSinceN(context: Context): Boolean {
        val userManager = context.getSystemService(UserManager::class.java)
        return SystemServiceUtils.accessSystemServiceSafelyOrDefault<UserManager?, Boolean?>(
            userManager,
            "detect unlocked user state",
            "User manager",
            true
        ) { input -> input.isUserUnlocked } ?: true
    }
}
