-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.billingv6.impl'

-keep public class !io.appmetrica.analytics.billingv6.impl.**, io.appmetrica.analytics.billingv6.** {
    public *;
}
