package io.appmetrica.analytics.impl.location.stub

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor

internal class PermissionExtractorStub : PermissionExtractor {

    override fun hasPermission(context: Context, permission: String): Boolean = false
}
