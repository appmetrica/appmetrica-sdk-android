-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.logger.appmetrica.impl'

-keep public class !io.appmetrica.analytics.logger.appmetrica.impl.**, io.appmetrica.analytics.logger.appmetrica.** {
    public protected *;
}
