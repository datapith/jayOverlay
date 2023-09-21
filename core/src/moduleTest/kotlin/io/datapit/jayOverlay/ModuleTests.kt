package io.datapit.jayOverlay

import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.runner.RunWith

@RunWith(Cucumber::class)
@CucumberOptions(
    plugin = ["pretty", "html:build/test-results/moduleTest/core.html"],
    features = ["src/moduleTest/features"],
    glue = ["io.datapit.jayOverlay.steps"]
)
class ModuleTests {
}