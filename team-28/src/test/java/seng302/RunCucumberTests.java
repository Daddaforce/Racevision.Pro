package seng302;

import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith ;
import cucumber.api.junit.Cucumber ;
import cucumber.api.SnippetType ;

@RunWith (Cucumber.class)
@CucumberOptions(features="features",
        format = {"pretty"  ,
                "html:target/site/cucumber-pretty" ,
                "json:target/cucumber.json"} ,
        snippets = SnippetType.CAMELCASE )

public class RunCucumberTests {
}
