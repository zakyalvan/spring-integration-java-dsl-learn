package com.jwebs.learn.threadbarrier;

import java.util.Calendar;

import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.aggregator.BarrierMessageHandler;
import org.springframework.integration.aggregator.MessageGroupProcessor;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.jms.Jms;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

/**
 * 
 * @author zakyalvan
 */
@SpringBootApplication
@IntegrationComponentScan
public class SourceApplication {
	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext applicationContext = new SpringApplicationBuilder(SourceApplication.class)
			.web(false)
			.run(args);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> applicationContext.close()));
		
		ProcessorGateway processor = applicationContext.getBean(ProcessorGateway.class);
		processor.process(new Command("shutdown"));
		
		System.out.println("Press enter key to exit...");
		System.in.read();
		System.exit(1);
	}
	
	@Autowired
	private ConnectionFactory connectionFactory;
	
	@MessagingGateway
	public static interface ProcessorGateway {
		@Gateway(requestChannel="process.input")
		boolean process(Command command);
	}
	
	@Bean
	public MessageGroupProcessor messageGroupProcessor() {
		return group -> {
			Object command = null;
			for(Message<?> message : group.getMessages()) {
				if(Command.class.isAssignableFrom(message.getPayload().getClass())) {
					command = message.getPayload();
				}
			}
			return command;
		};
	}
	
	@Bean
	public BarrierMessageHandler barrier() {
		BarrierMessageHandler barrier = new BarrierMessageHandler(3000, messageGroupProcessor());
		barrier.setRequiresReply(true);
		return barrier;
	}
	
	@Bean
	public IntegrationFlow process() {
		return flow -> flow
				.enrich(enrichSpec -> enrichSpec
						.headerFunction(IntegrationMessageHeaderAccessor.CORRELATION_ID, (Message<Command> message) -> message.getPayload().getId()))
				.wireTap(wiretapFlow -> wiretapFlow
						.handle(message -> System.out.println("@@@@@@@@@@ Sending command message : " + message.toString())))
				.publishSubscribeChannel(spec -> spec.subscribe(ping()))
				.handle(barrier())
				.wireTap(wiretapFlow -> wiretapFlow
						.handle(message -> System.out.println("$$$$$$$$$$ After barrier : " + message.toString())))
				.handle(Jms.outboundGateway(connectionFactory)
						.requestDestination("process.queue")
						.replyContainer(), 
					spec -> spec.requiresReply(true));
	}
	
	@Bean
	public MessageHandler releaser() {
		return message -> barrier().trigger(message);
	}
	
	@Bean
	public IntegrationFlow ping() {
		return flow -> flow
				.channel(channelSpec -> channelSpec.queue(10))
				.transform(Command.class, 
						command -> Calendar.getInstance().getTime(), 
						endpointSpec -> endpointSpec.poller(pollerSpec -> pollerSpec.fixedDelay(2000)))		
				.handle(Jms.outboundGateway(connectionFactory)
							.requestDestination("ping.queue")
							.replyContainer(), 
						spec -> spec.requiresReply(true))
				.handle(releaser());
	}
}
