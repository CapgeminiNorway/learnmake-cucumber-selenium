package learnmake.automation;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static io.github.bonigarcia.wdm.DriverManagerType.CHROME;
import static io.github.bonigarcia.wdm.DriverManagerType.FIREFOX;

public class SharedContext {

    /*
    to make use of this, include the code below on top of each StepDefs class

    private SharedContext contextSteps;
    private String withBrowser;// = "chrome";

    // PicoContainer injects class ContextSteps
    public MyStepDefs(SharedContext contextSteps) {
        this.contextSteps = contextSteps;

     */

    public enum Browser {
        firefox, chrome
    }
    private Map<Browser, RemoteWebDriver> webDrivers = new HashMap<>();
    private String withBrowser = "firefox";

    private final String FIREFOX_DRIVER_VERSION = "0.20.1"; // https://github.com/mozilla/geckodriver/releases
    private final String CHROME_DRIVER_VERSION = "2.40"; // https://sites.google.com/a/chromium.org/chromedriver/downloads

    /*
    // NB! remember to pass GITHUB_TOKEN for public repos!
    This is required for WDM lib.

    -DGITHUB_TOKEN=...your_github_token...

    e.g. read GITHUB_TOKEN from 'Jenkins' credentials or env-vars!!!
        Jenkinsfile should inject env vars when running tests, e.g.
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'github-token', usernameVariable: 'GITHUB_USERNAME', passwordVariable: 'GITHUB_TOKEN']]) {
        java ... -DGITHUB_TOKEN=${GITHUB_TOKEN} ...
    }


     */
    private final String GITHUB_TOKEN = System.getProperty("GITHUB_TOKEN", ""); // see known issues on https://github.com/bonigarcia/webdrivermanager

    private static final String PROXY_HOST =
            isLocalEnv() ?
                    "local-proxy-url"
                    : "test-env-proxy-url";
    private static final int PROXY_PORT = 8088;
    private static final String NON_PROXY_HOSTS = "localhost, 127.0.0.1,.local";

    final String PROXY = PROXY_HOST+":"+PROXY_PORT;
    final Proxy proxy = new Proxy()
            .setHttpProxy(PROXY)
            .setSslProxy(PROXY)
            .setNoProxy(NON_PROXY_HOSTS)
            .setProxyType(Proxy.ProxyType.MANUAL)
            ;

    public static final Integer SLEEP_BETWEEN_POLLS = 1000; // The duration in milliseconds to sleep between polls.
    public static final Integer IMPLICIT_WAIT = 15; // The timeout in seconds when an expectation is called
    public static final Integer IMPLICIT_WAIT_LOGIN = 30;

    //@Before("web")
    public void setUp() throws Exception {

        if (isLocalEnv()) {
            // -DisLocal=true -DGITHUB_TOKEN=...
            // -Dwdm.override=true -Dwdm.targetPath=wdm
            // -Dwebdriver.gecko.driver=wdm_local/geckodriver
            // -Dwebdriver.chrome.driver=wdm_local/chromedriver

            String driverExt = "";
            if (SystemUtils.IS_OS_WINDOWS) {
                driverExt += ".exe";
            }
            else if (SystemUtils.IS_OS_LINUX) {
                driverExt += "_linux";
            }
            else if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
                driverExt += "_mac";
            }

            String geckoDriver = "wdm_local/geckodriver" + driverExt;
            if (!System.getProperties().containsKey("webdriver.gecko.driver")) {
                System.setProperty("webdriver.gecko.driver", geckoDriver);
            }

            String chromeDriver = "wdm_local/chromedriver" + driverExt;
            if (!System.getProperties().containsKey("webdriver.chrome.driver")) {
                System.setProperty("webdriver.chrome.driver", chromeDriver);
            }

            System.out.println("-> OS_NAME: "+ SystemUtils.OS_NAME
                    + " | geckoDriver: " + geckoDriver
                    + " | chromeDriver: " + chromeDriver
            );
        }
        else {
            String wdmPath = (new File("wdm")).getAbsolutePath();
            System.out.println("wdm.targetPath : " + wdmPath);
            System.setProperty("WDM_TARGETPATH", wdmPath);
            System.setProperty("wdm.targetPath", wdmPath);
            System.setProperty("wdm.gitHubTokenName", "WebDriverManager-for-Selenium");
            System.setProperty("wdm.gitHubTokenSecret", GITHUB_TOKEN);
            // NOTE: if you need proxy, set correct values then  enable it here
            WebDriverManager.getInstance(CHROME)
                    //.proxy(PROXY)
                    .version(CHROME_DRIVER_VERSION).setup();
            WebDriverManager.getInstance(FIREFOX)
                    //.proxy(PROXY)
                    .version(FIREFOX_DRIVER_VERSION).setup();
        }

