-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.adrevenue.other.impl'

-keep public class !io.appmetrica.analytics.adrevenue.other.impl.**, io.appmetrica.analytics.adrevenue.other.** {
    public *;
}
