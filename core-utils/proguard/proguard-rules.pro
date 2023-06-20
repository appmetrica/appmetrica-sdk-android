-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.coreutils.impl'

-keep public class !io.appmetrica.analytics.coreutils.impl.**, io.appmetrica.analytics.coreutils.** {
    public protected *;
}
