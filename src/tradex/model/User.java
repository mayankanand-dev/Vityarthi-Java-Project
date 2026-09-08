package tradex.model;

import java.time.LocalDateTime;

public class User {
    int id;
    String username;
    String passwordHash;
    String role;
    LocalDateTime createdAt;

    public User(int id, String username, String passwordHash, String role, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        if (role == null) {
            this.role = "TRADER";
        } else {
            this.role = role;
        }
        if (createdAt == null) {
            this.createdAt = LocalDateTime.now();
        } else {
            this.createdAt = createdAt;
        }
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
        if (role.equalsIgnoreCase("ADMIN")) {
            return true;
        }
        return false;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "User[id=" + id + ", username=" + username + ", role=" + role + "]";
    }
}