        webDrivers = new HashMap<>();
        for (Browser browser:withBrowsers()) {
            webDrivers.put(browser, initBrowserDriver(browser));
        }
        //webDrivers.put(Browser.firefox, initBrowserDriver(Browser.firefox));
    }

    //@After("web")
    public void tearDown() {
        for (RemoteWebDriver webDriver:webDrivers.values()) {
            webDriver.quit();
            try {
                if (webDriver.getSessionId() != null) {
                    Alert alert = webDriver.switchTo().alert();
                    if (alert != null ) {
                        alert.accept();
                        alert.dismiss();
                        webDriver.quit();
                    }
                }
            } catch (Exception ex) {
                System.out.println(SharedContext.class.getSimpleName()+" | while forcing quit: " + ex);
            }
            finally {
                if (webDriver.getSessionId() != null) {
                    webDriver.quit();
                }
            }
        }
    }

    public String getWithBrowser() {
        return withBrowser;
    }

    public void setWithBrowser(String withBrowser) {
        this.withBrowser = withBrowser;
    }

    private RemoteWebDriver initBrowserDriver() {
        return initBrowserDriver(Browser.firefox); // default: firefox
    }
    private RemoteWebDriver initBrowserDriver(Browser browser) {
        RemoteWebDriver browserDriver;

        if (browser.equals(Browser.chrome)) {

            ChromeOptions browserOptions = new ChromeOptions();

            // NOTE: if you need proxy, set correct values then  enable it here
            //browserOptions.setProxy(proxy);
            browserOptions.setAcceptInsecureCerts(true);

            browserOptions.setCapability("media.gmp-provider.enabled", false);
            browserOptions.setCapability("extensions.logging.enabled", false);
            browserOptions.setCapability("security.sandbox.content.level", 4);

            // to avoid UnhandledAlertException, see;
            // http://agilesoftwareautomationtesting.blogspot.dk/2012/09/usual-problem-in-firefox-under-heavy.html
            browserOptions.setCapability("dom.max_chrome_script_run_time" , "99");
            browserOptions.setCapability("dom.max_script_run_time" , "99");
            browserOptions.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.DISMISS);
            browserOptions.addArguments("incognito");
            browserOptions.setHeadless( isHeadless() );

            browserDriver = new ChromeDriver(browserOptions);
        }
        else {// default: firefox

            FirefoxOptions browserOptions = new FirefoxOptions();

            // NOTE: if you need proxy, set correct values then  enable it here
            /*
            browserOptions.addPreference("network.proxy.type", 1);
            browserOptions.addPreference("network.proxy.http", PROXY_HOST);
            browserOptions.addPreference("network.proxy.http_port", PROXY_PORT);
            browserOptions.addPreference("network.proxy.ssl", PROXY_HOST);
            browserOptions.addPreference("network.proxy.ssl_port", PROXY_PORT);
            browserOptions.addPreference("network.proxy.no_proxies_on", NON_PROXY_HOSTS);
            */

            FirefoxProfile firefoxProfile = new FirefoxProfile();
            firefoxProfile.setAcceptUntrustedCertificates(true);
            firefoxProfile.setPreference("browser.privatebrowsing.autostart", true);
            firefoxProfile.setPreference("media.gmp-provider.enabled", false);
            firefoxProfile.setPreference("security.sandbox.content.level", 4);
            browserOptions.setProfile(firefoxProfile);

            // to avoid UnhandledAlertException, see;
            // http://agilesoftwareautomationtesting.blogspot.dk/2012/09/usual-problem-in-firefox-under-heavy.html
            browserOptions.addPreference("dom.max_chrome_script_run_time" , "99");
            browserOptions.addPreference("dom.max_script_run_time" , "99");

            browserOptions.setAcceptInsecureCerts(true);
            browserOptions.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.DISMISS);
            browserOptions.addArguments("-private");
            browserOptions.setHeadless( isHeadless() );

            browserDriver = new FirefoxDriver(browserOptions);
        }

        try {
            // browserDriver.manage().deleteAllCookies(); // no-need anymore, we are using browsers in private/incognito mode
            browserDriver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT, TimeUnit.SECONDS);
            browserDriver.manage().window().maximize();
        } catch (UnhandledAlertException ex) { // see https://stackoverflow.com/a/46444423
            try {
                System.out.println(SharedContext.class.getSimpleName()+"|" + "UnhandledAlertException: " + ex);
                Alert alert = browserDriver.switchTo().alert();
                System.out.println(SharedContext.class.getSimpleName()+"|" + "alert.getText: " + alert.getText());
                alert.dismiss();// .accept();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return browserDriver;
    }

    public RemoteWebDriver getDriver() {
        return getDriver(Browser.firefox);
    }
    public RemoteWebDriver getDriver(String browser) {
        if (StringUtils.isNotEmpty(browser)) {
            for (Browser theBrowser:Browser.values()) {
                if (browser.equalsIgnoreCase(theBrowser.name())) {
                    return getDriver(theBrowser);
                }
            }
            return getDriver(Browser.firefox); // default: firefox
        }
        else {
            return getDriver(Browser.firefox);// default: firefox
        }
    }
    public RemoteWebDriver getDriver(Browser browser) {
        if (browser!=null
                && Arrays.asList(Browser.values()).contains(browser)
                ) {
            if (!webDrivers.containsKey(browser)) {
                webDrivers.put(browser, initBrowserDriver(browser));
            }
        }
        else {// default: firefox
            browser = Browser.firefox;
            if (!webDrivers.containsKey(browser)) {
                webDrivers.put(browser, initBrowserDriver(browser));
            }
        }
        return webDrivers.get(browser);
    }

    public WebDriverWait getWebDriverWait() {
        return getWebDriverWait(Browser.firefox.name(), "default");
    }
    public WebDriverWait getWebDriverWait(String withBrowser) {
        return getWebDriverWait(withBrowser, "default");
    }
    public WebDriverWait getWebDriverWait(String withBrowser, String whichType) {

        WebDriverWait wait;
        if ("login".equalsIgnoreCase(whichType)) {
            wait = new WebDriverWait(getDriver(withBrowser), IMPLICIT_WAIT_LOGIN, SLEEP_BETWEEN_POLLS);
        }
        else {// default
            if (StringUtils.isNotEmpty(whichType) && StringUtils.isNumeric(whichType)) {// if we have custom wait in seconds
                wait = new WebDriverWait(getDriver(withBrowser), Integer.valueOf(whichType), SLEEP_BETWEEN_POLLS);
            }
            else {
                wait = new WebDriverWait(getDriver(withBrowser), IMPLICIT_WAIT, SLEEP_BETWEEN_POLLS);
            }
        }
        wait.ignoring(UnhandledAlertException.class)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(ElementNotInteractableException.class)
        ;

        return wait;
    }

    public static List<Browser> withBrowsers() {

        List<Browser> browsers = new ArrayList<>();

        String withBrowsers = System.getProperty("withBrowsers");
        if (StringUtils.isEmpty(withBrowsers)) {
            withBrowsers = "";// default
        }
        for (String withBrowser:withBrowsers.split(",")) {

            for (Browser browser:Browser.values()) {
                if (browser.name().equalsIgnoreCase(withBrowser)
                        && !browsers.contains(browser)
                        ) {
                    browsers.add(browser);
                }
            }
        }

        /*if (browsers.size() == 0) {
            browsers.add(Browser.firefox); // default
        }*/

        return browsers;
    }

    public static boolean isHeadless() {

        try {
            return "true".equalsIgnoreCase(System.getProperty("isHeadless"));
        }
        catch (Exception ex) {// happens when there's no such property, so we ignore the exception
            return false;
        }
    }

    public static boolean isLocalEnv() {

        try {
            return "true".equalsIgnoreCase(System.getProperty("isLocal"));
        }
        catch (Exception ex) {// happens when there's no such property, so we ignore the exception
            return false;
        }
    }
}
