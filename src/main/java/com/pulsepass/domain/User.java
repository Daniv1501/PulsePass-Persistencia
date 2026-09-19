package com.pulsepass.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean active;

    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private UserProfile profile;
    
    @OneToMany(mappedBy = "user")
    private List<Ticket> tickets = new ArrayList<>();

    protected User() {
        // requerido por JPA
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.active = true;
    }

    public void assignProfile(UserProfile profile) {
        this.profile = profile;
        profile.setUser(this);
    }

    // ---- Getters y Setters ----

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public UserProfile getProfile() {
        return profile;
    }
    
    public List<Ticket> getTickets() {
        return tickets;
    }
}
