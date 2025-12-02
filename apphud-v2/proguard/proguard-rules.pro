-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.apphudv2.impl'

-keep public class !io.appmetrica.analytics.apphudv2.impl.**, io.appmetrica.analytics.apphudv2.** {
    public *;
}
