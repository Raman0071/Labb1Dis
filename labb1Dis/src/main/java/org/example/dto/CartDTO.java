package org.example.dto;

import java.math.BigDecimal;
import java.util.List;

/** Consistent, immutable cart snapshot. JSP can read it without accessing the mutable cart. */
public final class CartDTO {
    private final List<CartItemDTO> items;
    private final int count;
    private final BigDecimal total;

    public CartDTO(List<CartItemDTO> items, int count, BigDecimal total) {
        this.items = List.copyOf(items);
        this.count = count;
        this.total = total;
    }
    public List<CartItemDTO> getItems() { return items; }
    public int getCount() { return count; }
    public BigDecimal getTotal() { return total; }
}
