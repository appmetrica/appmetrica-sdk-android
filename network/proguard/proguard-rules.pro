-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.network.impl'

-keep public class !io.appmetrica.analytics.network.impl.**, io.appmetrica.analytics.network.** {
    public *;
}
