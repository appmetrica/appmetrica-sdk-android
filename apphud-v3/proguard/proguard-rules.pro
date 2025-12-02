-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.apphudv3.impl'

-keep public class !io.appmetrica.analytics.apphudv3.impl.**, io.appmetrica.analytics.apphudv3.** {
    public *;
}
