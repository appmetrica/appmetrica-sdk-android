package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.IComponent;
import io.appmetrica.analytics.impl.component.IReportableComponent;
import io.appmetrica.analytics.impl.startup.StartupUnit;

public interface ComponentUnitFactory<C extends IReportableComponent & IComponent> {

    @NonNull
    C createComponentUnit(@NonNull Context context,
                          @NonNull ComponentId componentId,
                          @NonNull CommonArguments.ReporterArguments sdkConfig,
                          @NonNull StartupUnit startupUnit);
}
