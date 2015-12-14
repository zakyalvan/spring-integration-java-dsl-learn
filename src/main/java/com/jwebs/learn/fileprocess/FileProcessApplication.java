package com.jwebs.learn.fileprocess;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.integration.annotation.IntegrationComponentScan;

/**
 * Processing file from ftp and store record into database.
 * 
 * @author zakyalvan
 */
@SpringBootApplication
@IntegrationComponentScan
@EnableConfigurationProperties(FtpConnectionConfigProperties.class)
public class FileProcessApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext applicationContext = new SpringApplicationBuilder(FileProcessApplication.class)
				.profiles("fileprocess")
				.web(true)
				.run(args);
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> applicationContext.close()));
	}
}
