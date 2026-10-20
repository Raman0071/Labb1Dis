package org.example.dto;

import java.math.BigDecimal;

/** Read-only cart row. The business layer supplies the already calculated subtotal. */
public final class CartItemDTO {
    private final ProductDTO product;
    private final int quantity;
    private final BigDecimal subtotal;

    public CartItemDTO(ProductDTO product, int quantity, BigDecimal subtotal) {
        this.product = product;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }
    public ProductDTO getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public BigDecimal getSubtotal() { return subtotal; }
}
