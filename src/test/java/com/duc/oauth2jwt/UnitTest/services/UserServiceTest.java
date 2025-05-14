package com.duc.oauth2jwt.UnitTest.services;

import com.duc.oauth2jwt.dto.UserDto;
import com.duc.oauth2jwt.exception.ResourceNotFoundException;
import com.duc.oauth2jwt.model.Role;
import com.duc.oauth2jwt.model.User;
import com.duc.oauth2jwt.repository.RoleRepository;
import com.duc.oauth2jwt.repository.UserRepository;
import com.duc.oauth2jwt.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;
    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // Setup roles
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName(Role.RoleName.ROLE_USER);

        adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName(Role.RoleName.ROLE_ADMIN);

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setImageUrl("http://example.com/image.jpg");
        testUser.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getCurrentUser_Success() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        UserDto result = userService.getCurrentUser();

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getImageUrl(), result.getImageUrl());
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains("ROLE_USER"));

        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getCurrentUser_UserNotFound() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getCurrentUser());

        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        // Act
        UserDto result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_UserNotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));

        verify(userRepository).findById(999L);
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setName("User Two");
        user2.setRoles(Collections.singleton(userRole));

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        // Act
        List<UserDto> results = userService.getAllUsers();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(testUser.getId(), results.get(0).getId());
        assertEquals(user2.getId(), results.get(1).getId());

        verify(userRepository).findAll();
    }

    @Test
    void giveAdminRole_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName(Role.RoleName.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDto result = userService.giveAdminRole(1L);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(roleRepository).findByName(Role.RoleName.ROLE_ADMIN);
        verify(userRepository).save(testUser);
    }

    @Test
    void giveAdminRole_UserNotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.giveAdminRole(999L));

        verify(userRepository).findById(999L);
        verifyNoInteractions(roleRepository);
    }

    @Test
    void removeAdminRole_Success() {
        // Arrange
        // First set up a user with both roles
        testUser.setRoles(new HashSet<>(Arrays.asList(userRole, adminRole)));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName(Role.RoleName.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName(Role.RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDto result = userService.removeAdminRole(1L);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(roleRepository).findByName(Role.RoleName.ROLE_ADMIN);
        verify(roleRepository).findByName(Role.RoleName.ROLE_USER);
        verify(userRepository).save(testUser);
    }

    @Test
    void removeAdminRole_UserNotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.removeAdminRole(999L));

        verify(userRepository).findById(999L);
        verifyNoInteractions(roleRepository);
    }
}
