-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.networklegacy.impl'

-keep public class !io.appmetrica.analytics.networklegacy.impl.**, io.appmetrica.analytics.networklegacy.** {
    public *;
}
