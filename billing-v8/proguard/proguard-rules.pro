-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.billingv8.impl'

-keep public class !io.appmetrica.analytics.billingv8.impl.**, io.appmetrica.analytics.billingv8.** {
    public *;
}
