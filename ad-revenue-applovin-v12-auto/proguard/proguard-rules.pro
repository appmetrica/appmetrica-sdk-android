-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl'

-keep public class !io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.**, io.appmetrica.analytics.adrevenue.applovin.v12.auto.** {
    public *;
}
