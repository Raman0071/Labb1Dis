package org.example.dao;

import org.example.model.Product;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** All product SQL belongs to the persistence layer. */
public class ProductDao {
    private final Database database;
    public ProductDao(Database database) { this.database = database; }
    public List<Product> findAll() throws SQLException {
        try (var connection = database.connect(); var query = connection.prepareStatement("SELECT * FROM products ORDER BY id"); var rows = query.executeQuery()) {
            List<Product> products = new ArrayList<>();
            while (rows.next()) { products.add(read(rows)); }
            return List.copyOf(products);
        }
    }
    public Optional<Product> findById(long id) throws SQLException {
        try (var connection = database.connect(); var query = connection.prepareStatement("SELECT * FROM products WHERE id = ?")) {
            query.setLong(1, id);
            try (var rows = query.executeQuery()) { return rows.next() ? Optional.of(read(rows)) : Optional.empty(); }
        }
    }
    private Product read(ResultSet row) throws SQLException {
        return new Product(row.getLong("id"), row.getString("name"), row.getString("description"), row.getBigDecimal("price"));
    }
}
