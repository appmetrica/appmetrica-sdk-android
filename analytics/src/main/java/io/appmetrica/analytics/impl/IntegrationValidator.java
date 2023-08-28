package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.internal.CounterConfiguration;

public class IntegrationValidator {

    /**
     * Checks the validity of the entire configuration for AppMetrica.
     * @throws IllegalStateException
     */
    public static void checkValidityOfAppMetricaConfiguration() {
        checkImportantAppMetricaClasses();
    }

    /**
     * Checks/Tests accessibility of some important classes for AppMetrica.
     * @throws IllegalStateException
     */
    public static void checkImportantAppMetricaClasses() throws IllegalStateException {
        if (!isClassExisting(CounterConfiguration.ORIGINAL_CLASS_PATH)) {
            throw new IntegrationException(
                    "\nClass " + CounterConfiguration.ORIGINAL_CLASS_PATH + " isn't found.\n" +
                    "Perhaps this is due to obfuscation.\n" +
                    "If you build your application with ProGuard,\n" + "" +
                    "you need to keep classes of AppMetrica SDK.\n" +
                    "Please try to use the following lines of code:\n" +
                    "##########################################\n" +
                    "-keep class io.appmetrica.analytics.** { *; }\n" +
                    "-dontwarn io.appmetrica.analytics.**\n" +
                    "##########################################"
            );
        }
    }

    /**
     * Checks that class is existing.
     */
    public static boolean isClassExisting(final String className) {
        boolean resultExistence = true;

        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            resultExistence = false;
        }

        return resultExistence;
    }

    static class IntegrationException extends RuntimeException {
        public IntegrationException(final String msg) {
            super(msg);
        }
    }
}
