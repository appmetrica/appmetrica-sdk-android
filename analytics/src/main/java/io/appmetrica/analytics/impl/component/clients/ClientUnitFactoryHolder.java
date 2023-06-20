package io.appmetrica.analytics.impl.component.clients;

import androidx.annotation.NonNull;

public class ClientUnitFactoryHolder {

    @NonNull
    public ClientUnitFactory getClientUnitFactory(@NonNull ClientDescription clientDescription) {
        ClientUnitFactory factory;
        switch (clientDescription.getReporterType()) {
            case COMMUTATION:
                factory = new MainCommutationClientUnitFactory();
                break;
            case MAIN:
                factory = new MainReporterClientFactory();
                break;
            case SELF_DIAGNOSTIC_MAIN:
                factory = new SelfDiagnosticMainClientUnitFactory();
                break;
            case SELF_DIAGNOSTIC_MANUAL:
                factory = new SelfDiagnosticReporterClientUnitFactory();
                break;
            case MANUAL:
                factory = new ReporterClientUnitFactory();
                break;
            case SELF_SDK:
                factory = new SelfSdkReportingFactory();
                break;
            default:
                factory = new MainReporterClientFactory();
        }
        return factory;
    }

}
