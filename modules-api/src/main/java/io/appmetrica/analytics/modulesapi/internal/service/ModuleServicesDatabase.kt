package io.appmetrica.analytics.modulesapi.internal.service

import io.appmetrica.analytics.modulesapi.internal.common.TableDescription

abstract class ModuleServicesDatabase {

    abstract val tables: List<TableDescription>
}
