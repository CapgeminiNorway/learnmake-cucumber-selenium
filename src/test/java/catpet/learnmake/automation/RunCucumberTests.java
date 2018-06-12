package catpet.learnmake.automation;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty", "html:target/cucumber", "json:target/cucumber.json", "junit:target/cucumber.xml"},
        features = "src/test/resources/catpet/learnmake/automation",
        glue = {"catpet.learnmake.automation.stepdefs"})
public class RunCucumberTests {
}