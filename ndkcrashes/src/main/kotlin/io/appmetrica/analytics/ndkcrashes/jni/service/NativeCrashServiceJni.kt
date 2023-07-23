package io.appmetrica.analytics.ndkcrashes.jni.service

internal object NativeCrashServiceJni {
    external fun init(crashFolder: String)
    external fun readCrash(uuid: String): CrashpadCrash?
    external fun readAllCrashes(): List<CrashpadCrash>
    external fun markCrashCompleted(uuid: String): Boolean
    external fun deleteCompletedCrashes(): Boolean
}

// for tests
internal object NativeCrashServiceJniWrapper {
    @JvmStatic
    fun init(crashFolder: String): Unit = NativeCrashServiceJni.init(crashFolder)

    @JvmStatic
    fun readCrash(uuid: String): CrashpadCrash? = NativeCrashServiceJni.readCrash(uuid)

    @JvmStatic
    fun readAllCrashes(): List<CrashpadCrash> = NativeCrashServiceJni.readAllCrashes()

    @JvmStatic
    fun markCrashCompleted(uuid: String): Boolean = NativeCrashServiceJni.markCrashCompleted(uuid)

    @JvmStatic
    fun deleteCompletedCrashes(): Boolean = NativeCrashServiceJni.deleteCompletedCrashes()
}
