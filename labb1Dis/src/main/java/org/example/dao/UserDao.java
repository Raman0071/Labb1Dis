package org.example.dao;

import org.example.model.User;
import java.sql.SQLException;
import java.util.Optional;

/** Credentials remain in the DAO/service layers and never enter the HTTP session. */
public class UserDao {
    public record Credentials(User user, String passwordHash) { }
    private final Database database;
    public UserDao(Database database) { this.database = database; }
    public Optional<Credentials> findByUsername(String username) throws SQLException {
        try (var connection = database.connect(); var query = connection.prepareStatement("SELECT id, username, password_hash FROM users WHERE username = ?")) {
            query.setString(1, username);
            try (var rows = query.executeQuery()) {
                return rows.next() ? Optional.of(new Credentials(new User(rows.getLong("id"), rows.getString("username")), rows.getString("password_hash"))) : Optional.empty();
            }
        }
    }
}
