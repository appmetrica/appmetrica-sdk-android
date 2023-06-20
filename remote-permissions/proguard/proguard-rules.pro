-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.remotepermissions.impl'

-keep public class !io.appmetrica.analytics.remotepermissions.impl.**, io.appmetrica.analytics.remotepermissions.** {
    public *;
}
