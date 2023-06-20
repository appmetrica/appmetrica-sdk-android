-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.impl'

-keep public class !io.appmetrica.analytics.impl.**, io.appmetrica.analytics.** {
    public *;
    protected *;
}

-keep class io.appmetrica.analytics.impl.ac.** {
    public static <methods>;
    native <methods>;
}
