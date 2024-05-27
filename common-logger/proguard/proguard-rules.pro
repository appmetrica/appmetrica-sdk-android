-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.logger.common.impl'

-keep public class !io.appmetrica.analytics.logger.common.impl.**, io.appmetrica.analytics.logger.common.** {
    public protected *;
}
