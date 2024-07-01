-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.adrevenue.applovin.v12.impl'

-keep public class !io.appmetrica.analytics.adrevenue.applovin.v12.impl.**, io.appmetrica.analytics.adrevenue.applovin.v12.** {
    public *;
}
