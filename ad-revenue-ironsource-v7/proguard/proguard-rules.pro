-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.adrevenue.ironsource.v7.impl'

-keep public class !io.appmetrica.analytics.adrevenue.ironsource.v7.impl.**, io.appmetrica.analytics.adrevenue.ironsource.v7.** {
    public *;
}
