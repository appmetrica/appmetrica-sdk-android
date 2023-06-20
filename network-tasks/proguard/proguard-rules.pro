-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.networktasks.impl'

-keep public class !io.appmetrica.analytics.networktasks.impl.**, io.appmetrica.analytics.networktasks.** {
    public *;
    protected *;
}
