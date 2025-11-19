-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.adrevenue.ironsource.v9.impl'

-keep public class !io.appmetrica.analytics.adrevenue.ironsource.v9.impl.**, io.appmetrica.analytics.adrevenue.ironsource.v9.** {
    public *;
}
