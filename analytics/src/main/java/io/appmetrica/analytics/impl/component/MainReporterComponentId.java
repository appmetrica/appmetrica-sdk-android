package io.appmetrica.analytics.impl.component;

public class MainReporterComponentId extends ComponentId {

    public MainReporterComponentId(final String packageName, final String apiKey) {
        super(packageName, apiKey);
    }

    @Override
    public String toString() {
        return getPackage();
    }

    @Override
    public String toStringAnonymized() {
        return toString();
    }

    @Override
    public boolean isMain() {
        return true;
    }
}
