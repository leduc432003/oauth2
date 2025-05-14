package com.duc.oauth2jwt.UnitTest.controller;

import com.duc.oauth2jwt.controller.AdminController;
import com.duc.oauth2jwt.dto.UserDto;
import com.duc.oauth2jwt.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(adminController)
                .build();
    }

    @Test
    public void testGetAllUsers() throws Exception {
        // Arrange
        UserDto user1 = new UserDto();
        user1.setId(1L);
        user1.setEmail("user1@example.com");
        user1.setName("User One");

        UserDto user2 = new UserDto();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setName("User Two");

        List<UserDto> userList = Arrays.asList(user1, user2);

        when(userService.getAllUsers()).thenReturn(userList);

        // Act & Assert
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].email").value("user2@example.com"));
    }

    @Test
    public void testGiveAdminRole() throws Exception {
        // Arrange
        UserDto updatedUser = new UserDto();
        updatedUser.setId(1L);
        updatedUser.setEmail("user1@example.com");
        updatedUser.setName("User One");
        updatedUser.setRoles(Set.of("ROLE_USER", "ROLE_ADMIN"));

        when(userService.giveAdminRole(anyLong())).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/admin/users/1/give-admin"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user1@example.com"))
                .andExpect(jsonPath("$.roles.length()").value(2))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    public void testRemoveAdminRole() throws Exception {
        // Arrange
        UserDto updatedUser = new UserDto();
        updatedUser.setId(1L);
        updatedUser.setEmail("user1@example.com");
        updatedUser.setName("User One");
        updatedUser.setRoles(Set.of("ROLE_USER"));

        when(userService.removeAdminRole(anyLong())).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/admin/users/1/remove-admin"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user1@example.com"))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }
}
