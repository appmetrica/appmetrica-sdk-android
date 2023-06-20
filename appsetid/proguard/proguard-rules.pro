-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.appsetid.impl'

-keep public class !io.appmetrica.analytics.appsetid.impl.**, io.appmetrica.analytics.appsetid.** {
    public *;
}
