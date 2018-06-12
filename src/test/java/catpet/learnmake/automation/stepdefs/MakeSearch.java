package catpet.learnmake.automation.stepdefs;

import catpet.learnmake.automation.pages.AbstractPage;
import catpet.learnmake.automation.pages.DuckDuckGoPage;
import catpet.learnmake.automation.pages.GooglePage;
import cucumber.api.Scenario;
import cucumber.api.java8.En;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import static org.assertj.core.api.Assertions.assertThat;

public class MakeSearch implements En {
    private WebDriver driver;
    private AbstractPage searchPage;

    // Warning: Make sure the timeouts for hooks using a web driver are zero

    public MakeSearch() {
        Before(new String[]{"@web", "@chrome"}, 0, 1, (Scenario scenario) -> {
            driver = new ChromeDriver();
        });
        Before(new String[]{"@web", "@firefox"}, 0, 1, (Scenario scenario) -> {
            driver = new FirefoxDriver();
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
            driver.quit();
        });
    }
}
