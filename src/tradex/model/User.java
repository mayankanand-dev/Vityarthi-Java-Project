package tradex.model;

import java.time.LocalDateTime;

public class User {
    private final int id;
    private final String username;
    private final String passwordHash;
    private final String role; // "TRADER" or "ADMIN"
    private final LocalDateTime createdAt;

    public User(int id, String username, String passwordHash, String role, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role != null ? role : "TRADER";
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public User(String username, String passwordHash, String role) {
        this(0, username, passwordHash, role, LocalDateTime.now());
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return String.format("User[id=%d, username='%s', role='%s']", id, username, role);
    }
}
