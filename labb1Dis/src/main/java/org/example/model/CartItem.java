package org.example.model;

import java.math.BigDecimal;

/** Immutable snapshot of a cart row for rendering outside the cart lock. */
public class CartItem {
    private final Product product;
    private final int quantity;
    public CartItem(Product product, int quantity) { this.product = product; this.quantity = quantity; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public BigDecimal getSubtotal() { return product.getPrice().multiply(BigDecimal.valueOf(quantity)); }
}
