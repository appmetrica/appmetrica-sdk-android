-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.modulesapi.impl'

-keep public class !io.appmetrica.analytics.modulesapi.impl.**, io.appmetrica.analytics.modulesapi.** {
    public *;
}
