package com.duc.oauth2jwt.UnitTest.controller;

import com.duc.oauth2jwt.controller.UserController;
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
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();
    }

    @Test
    public void testGetCurrentUser() throws Exception {
        // Arrange
        UserDto currentUser = new UserDto();
        currentUser.setId(1L);
        currentUser.setEmail("current@example.com");
        currentUser.setName("Current User");
        currentUser.setRoles(Set.of("ROLE_USER"));

        when(userService.getCurrentUser()).thenReturn(currentUser);

        // Act & Assert
        mockMvc.perform(get("/user/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("current@example.com"))
                .andExpect(jsonPath("$.name").value("Current User"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    public void testGetUserById() throws Exception {
        // Arrange
        UserDto user = new UserDto();
        user.setId(2L);
        user.setEmail("user2@example.com");
        user.setName("User Two");
        user.setRoles(Set.of("ROLE_USER"));

        when(userService.getUserById(anyLong())).thenReturn(user);

        // Act & Assert
        mockMvc.perform(get("/user/2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.email").value("user2@example.com"))
                .andExpect(jsonPath("$.name").value("User Two"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }
}
