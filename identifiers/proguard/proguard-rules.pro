-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.identifiers.impl'

-keep class !io.appmetrica.analytics.identifiers.impl.**, io.appmetrica.analytics.identifiers.** {
    public *;
}
