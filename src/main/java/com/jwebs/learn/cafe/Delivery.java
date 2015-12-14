package com.jwebs.learn.cafe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("serial")
public class Delivery implements Serializable {
	private Collection<Drink> drinks = new ArrayList<>();
	
	public Delivery(Collection<Drink> drinks) {
		this.drinks.addAll(drinks);
	}

	public Collection<Drink> getDrinks() {
		return drinks;
	}

	@Override
	public String toString() {
		return "Delivery [drinks=" + drinks + "]";
	}
}
