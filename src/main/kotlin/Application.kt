package com.sriniketh

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
