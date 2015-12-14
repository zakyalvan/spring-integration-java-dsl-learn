package com.jwebs.learn.errorhandling;

import java.util.Random;

import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.dsl.jms.Jms;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessagingException;

/**
 * Show how to handle error in spring integration flow.
 * Please note, errorChannel in spring integration only applicable to
 * error thrown in asynch component.
 * 
 * @author zakyalvan
 */
@SpringBootApplication
@IntegrationComponentScan
public class ErrorHandlingApplication {
	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext applicationContext = new SpringApplicationBuilder(ErrorHandlingApplication.class)
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
	public MessageSource<Integer> randomIntegerMessageSource() {
		return () -> MessageBuilder.withPayload(new Random().nextInt()).build();
	}
	
	@Bean
	public IntegrationFlow withErrorFlow() {
		return IntegrationFlows.from(randomIntegerMessageSource(), spec -> spec.poller(Pollers.fixedDelay(1000)))
					.handle(Jms.outboundGateway(connectionFactory)
					.requestDestination("processor.input")
					.replyContainer(spec -> spec.sessionTransacted(true)))
					.get();
	}
	
	@Autowired
	@Qualifier("errorChannel")
	private PublishSubscribeChannel errorChannel;
	
	@Bean
	public IntegrationFlow errorHandlingFlow() {
		return IntegrationFlows.from(errorChannel)
				.handle(message -> System.out.println("@@@@@@@@@@@@@@@@@@@@@" + ((MessagingException) message.getPayload()).getFailedMessage().getPayload()))
				.get();
	}
}
