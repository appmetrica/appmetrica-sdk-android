package io.appmetrica.analytics.impl.component;

import androidx.annotation.VisibleForTesting;
import java.util.LinkedList;
import java.util.List;

public class ComponentMigrationHelper {

    public static class Creator {

        ComponentMigrationHelper create() {
            return new ComponentMigrationHelper();
        }
    }

    private List<MigrationScript> mMigrationScripts;

    private ComponentMigrationHelper() {
        initMigrationScripts();
    }

    private void initMigrationScripts() {
        mMigrationScripts = new LinkedList<>();
    }

    void checkMigration() {
        for (MigrationScript script : mMigrationScripts) {
            script.checkMigration();
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    List<MigrationScript> getMigrationScripts() {
        return mMigrationScripts;
    }
}
