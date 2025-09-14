package com.sriniketh.utils

import com.sriniketh.mediawiki_mcp_server.BuildConfig

interface BuildConfigProvider {
    fun appVersion(): String
}

class BuildConfigProviderImpl : BuildConfigProvider {
    override fun appVersion(): String = BuildConfig.APP_VERSION
}
