package com.jwebs.learn.cafe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.util.Assert;

@SuppressWarnings("serial")
public class Order implements Serializable {
	private Integer id;
	private Collection<OrderItem> items = new ArrayList<>();
	
	public Order(Integer id) {
		Assert.notNull(id);
		this.id = id;
	}
	
	public Integer getId() {
		return id;
	}
	public Collection<OrderItem> getItems() {
		return items;
	}

	public void addItem(DrinkType type, int number, boolean cold) {
		OrderItem item = new OrderItem(this, type, number, cold);
		items.add(item);
	}

	@Override
	public String toString() {
		return "Order [id=" + id + ", items=" + items + "]";
	}
}
