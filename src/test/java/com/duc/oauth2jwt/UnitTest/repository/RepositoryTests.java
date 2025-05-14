package com.duc.oauth2jwt.UnitTest.repository;

import com.duc.oauth2jwt.model.RefreshToken;
import com.duc.oauth2jwt.model.Role;
import com.duc.oauth2jwt.model.User;
import com.duc.oauth2jwt.repository.RefreshTokenRepository;
import com.duc.oauth2jwt.repository.RoleRepository;
import com.duc.oauth2jwt.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class RepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    public void testFindUserByEmail() {
        // Arrange
        Role userRole = Role.builder().name(Role.RoleName.ROLE_USER).build();
        entityManager.persist(userRole);

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
                .email("test@example.com")
                .name("Test User")
                .password("password")
                .provider(User.AuthProvider.LOCAL)
                .roles(roles)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // Act
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getName()).isEqualTo("Test User");
    }

    @Test
    public void testExistsByEmail() {
        // Arrange
        User user = User.builder()
                .email("exists@example.com")
                .name("Existing User")
                .password("password")
                .provider(User.AuthProvider.LOCAL)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // Act & Assert
        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    public void testFindUserByProviderAndProviderId() {
        // Arrange
        User user = User.builder()
                .email("google@example.com")
                .name("Google User")
                .provider(User.AuthProvider.GOOGLE)
                .providerId("google123")
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // Act
        Optional<User> found = userRepository.findByProviderAndProviderId(
                User.AuthProvider.GOOGLE, "google123");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("google@example.com");
        assertThat(found.get().getProviderId()).isEqualTo("google123");
    }

    @Test
    public void testFindRoleByName() {
        // Arrange
        Role adminRole = Role.builder().name(Role.RoleName.ROLE_ADMIN).build();
        entityManager.persist(adminRole);
        entityManager.flush();

        // Act
        Optional<Role> found = roleRepository.findByName(Role.RoleName.ROLE_ADMIN);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(Role.RoleName.ROLE_ADMIN);
    }

    @Test
    public void testFindRefreshTokenByToken() {
        // Arrange
        User user = User.builder()
                .email("token@example.com")
                .name("Token User")
                .password("password")
                .provider(User.AuthProvider.LOCAL)
                .build();
        entityManager.persist(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token("test-token-value")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(86400))
                .build();
        entityManager.persist(refreshToken);
        entityManager.flush();

        // Act
        Optional<RefreshToken> found = refreshTokenRepository.findByToken("test-token-value");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getToken()).isEqualTo("test-token-value");
        assertThat(found.get().getUser().getEmail()).isEqualTo("token@example.com");
    }

    @Test
    public void testFindRefreshTokenByUser() {
        // Arrange
        User user = User.builder()
                .email("user-token@example.com")
                .name("User Token")
                .password("password")
                .provider(User.AuthProvider.LOCAL)
                .build();
        entityManager.persist(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token("user-associated-token")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(86400))
                .build();
        entityManager.persist(refreshToken);
        entityManager.flush();

        // Act
        Optional<RefreshToken> found = refreshTokenRepository.findByUser(user);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getToken()).isEqualTo("user-associated-token");
    }

    @Test
    public void testDeleteRefreshTokenByUser() {
        // Arrange
        User user = User.builder()
                .email("delete-token@example.com")
                .name("Delete Token User")
                .password("password")
                .provider(User.AuthProvider.LOCAL)
                .build();
        entityManager.persist(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token("delete-this-token")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(86400))
                .build();
        entityManager.persist(refreshToken);
        entityManager.flush();

        // Act
        refreshTokenRepository.deleteByUser(user);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<RefreshToken> found = refreshTokenRepository.findByToken("delete-this-token");
        assertThat(found).isEmpty();
    }
}
