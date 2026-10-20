package org.example.service;

import org.example.dao.ProductDao;
import org.example.dao.UserDao;
import org.example.dto.CartDTO;
import org.example.dto.CartItemDTO;
import org.example.dto.ProductDTO;
import org.example.dto.UserDTO;
import org.example.model.Cart;
import org.example.model.Product;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Business layer: authentication, product lookup and cart validation; no servlet API. */
public class ShopService {
    private final ProductDao products;
    private final UserDao users;
    private final String dummyHash = Passwords.hash("unused-password");
    public ShopService(ProductDao products, UserDao users) { this.products = products; this.users = users; }
    public List<ProductDTO> listProducts() throws SQLException {
        return products.findAll().stream().map(this::toProductDTO).toList();
    }
    public Optional<UserDTO> login(String username, String password) throws SQLException {
        if (username == null || password == null || username.length() > 80 || password.length() > 256) { return Optional.empty(); }
        var credentials = users.findByUsername(username.trim());
        boolean valid = Passwords.matches(password, credentials.map(UserDao.Credentials::passwordHash).orElse(dummyHash));
        return valid ? credentials.map(c -> new UserDTO(c.user().getId(), c.user().getUsername())) : Optional.empty();
    }
    public void addToCart(Cart cart, long productId, int quantity) throws SQLException {
        Product product = products.findById(productId).orElseThrow(() -> new IllegalArgumentException("Produkten finns inte."));
        cart.add(product, quantity);
    }
    public void removeFromCart(Cart cart, long productId) { cart.remove(productId); }

    /** Map under the cart lock so rows, count and total describe the same instant. */
    public CartDTO getCart(Cart cart) {
        synchronized (cart) {
            List<CartItemDTO> items = cart.getItems().stream()
                    .map(item -> new CartItemDTO(toProductDTO(item.getProduct()), item.getQuantity(), item.getSubtotal()))
                    .toList();
            return new CartDTO(items, cart.getCount(), cart.getTotal());
        }
    }
    private ProductDTO toProductDTO(Product product) {
        return new ProductDTO(product.getId(), product.getName(), product.getDescription(), product.getPrice());
    }
}
