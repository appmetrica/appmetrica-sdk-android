-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.adrevenue.impl'

-keep public class !io.appmetrica.analytics.adrevenue.impl.**, io.appmetrica.analytics.adrevenue.** {
    public *;
}
