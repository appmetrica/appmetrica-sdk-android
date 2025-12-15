-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.networkapi.impl'

-keep public class !io.appmetrica.analytics.networkapi.impl.**, io.appmetrica.analytics.networkapi.** {
    public *;
    protected *;
}
