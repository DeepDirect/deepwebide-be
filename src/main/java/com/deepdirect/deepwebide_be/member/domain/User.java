package com.deepdirect.deepwebide_be.member.domain;

import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.domain.RepositoryFavorite;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString(exclude = "password")
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 1000)
    private String profileImageUrl;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "owner")
    private List<Repository> repositories = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepositoryFavorite> favorites = new ArrayList<>();



    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public User(String username, String nickname, String email, String phoneNumber, String password, String profileImageUrl) {
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
    }
}
