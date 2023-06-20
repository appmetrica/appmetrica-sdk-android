package io.appmetrica.analytics.appsetid.internal

import android.content.Context
import com.google.android.gms.appset.AppSet
import com.google.android.gms.appset.AppSetIdInfo
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope

class AppSetIdRetriever
@Throws(Throwable::class) constructor() : IAppSetIdRetriever {

    private val listenersLock = Object()
    private val listeners = mutableListOf<OnCompleteListener<AppSetIdInfo>>()

    @Throws(Throwable::class)
    override fun retrieveAppSetId(context: Context, listener: AppSetIdListener) {
        val client = AppSet.getClient(context)
        val task: Task<AppSetIdInfo> = client.appSetIdInfo
        val onCompleteListener = object : OnCompleteListener<AppSetIdInfo> {
            override fun onComplete(completedTask: Task<AppSetIdInfo>) {
                synchronized(listenersLock) {
                    listeners.remove(this)
                }
                if (completedTask.isSuccessful) {
                    listener.onAppSetIdRetrieved(completedTask.result.id, convertScope(completedTask.result.scope))
                } else {
                    listener.onFailure(completedTask.exception)
                }
            }
        }
        synchronized(listenersLock) {
            listeners.add(onCompleteListener)
        }
        task.addOnCompleteListener(onCompleteListener)
    }

    private fun convertScope(scope: Int): AppSetIdScope {
        return when (scope) {
            AppSetIdInfo.SCOPE_APP -> AppSetIdScope.APP
            AppSetIdInfo.SCOPE_DEVELOPER -> AppSetIdScope.DEVELOPER
            else -> AppSetIdScope.UNKNOWN
        }
    }
}
