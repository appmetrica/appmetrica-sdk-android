-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.apphud.impl'

-keep public class !io.appmetrica.analytics.apphud.impl.**, io.appmetrica.analytics.apphud.** {
    public *;
}
