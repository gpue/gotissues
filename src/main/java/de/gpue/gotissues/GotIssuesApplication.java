package de.gpue.gotissues;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class GotIssuesApplication {

	private static final Log log = LogFactory
			.getLog(GotIssuesApplication.class);

	public static void main(String[] args) {
		log.debug("Starting...");
		SpringApplication.run(GotIssuesApplication.class, args);
	}

}
