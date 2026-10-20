package org.example.dto;

import java.math.BigDecimal;

/** Immutable product data for presentation; contains no persistence or business logic. */
public final class ProductDTO {
    private final long id;
    private final String name;
    private final String description;
    private final BigDecimal price;

    public ProductDTO(long id, String name, String description, BigDecimal price) {
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
