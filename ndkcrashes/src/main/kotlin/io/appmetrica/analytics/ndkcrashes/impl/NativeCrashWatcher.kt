package io.appmetrica.analytics.ndkcrashes.impl

import android.content.Context
import android.net.LocalServerSocket
import java.util.LinkedList

class NativeCrashWatcher(private val socketName: String) {
    interface Listener {
        fun onNewCrash(uuid: String)
    }

    private val tag = "[NativeCrashWatcher]"

    private var socket: LocalServerSocket? = null

    @Volatile
    private var isRunning = false

    private val subscribers: MutableList<Listener> = LinkedList()

    private val watcherThread: Thread = object : Thread() {
        override fun run() {
            while (isRunning) {
                try {
                    val buff = ByteArray(256)
                    val bytes = socket!!.accept().use { client ->
                        client.inputStream.read(buff)
                    }
                    val stringData = ByteArray(bytes)
                    System.arraycopy(buff, 0, stringData, 0, bytes)
                    onNewCrash(String(stringData))
                } catch (e: Throwable) {
                    NativeCrashLogger.error(tag, "error reading data", e)
                }
            }
        }
    }

    fun subscribe(crashConsumer: Listener) {
        NativeCrashLogger.debug(tag, "start watcher")
        synchronized(this) { subscribers.add(crashConsumer) }
        if (!isRunning) {
            synchronized(this) {
                // close() does not interrupt accept() and this name become "forever in use"
                if (!isRunning) {
                    try {
                        NativeCrashLogger.debug(tag, "open socket")
                        socket = LocalServerSocket(socketName)
                        isRunning = true
                        watcherThread.start()
                    } catch (exception: Throwable) {
                        NativeCrashLogger.error(tag, "can't start crashpad socket", exception)
                    }
                }
            }
        }
    }

    @Synchronized
    fun unsubscribe(crashConsumer: Listener) {
        subscribers.remove(crashConsumer)
    }

    // Synchronize the whole onNewCrash() method and unsubscribe() to leave only two scenarios:
    // pause ComonentUnits destroying and send crash to the current session or just skip it.
    @Synchronized
    private fun onNewCrash(uuid: String) {
        NativeCrashLogger.debug(tag, "deliver new native crash from crashpad $uuid to ${subscribers.size} subscribers")
        for (consumer in subscribers) {
            consumer.onNewCrash(uuid)
        }
    }

    companion object {
        private const val SOCKET_PREFIX = "-appmetrica-crashpad_socket"

        @JvmStatic // for tests
        fun getSocketName(context: Context): String = context.packageName + SOCKET_PREFIX
    }
}
