package com.jwebs.learn.threadbarrier;

import java.util.Calendar;

import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.jms.Jms;

@SpringBootApplication
public class ProcessorApplication {
	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext applicationContext = new SpringApplicationBuilder(ProcessorApplication.class)
			.web(false)
			.run(args);
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> applicationContext.close()));
		
		System.out.println("Pres enter key to exit...");
		System.in.read();
		System.exit(0);
	}
	
	@Autowired
	private ConnectionFactory connectionFactory;
	
	@Bean
	public IntegrationFlow processFlow() {
		return IntegrationFlows
				.from(Jms.inboundGateway(connectionFactory)
						.destination("process.queue")
						.configureListenerContainer(spec -> spec.sessionTransacted(true)))
				.wireTap(wiretapFlow -> wiretapFlow.handle(message -> System.out.println("#### Command message received tobe processed : " + message.toString())))
				.handle((payload, headers) -> true)
				.get();
	}
	
	@Bean
	public IntegrationFlow pingFlow() {
		return IntegrationFlows
				.from(Jms.inboundGateway(connectionFactory).destination("ping.queue").configureListenerContainer(spec -> spec.sessionTransacted(true)))
				.wireTap(wiretapFlow -> wiretapFlow.handle(message -> System.out.println("#### Ping message received : " + message.toString())))
				.handle((payload, headers) -> Calendar.getInstance().getTime())
				.get();
	}
}
