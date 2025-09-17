package fakes

import io.modelcontextprotocol.kotlin.sdk.JSONRPCMessage
import io.modelcontextprotocol.kotlin.sdk.shared.Transport

class FakeTransport : Transport {

    private var messageHandler: (suspend (JSONRPCMessage) -> Unit)? = null
    private var closeHandler: (() -> Unit)? = null
    private var errorHandler: ((Throwable) -> Unit)? = null
    private var isStarted = false
    private var isClosed = false

    var linkedTransport: FakeTransport? = null

    companion object {
        fun createLinkedPair(): Pair<FakeTransport, FakeTransport> {
            val transport1 = FakeTransport()
            val transport2 = FakeTransport()
            transport1.linkedTransport = transport2
            transport2.linkedTransport = transport1
            return Pair(transport1, transport2)
        }
    }

    override suspend fun start() {
        isStarted = true
    }

    override suspend fun send(message: JSONRPCMessage) {
        if (isClosed) return

        linkedTransport?.let { linked ->
            if (!linked.isClosed) {
                if (linked.isStarted) {
                    try {
                        linked.messageHandler?.invoke(message)
                    } catch (exception: Exception) {
                        linked.errorHandler?.invoke(exception)
                    }
                }
            }
        }
    }

    override suspend fun close() {
        closeHandler?.invoke()
        isClosed = true
    }

    override fun onClose(block: () -> Unit) {
        closeHandler = block
    }

    override fun onError(block: (Throwable) -> Unit) {
        errorHandler = block
    }

    override fun onMessage(block: suspend (JSONRPCMessage) -> Unit) {
        messageHandler = block
    }
}
