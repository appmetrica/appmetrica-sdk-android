-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.adrevenue.fyber.v3.impl'

-keep public class !io.appmetrica.analytics.adrevenue.fyber.v3.impl.**, io.appmetrica.analytics.adrevenue.fyber.v3.** {
    public *;
}
