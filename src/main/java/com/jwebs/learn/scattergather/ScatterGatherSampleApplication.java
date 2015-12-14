package com.jwebs.learn.scattergather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.IntegrationComponentScan;

@SpringBootApplication
@IntegrationComponentScan
public class ScatterGatherSampleApplication {
	public static void main(String[] args) {
		SpringApplication.run(ScatterGatherSampleApplication.class, args);
	}
}
