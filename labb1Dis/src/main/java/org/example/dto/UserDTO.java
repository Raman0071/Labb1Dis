package org.example.dto;

/** Public identity shared with the web layer. Never contains credentials or a password hash. */
public final class UserDTO {
    private final long id;
    private final String username;

    public UserDTO(long id, String username) { this.id = id; this.username = username; }
    public long getId() { return id; }
    public String getUsername() { return username; }
}
