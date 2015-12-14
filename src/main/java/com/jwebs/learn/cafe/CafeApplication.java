package com.jwebs.learn.cafe;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.stream.CharacterStreamWritingMessageHandler;

@SpringBootApplication
@IntegrationComponentScan
public class CafeApplication {
	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext applicationContext = 
				new SpringApplication(CafeApplication.class).run(args);
		
		Cafe cafe = applicationContext.getBean(Cafe.class);
		for(int i=1; i<=100; i++) {
			Order order = new Order(i);
			order.addItem(DrinkType.MOCHA, 2, true);
			order.addItem(DrinkType.LATTE, 3, false);
			cafe.placeOrder(order);
		}
		
		System.out.println("Hit enter to terminate");
		System.in.read();
		applicationContext.close();
	}
	
	private AtomicInteger coldDrinkCounter = new AtomicInteger(0);
	private AtomicInteger hotDrinkCounter = new AtomicInteger(0);
	
	@Bean(name=PollerMetadata.DEFAULT_POLLER)
	public PollerMetadata defaultPoller() {
		return Pollers.fixedDelay(1000l).get();
	}
	
	@MessagingGateway
	public static interface Cafe {
		@Gateway(requestChannel="cafe.input")
		void placeOrder(Order order);
	}
	
	/**
	 * Flow of main drink processing.
	 * 
	 * @return
	 * @throws InterruptedException 
	 */
	@Bean
	public IntegrationFlow cafe() {
		return flow -> flow
				.split(Order.class, Order::getItems)
				.channel(spec -> spec.executor(Executors.newCachedThreadPool()))
				.route(OrderItem::isIced, mapping -> mapping
						.subFlowMapping("true", coldFlow())
						.subFlowMapping("false", hotFlow()))
				.<OrderItem, Drink>transform(item -> new Drink(item.getOrder(), item.getType(), item.isIced(), item.getShots()))
				.aggregate(spec -> spec
						.outputProcessor(group -> new Delivery(group.getMessages().stream().map(message -> (Drink) message.getPayload()).collect(Collectors.toList())))
						.correlationStrategy(message -> ((Drink) message.getPayload()).getOrder().getId())
				)
				.handle(CharacterStreamWritingMessageHandler.stdout());
	}
	
	/**
	 * Flow of cold drink processing.
	 * 
	 * @return
	 * @throws InterruptedException 
	 */
	@Bean
	public IntegrationFlow coldFlow() {
		return flow -> flow
				.channel(spec -> spec.queue(10))
				.publishSubscribeChannel(spec -> spec
						.subscribe(sub -> sub.handle(message -> {
							try {
								Thread.sleep(1000l);
							}
							catch(Exception e) {
								e.printStackTrace();
							}
						}))
						.subscribe(sub -> sub.
								<OrderItem, String>transform(item -> Thread.currentThread().getName() + " prepared cold drink #" + this.coldDrinkCounter.incrementAndGet() + " for order #" + item.getOrder().getId())
								.handle(message -> System.out.println(message.getPayload()))));
	}
	
	/**
	 * Flow of hot drink processing.
	 * 
	 * @return
	 */
	@Bean
	public IntegrationFlow hotFlow() {
		return flow -> flow
				.channel(spec -> spec.queue(10))
				.publishSubscribeChannel(spec -> spec
						.subscribe(sub -> sub.handle(message -> {
							try {
								Thread.sleep(3000l);
							}
							catch(Exception e) {
								e.printStackTrace();
							}
						}))
						.subscribe(sub -> sub.
								<OrderItem, String>transform(item -> Thread.currentThread().getName() + "is preparing hot drink #" + this.hotDrinkCounter.incrementAndGet() + " for order #" + item.getOrder().getId())
								.handle(message -> System.out.println(message.getPayload()))));
	}
}
