package org.example.model;

import java.math.BigDecimal;

/** Immutable product read from the database; prices use exact decimal arithmetic. */
public class Product {
    private final long id;
    private final String name;
    private final String description;
    private final BigDecimal price;

    public Product(long id, String name, String description, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }
    public long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
}
