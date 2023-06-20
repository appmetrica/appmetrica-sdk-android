package io.appmetrica.analytics.impl

import android.content.Context

internal class StringArrayResourceRetriever(context: Context, resourceName: String) :
    ResourceRetriever<Array<String?>>(context, resourceName, "array") {

    override fun callAppropriateMethod(resourceId: Int): Array<String?>? {
        return mContext.resources.getStringArray(resourceId)
    }
}
