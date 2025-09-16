package fakes

import com.sriniketh.utils.EnvConfigProvider

class FakeEnvConfigProvider : EnvConfigProvider {
    override fun wikiName(): String = "TestWiki"
    override fun apiUrl(): String = "https://testwiki.org/api.php"
}
