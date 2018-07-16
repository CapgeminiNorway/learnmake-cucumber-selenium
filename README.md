Automation with BDD Testing  
===========================  

Learn-Make _BDD Automation Tests using Cucumber, Selenium, Java_         


# LEARN & MAKE  

## Reading   
Some useful links to advance your learning:   

- Cucumber [10 Minute Tutorial](https://docs.cucumber.io/guides/10-minute-tutorial/)   
- Selenium [WebDriver](https://www.seleniumhq.org/docs/03_webdriver.jsp)  
- Very nice [blog about BDD Testing >>](https://automationpanda.com/bdd/)  


## Setup your DEV env      

Getting up and running for your dev-env is as easy as counting 1 to 4 :-)     

1. Make sure you have installed [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html) _version 8_, [Maven](https://maven.apache.org/index.html) _version 3.3.1 or higher_.      

2. Clone this repo and verify compile  

    ```
    git clone https://github.com/CapgeminiNorway/learnmake-cucumber-selenium  
    cd path/to/learnmake-cucumber-selenium  
    mvn clean compile  
    ```

3. Run all tests and enjoy automation

    ```
    mvn clean test -DisLocal=true -DisHeadless=false
    ```  
    to see test results are generated, just open this `learnmake-cucumber-selenium/target/cucumber/index.html` in your browser  
      
    _optional params:_    
    - when running inside Jenkins environment, you must append `-DGITHUB_TOKEN=...public_access_token...`  
    - if you want to forcefully use custom webdrivers (e.g. specific versions), then just append:     
    `-Dwebdriver.gecko.driver=wdm_local/geckodriver -Dwebdriver.chrome.driver=wdm_local/chromedriver`  

4. Run inside Intellij IDEA    
    Import as standard Maven project.    
    Then, set 'Cucumber java' defaults of this project as:    
    - Main class: `cucumber.api.cli.Main`       
    - Glue: `learnmake.automation.stepdefs`      
    - feature folder path: _point to folder where feature files are_  
    - VM options: (copy the same ones from mvn example )   
    
    p.s. Intellij has a bug with Cucumber, here's [a workaround](https://github.com/cucumber/cucumber-jvm/issues/1392)          
    If you remove `--plugin org.jetbrains.plugins.cucumber.java.run.CucumberJvm2SMFormatter` from program arguments in the run configuration it will work again. You'll have to do this until InteliJ fixes their plugin.  
 
## Together We DO!  
Please contribute with comments, pull-requests, etc.  
  
  
