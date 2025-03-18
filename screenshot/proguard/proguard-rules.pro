-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.screenshot.impl'

-keep public class !io.appmetrica.analytics.screenshot.impl.**, io.appmetrica.analytics.screenshot.** {
    public *;
}
