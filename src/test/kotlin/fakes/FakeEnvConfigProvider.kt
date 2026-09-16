package fakes

import com.sriniketh.utils.EnvConfigProvider

class FakeEnvConfigProvider(private val name: String = "TestWiki") : EnvConfigProvider {
    override fun wikiName(): String = name
    override fun apiUrl(): String = "https://testwiki.org/api.php"
}
