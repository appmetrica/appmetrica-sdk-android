-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.coreapi.impl'

-keep public class !io.appmetrica.analytics.coreapi.impl.**, io.appmetrica.analytics.coreapi.** {
    public *;
}

-keep,allowobfuscation @io.appmetrica.analytics.coreapi.internal.annotations.DoNotInline class * {
    *;
}
