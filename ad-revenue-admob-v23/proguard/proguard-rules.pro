-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.adrevenue.admob.v23.impl'

-keep public class !io.appmetrica.analytics.adrevenue.admob.v23.impl.**, io.appmetrica.analytics.adrevenue.admob.v23.** {
    public *;
}
