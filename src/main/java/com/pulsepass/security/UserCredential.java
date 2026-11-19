package com.pulsepass.security;

import com.pulsepass.domain.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_credentials")
public class UserCredential {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "password_changed_at", nullable = false)
    private LocalDateTime passwordChangedAt;

    @ElementCollection
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    protected UserCredential() {}

    public UserCredential(User user, String passwordHash, Set<Role> roles) {
        this.user = user;
        this.passwordHash = passwordHash;
        this.roles.addAll(roles);
        this.passwordChangedAt = LocalDateTime.now();
    }

    public Long getUserId() { return userId; }
    public User getUser() { return user; }
    public String getPasswordHash() { return passwordHash; }
    public Set<Role> getRoles() { return Set.copyOf(roles); }
}
