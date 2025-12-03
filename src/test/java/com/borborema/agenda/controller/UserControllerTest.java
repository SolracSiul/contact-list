package com.borborema.agenda.controller;

import com.borborema.agenda.business.UserService;
import com.borborema.agenda.configuration.security.SecurityFilter;
import com.borborema.agenda.infrastructure.entitys.user.User;
import com.borborema.agenda.infrastructure.entitys.user.UserRole;
import com.borborema.agenda.infrastructure.models.UserAuthenticationDTO;
import com.borborema.agenda.infrastructure.models.UserDTO;
import com.borborema.agenda.infrastructure.models.UserRegisterDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void testCreateUser_Success() throws Exception {
        UserRegisterDTO registerDTO = new UserRegisterDTO("user@example.com", "senha123", UserRole.USER);
        String mockPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...";

        when(userService.createUser(any(UserRegisterDTO.class))).thenReturn(mockPrivateKey);

        mockMvc.perform(post("/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string(mockPrivateKey));
    }

    @Test
    void testCreateUser_Failure() throws Exception {
        UserRegisterDTO registerDTO = new UserRegisterDTO("user@example.com", "senha123", UserRole.USER);

        when(userService.createUser(any(UserRegisterDTO.class))).thenThrow(new RuntimeException("Erro ao criar usuário"));

        mockMvc.perform(post("/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Não foi possível cadastrar o usuario"));
    }

    @Test
    void testLogin_Success() throws Exception {
        UserAuthenticationDTO authDTO = new UserAuthenticationDTO("user@example.com", "senha123");
        String mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

        when(userService.authenticate(any(UserAuthenticationDTO.class))).thenReturn(mockToken);

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string(mockToken));
    }

    @Test
    void testLogin_Failure() throws Exception {
        UserAuthenticationDTO authDTO = new UserAuthenticationDTO("user@example.com", "senhaErrada");

        when(userService.authenticate(any(UserAuthenticationDTO.class))).thenThrow(new RuntimeException("Credenciais inválidas"));

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Não foi possível realizar o login"));
    }

    @Test
    void testGetUsers_Success() throws Exception {
        User user1 = User.builder()
                .userId(UUID.randomUUID())
                .email("user1@example.com")
                .role(UserRole.USER)
                .build();

        User user2 = User.builder()
                .userId(UUID.randomUUID())
                .email("user2@example.com")
                .role(UserRole.ADMIN)
                .build();

        List<User> users = Arrays.asList(user1, user2);

        when(userService.listUsers()).thenReturn(users);

        mockMvc.perform(get("/user/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$[1].email").value("user2@example.com"));
    }

    @Test
    void testGetUsers_EmptyList() throws Exception {
        when(userService.listUsers()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/user/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}