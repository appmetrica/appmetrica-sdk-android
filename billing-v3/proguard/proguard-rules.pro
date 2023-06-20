-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.billingv3.impl'

-keep public class !io.appmetrica.analytics.billingv3.impl.**, io.appmetrica.analytics.billingv3.** {
    public *;
}
