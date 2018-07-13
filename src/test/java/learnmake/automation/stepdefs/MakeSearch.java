package learnmake.automation.stepdefs;

import learnmake.automation.SharedContext;
import learnmake.automation.pages.AbstractPage;
import learnmake.automation.pages.DuckDuckGoPage;
import learnmake.automation.pages.GooglePage;
import cucumber.api.Scenario;
import cucumber.api.java8.En;
import org.openqa.selenium.WebDriver;

import static org.assertj.core.api.Assertions.assertThat;

public class MakeSearch implements En {
    private WebDriver driver;
    private AbstractPage searchPage;

    private SharedContext sharedContext;

    // Warning: Make sure the timeouts for hooks using a web driver are zero

    // NOTE: picocontainer injects SharedContext
    public MakeSearch(SharedContext sharedContext) {
        this.sharedContext = sharedContext;

        Before(new String[]{"@web"}, 0, 1, (Scenario scenario) -> {
            this.sharedContext.setUp();
        });

        Before(new String[]{"@web", "@chrome"}, 0, 1, (Scenario scenario) -> {
            driver = this.sharedContext.getDriver("chrome");
        });
        Before(new String[]{"@web", "@firefox"}, 0, 1, (Scenario scenario) -> {
            driver = this.sharedContext.getDriver("firefox");
        });
        Before(new String[]{"@google"}, 0, 10, (Scenario scenario) -> {
            searchPage = new GooglePage(driver);
        });
        Before(new String[]{"@duckduckgo"}, 0, 10, (Scenario scenario) -> {
            searchPage = new DuckDuckGoPage(driver);
        });
        Given("^a web browser is on the search engine page$", () -> {
            searchPage.navigateToHomePage();
        });
        When("^the search phrase \"([^\"]*)\" is entered$", (String phrase) -> {
            searchPage.enterSearchPhrase(phrase);
        });
        Then("^results for \"([^\"]*)\" are shown$", (String phrase) -> {
            assertThat(searchPage.pageTitleContains(phrase)).isTrue();
        });

        After(new String[]{"@web"}, (Scenario scenario) -> {
            this.sharedContext.tearDown();
        });
    }
}
