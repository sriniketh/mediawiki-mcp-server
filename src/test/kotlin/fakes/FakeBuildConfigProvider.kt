package fakes

import com.sriniketh.utils.BuildConfigProvider

class FakeBuildConfigProvider : BuildConfigProvider {
    override fun appVersion(): String = "0.1.0-test"
}
