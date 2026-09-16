package com.sriniketh

import com.sriniketh.mcp.MediaWikiMCPServer

suspend fun main() {
    try {
        val server = MediaWikiMCPServer()
        server.start()
        server.awaitShutdown()
    } catch (exception: Exception) {
        exception.printStackTrace(System.err)
        throw exception
    }
}
