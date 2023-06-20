-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.billinginterface.impl'

-keep public class !io.appmetrica.analytics.billinginterface.impl.**, io.appmetrica.analytics.billinginterface.** {
    public *;
}
