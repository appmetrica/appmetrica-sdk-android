package io.appmetrica.analytics.impl.component;

abstract class MigrationScript {

    private final ComponentUnit mComponentUnit;

    MigrationScript(ComponentUnit componentUnit) {
        mComponentUnit = componentUnit;
    }

    ComponentUnit getComponent() {
        return mComponentUnit;
    }

    void checkMigration() {
        if (shouldMigrate()) {
            migrate();
        }
    }

    protected abstract boolean shouldMigrate();

    protected abstract void migrate();
}
