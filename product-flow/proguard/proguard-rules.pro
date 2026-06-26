-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.productflow.impl'

-keep public class !io.appmetrica.analytics.productflow.impl.**, io.appmetrica.analytics.productflow.** {
    public *;
}
