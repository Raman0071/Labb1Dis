package org.example.model;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** One cart per HTTP session. Synchronized methods handle concurrent requests. */
public class Cart {
    private final Map<Long, CartItem> items = new LinkedHashMap<>();
    public synchronized void add(Product product, int quantity) {
        CartItem previous = items.get(product.getId());
        int existing = previous == null ? 0 : previous.getQuantity();
        if (quantity < 1 || quantity > 99 || existing + quantity > 99) {
            throw new IllegalArgumentException("Antalet måste vara 1–99 per produkt.");
        }
        items.put(product.getId(), new CartItem(product, existing + quantity));
    }
    public synchronized void remove(long productId) { items.remove(productId); }
    public synchronized List<CartItem> getItems() { return List.copyOf(items.values()); }
    public synchronized int getCount() { return items.values().stream().mapToInt(CartItem::getQuantity).sum(); }
    public synchronized BigDecimal getTotal() {
        return items.values().stream().map(CartItem::getSubtotal).reduce(new BigDecimal("0.00"), BigDecimal::add);
    }
}
