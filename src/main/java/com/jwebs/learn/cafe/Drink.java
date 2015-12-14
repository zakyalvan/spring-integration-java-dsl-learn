package com.jwebs.learn.cafe;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Drink implements Serializable {
	private Order order;
	private DrinkType type;
	private boolean iced;
	private Integer shots;
	
	public Drink(Order order, DrinkType type, boolean iced, Integer shots) {
		this.order = order;
		this.type = type;
		this.iced = iced;
		this.shots = shots;
	}

	public DrinkType getType() {
		return type;
	}

	public boolean isIced() {
		return iced;
	}

	public Integer getShots() {
		return shots;
	}

	public Order getOrder() {
		return order;
	}

	@Override
	public String toString() {
		return "Drink [type=" + type + ", iced=" + iced + ", shots=" + shots + "]";
	}
}
