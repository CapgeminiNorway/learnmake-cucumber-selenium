
mvn clean test -Dwebdriver.chrome.driver=wdm_local/chromedriver  

https://github.com/cucumber/cucumber-jvm/issues/1392  
An error is caused by IDEA's CucumberJvm2SMFormatter. It being the formatter for Cucumber2, runs into a breaking change in Cucumber 3.  

If you remove --plugin org.jetbrains.plugins.cucumber.java.run.CucumberJvm2SMFormatter from program arguments in the run configuration it will work again. You'll have to do this until InteliJ fixes their plugin.

