package com.jwebs.learn.jmsreqrep.client;

import java.util.Random;

import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.jms.Jms;
import org.springframework.integration.support.MessageBuilder;

@SpringBootApplication
public class ClientApplication {
	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext applicationContext = new SpringApplicationBuilder(ClientApplication.class)
				.web(false)
				.run(args);
			
			Runtime.getRuntime().addShutdownHook(new Thread(() -> applicationContext.close()));
			
			System.out.println("Press enter key to exit...");
			System.in.read();
			System.exit(0);
	}

	@Autowired
	private ConnectionFactory connectionFactory;
	
	private Random random = new Random();
	
	@Bean
	public MessageSource<String> randomMessageSource() {
		return () -> MessageBuilder.withPayload(random.nextInt(100) % 2 == 0 ? "ping" : "pong").build();
	}
	
	@Bean
	public IntegrationFlow sendPing() {
		return IntegrationFlows
				.from(randomMessageSource(), sourceSpec -> sourceSpec.poller(pollerSpec -> pollerSpec.fixedDelay(1000).maxMessagesPerPoll(10)))
				.wireTap(wiretapFlow -> wiretapFlow.handle(message -> System.out.println("#### Sending ping message : " + message.toString())))
				.handle(Jms.outboundGateway(connectionFactory)
						.requestDestination("service.ping.queue").replyContainer(containerSpec -> containerSpec.sessionTransacted(true))
				)
				.<String, Boolean>route(payload -> payload.equalsIgnoreCase("PONG"), 
						mapping -> mapping
								.subFlowMapping("true", subFlow -> subFlow.handle((payload, headers) -> {System.out.println("Received ping response : " + payload); return payload;}))
								.subFlowMapping("false", subFlow -> subFlow.handle((payload, headers) -> {System.out.println("Received ping response : " + payload); return payload;}))
				)
				.handle(message -> System.out.println(message.toString()))
				.get();
	}
}
