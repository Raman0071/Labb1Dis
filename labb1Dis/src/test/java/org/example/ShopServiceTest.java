package org.example;

import org.example.dao.Database;
import org.example.dao.ProductDao;
import org.example.dao.UserDao;
import org.example.model.Cart;
import org.example.service.ShopService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

class ShopServiceTest {
    private Database database;
    private ShopService shop;
    @BeforeEach void setUp() throws Exception {
        database = new Database("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        database.initialize();
        shop = new ShopService(new ProductDao(database), new UserDao(database));
    }
    @Test void loginChecksCredentialsAndRejectsSqlInjection() throws Exception {
        assertEquals("student", shop.login("student", "labb123").orElseThrow().getUsername());
        assertTrue(shop.login("student", "wrong").isEmpty());
        assertTrue(shop.login("' OR '1'='1", "labb123").isEmpty());
        assertTrue(shop.login(null, null).isEmpty());
    }
    @Test void addsMergeAndTotalsUseExactDecimals() throws Exception {
        Cart cart = new Cart();
        shop.addToCart(cart, 3, 2);
        shop.addToCart(cart, 3, 1);
        shop.addToCart(cart, 1, 1);
        assertEquals(2, cart.getItems().size());
        assertEquals(4, cart.getCount());
        assertEquals(new BigDecimal("117.50"), cart.getTotal());
        var snapshot = cart.getItems();
        shop.removeFromCart(cart, 3);
        assertEquals(new BigDecimal("59.00"), cart.getTotal());
        assertEquals(3, snapshot.get(0).getQuantity());
    }
    @Test void invalidChangesLeaveCartIntact() throws Exception {
        Cart cart = new Cart();
        assertThrows(IllegalArgumentException.class, () -> shop.addToCart(cart, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> shop.addToCart(cart, 1, -1));
        assertThrows(IllegalArgumentException.class, () -> shop.addToCart(cart, 1, Integer.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> shop.addToCart(cart, 999, 1));
        shop.addToCart(cart, 1, 99);
        assertThrows(IllegalArgumentException.class, () -> shop.addToCart(cart, 1, 1));
        assertEquals(99, cart.getCount());
    }
    @Test void cartsAreIndependentAndConcurrentAddsAreNotLost() throws Exception {
        var product = new ProductDao(database).findById(1).orElseThrow();
        Cart first = new Cart();
        Cart second = new Cart();
        var pool = Executors.newFixedThreadPool(4);
        try {
            var tasks = java.util.stream.IntStream.range(0, 40)
                    .<java.util.concurrent.Callable<Void>>mapToObj(i -> () -> { first.add(product, 1); return null; }).toList();
            for (var result : pool.invokeAll(tasks)) { result.get(); }
        } finally { pool.shutdownNow(); }
        assertEquals(40, first.getCount());
        assertEquals(0, second.getCount());
    }
    @Test void reinitializingPreservesExistingDataAndHashIsNotPlaintext() throws Exception {
        try (var connection = database.connect(); var statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE products SET price = 42.00 WHERE id = 1");
        }
        database.initialize();
        assertEquals(3, shop.listProducts().size());
        assertEquals(new BigDecimal("42.00"), shop.listProducts().get(0).getPrice());
        String hash = new UserDao(database).findByUsername("student").orElseThrow().passwordHash();
        assertFalse(hash.contains("labb123"));
        assertTrue(shop.login("student", "labb123").isPresent());
    }

    @Test void cartDtoIsAnImmutableSnapshotOfCalculatedData() throws Exception {
        Cart cart = new Cart();
        shop.addToCart(cart, 3, 2);
        var snapshot = shop.getCart(cart);
        assertEquals(2, snapshot.getCount());
        assertEquals(new BigDecimal("39.00"), snapshot.getTotal());
        var row = snapshot.getItems().get(0);
        assertEquals("Penna", row.getProduct().getName());
        assertEquals(new BigDecimal("19.50"), row.getProduct().getPrice());
        assertEquals(new BigDecimal("39.00"), row.getSubtotal());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.getItems().clear());
        shop.addToCart(cart, 3, 1);
        shop.removeFromCart(cart, 3);
        assertEquals(2, snapshot.getCount());
        assertEquals(2, row.getQuantity());
        assertEquals(new BigDecimal("39.00"), snapshot.getTotal());
        assertTrue(shop.getCart(cart).getItems().isEmpty());
    }
}
