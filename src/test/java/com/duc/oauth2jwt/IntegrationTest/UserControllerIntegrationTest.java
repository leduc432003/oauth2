package com.duc.oauth2jwt.IntegrationTest;

import com.duc.oauth2jwt.dto.AuthResponse;
import com.duc.oauth2jwt.dto.RegisterRequest;
import com.duc.oauth2jwt.dto.UserDto;
import com.duc.oauth2jwt.model.Role;
import com.duc.oauth2jwt.repository.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    private String userAccessToken;
    private Long userId;

    @BeforeEach
    public void setup() throws Exception {
        // Ensure required roles exist in database
        if (!roleRepository.findByName(Role.RoleName.ROLE_USER).isPresent()) {
            Role userRole = new Role();
            userRole.setName(Role.RoleName.ROLE_USER);
            roleRepository.save(userRole);
        }

        if (!roleRepository.findByName(Role.RoleName.ROLE_ADMIN).isPresent()) {
            Role adminRole = new Role();
            adminRole.setName(Role.RoleName.ROLE_ADMIN);
            roleRepository.save(adminRole);
        }

        // Register a user
        RegisterRequest userRegister = new RegisterRequest();
        userRegister.setName("Test User");
        userRegister.setEmail("user-test@example.com");
        userRegister.setPassword("Password123");

        MvcResult userResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegister)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse userResponse = objectMapper.readValue(
                userResult.getResponse().getContentAsString(), AuthResponse.class);
        userAccessToken = userResponse.getAccessToken();
        userId = userResponse.getUser().getId();
    }

    @Test
    public void testGetCurrentUser() throws Exception {
        MvcResult result = mockMvc.perform(get("/user/me")
                        .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andReturn();

        UserDto user = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserDto.class);

        assertNotNull(user);
        assertEquals("user-test@example.com", user.getEmail());
        assertEquals("Test User", user.getName());
        assertTrue(user.getRoles().contains("ROLE_USER"));
    }

    @Test
    public void testGetUserById() throws Exception {
        MvcResult result = mockMvc.perform(get("/user/{id}", userId)
                        .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andReturn();

        UserDto user = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserDto.class);

        assertNotNull(user);
        assertEquals(userId, user.getId());
        assertEquals("user-test@example.com", user.getEmail());
    }

    @Test
    public void testGetUserByIdNotFound() throws Exception {
        mockMvc.perform(get("/user/{id}", 99999L)
                        .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetUserWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/user/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetUserWithInvalidToken() throws Exception {
        mockMvc.perform(get("/user/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetUserByIdWithAuthentication() throws Exception {
        // Register another user
        RegisterRequest anotherRegister = new RegisterRequest();
        anotherRegister.setName("Another User");
        anotherRegister.setEmail("another-test@example.com");
        anotherRegister.setPassword("Password123");

        MvcResult anotherResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(anotherRegister)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse anotherResponse = objectMapper.readValue(
                anotherResult.getResponse().getContentAsString(), AuthResponse.class);
        Long anotherUserId = anotherResponse.getUser().getId();

        // User should be able to get information of another user
        mockMvc.perform(get("/user/{id}", anotherUserId)
                        .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("another-test@example.com"));
    }
}
