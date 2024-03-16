package io.appmetrica.analytics.modulesapi.internal.service

import io.appmetrica.analytics.modulesapi.internal.common.TableDescription

interface ModuleServicesDatabase {

    val tables: List<TableDescription>
}
