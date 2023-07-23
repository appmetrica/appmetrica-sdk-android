-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.ndkcrashesapi.impl'

-keep public class !io.appmetrica.analytics.ndkcrashesapi.impl.**, io.appmetrica.analytics.ndkcrashesapi.** {
    public *;
}
