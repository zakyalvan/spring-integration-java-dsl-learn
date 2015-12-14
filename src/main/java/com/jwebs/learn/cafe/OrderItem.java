package com.jwebs.learn.cafe;

import java.io.Serializable;

@SuppressWarnings("serial")
public class OrderItem implements Serializable {
	private Order order;
	private DrinkType type;
	private Integer shots;
	private boolean iced;
	
	public OrderItem(Order order, DrinkType type, int shots, boolean iced) {
		this.order = order;
		this.type = type;
		this.shots = shots;
		this.iced = iced;
	}

	public Order getOrder() {
		return order;
	}
	
	public DrinkType getType() {
		return type;
	}

	public Integer getShots() {
		return shots;
	}

	public boolean isIced() {
		return iced;
	}

	@Override
	public String toString() {
		return "OrderItem [type=" + type + ", shots=" + shots + ", iced=" + iced + "]";
	}
}
