package com.duc.oauth2jwt.IntegrationTest;

import com.duc.oauth2jwt.dto.AuthRequest;
import com.duc.oauth2jwt.dto.AuthResponse;
import com.duc.oauth2jwt.dto.RegisterRequest;
import com.duc.oauth2jwt.dto.UserDto;
import com.duc.oauth2jwt.model.Role;
import com.duc.oauth2jwt.repository.RoleRepository;
import com.fasterxml.jackson.core.type.TypeReference;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    private String adminAccessToken;
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

        // Register an admin user
        RegisterRequest adminRegister = new RegisterRequest();
        adminRegister.setName("Admin User");
        adminRegister.setEmail("admin@example.com");
        adminRegister.setPassword("Password123");

        MvcResult adminResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRegister)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse adminResponse = objectMapper.readValue(
                adminResult.getResponse().getContentAsString(), AuthResponse.class);
        adminAccessToken = adminResponse.getAccessToken();

        // Manually make this user an admin by calling the endpoint with this token
        // We need to grant admin privilege to the first created user
        // This is a bootstrap process for the first admin
        MvcResult giveAdminResult = mockMvc.perform(put("/admin/users/{id}/give-admin", adminResponse.getUser().getId())
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andReturn();

        UserDto adminUserDto = objectMapper.readValue(
                giveAdminResult.getResponse().getContentAsString(), UserDto.class);
        assertTrue(adminUserDto.getRoles().contains("ROLE_ADMIN"));

        // Login again to get updated token with admin role
        AuthRequest adminLogin = new AuthRequest();
        adminLogin.setEmail("admin@example.com");
        adminLogin.setPassword("Password123");

        MvcResult adminLoginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        adminResponse = objectMapper.readValue(
                adminLoginResult.getResponse().getContentAsString(), AuthResponse.class);
        adminAccessToken = adminResponse.getAccessToken();

        // Register a regular user
        RegisterRequest userRegister = new RegisterRequest();
        userRegister.setName("Regular User");
        userRegister.setEmail("user@example.com");
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
    public void testGetAllUsers() throws Exception {
        MvcResult result = mockMvc.perform(get("/admin/users")
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andReturn();

        List<UserDto> users = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<UserDto>>() {});

        assertNotNull(users);
        assertTrue(users.size() >= 2); // At least admin and regular user
    }

    @Test
    public void testGetAllUsersUnauthorized() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGiveAdminRole() throws Exception {
        mockMvc.perform(put("/admin/users/{id}/give-admin", userId)
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[?(@=='ROLE_ADMIN')]").exists());
    }

    @Test
    public void testRemoveAdminRole() throws Exception {
        // First give admin role
        mockMvc.perform(put("/admin/users/{id}/give-admin", userId)
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk());

        // Then remove admin role
        mockMvc.perform(put("/admin/users/{id}/remove-admin", userId)
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[?(@=='ROLE_ADMIN')]").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[?(@=='ROLE_USER')]").exists());
    }

    @Test
    public void testGiveAdminRoleUnauthorized() throws Exception {
        mockMvc.perform(put("/admin/users/{id}/give-admin", userId)
                        .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testRemoveAdminRoleUnauthorized() throws Exception {
        mockMvc.perform(put("/admin/users/{id}/remove-admin", userId)
                        .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isForbidden());
    }
}
