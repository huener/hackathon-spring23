package com.hackastars.eshop;

public class Product {
	private final String upc;

	private final String name;
	private final String imageLink;
	private final int averageGrade;

	public Product(String upc, String name, String imageLink, int averageGrade) {
		this.upc = upc;
		this.name = name;
		this.imageLink = imageLink;
		this.averageGrade = averageGrade;
	}

	public String getUpc() {
		return upc;
	}

	public String getName() {
		return name;
	}

	public String getImageLink() {
		return imageLink;
	}

	public int getAverageGrade() {
		return averageGrade;
	}
}
