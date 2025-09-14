package com.sriniketh

import com.sriniketh.mcp.MediaWikiMCPServer
import kotlinx.coroutines.awaitCancellation

suspend fun main() {
    try {
        val server = MediaWikiMCPServer()
        server.start()
        awaitCancellation()
    } catch (exception: Exception) {
        exception.printStackTrace(System.err)
        throw exception
    }
}
