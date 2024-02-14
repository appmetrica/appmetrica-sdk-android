-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.logger.impl'

-keep public class !io.appmetrica.analytics.logger.impl.**, io.appmetrica.analytics.logger.** {
    public protected *;
}
