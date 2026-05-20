package io.appmetrica.analytics.impl.id.reflection

import android.content.Context
import android.os.Bundle
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils.detectClassExists

internal class AdvIdentifiersProviderReflection(
    private val parser: ReflectionAdvIdParser = ReflectionAdvIdParser(),
) {

    private val clazz = "io.appmetrica.analytics.identifiers.internal.AdvIdentifiersProvider"
    private val method = "requestIdentifiers"

    fun isAvailable(): Boolean {
        return detectClassExists(clazz)
    }

    @Throws(Throwable::class)
    fun requestIdentifiers(context: Context, provider: String): AdTrackingInfoResult? {
        return requestIdentifiers(
            context,
            Bundle().apply {
                putString(Constants.PROVIDER, provider)
            }
        )?.let(parser::fromBundle)
    }

    private fun requestIdentifiers(context: Context, data: Bundle): Bundle? {
        val clazz = Class.forName(clazz)
        val method = clazz.getMethod(method, Context::class.java, Bundle::class.java)
        return method.invoke(null, context, data) as Bundle?
    }
}
