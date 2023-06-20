-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.billingv4.impl'

-keep public class !io.appmetrica.analytics.billingv4.impl.**, io.appmetrica.analytics.billingv4.** {
    public *;
}
