package com.borborema.agenda.controller;

import com.borborema.agenda.business.ContatoService;
import com.borborema.agenda.configuration.security.SecurityFilter;
import com.borborema.agenda.infrastructure.entitys.Contato;
import com.borborema.agenda.infrastructure.entitys.user.User;
import com.borborema.agenda.infrastructure.entitys.user.UserRole;
import com.borborema.agenda.infrastructure.models.ContatoDAO;
import com.borborema.agenda.infrastructure.models.ContatoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ContatoController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class ContatoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContatoService contatoService;

    private UUID testUserId;
    private String mockPrivateKey;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        mockPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...";
    }

    @Test
    void testSalvarContato_Success() throws Exception {
        ContatoDTO contatoDTO = new ContatoDTO("João Silva", 123456789L, "joao@gmail.com", testUserId);

        doNothing().when(contatoService).salvarContato(any(ContatoDTO.class));

        mockMvc.perform(post("/agenda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contatoDTO)))
                .andExpect(status().isOk());

        verify(contatoService, times(1)).salvarContato(any(ContatoDTO.class));
    }

    @Test
    void testSalvarContato_Failure() throws Exception {
        ContatoDTO contatoDTO = new ContatoDTO("João Silva", 123456789L, "joao@gmail.com", testUserId);

        doThrow(new RuntimeException("Erro ao salvar")).when(contatoService).salvarContato(any(ContatoDTO.class));

        mockMvc.perform(post("/agenda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contatoDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Não foi possível cadastrar o contato"));
    }

    @Test
    void testFindUserContacts_Success() throws Exception {
        User testUser = User.builder()
                .userId(testUserId)
                .email("test@example.com")
                .role(UserRole.USER)
                .build();

        Contato contato1 = Contato.builder()
                .id(1)
                .nome("encryptedNome1")
                .numero("encryptedNumero1")
                .email("encryptedEmail1")
                .user(testUser)
                .build();

        Contato contato2 = Contato.builder()
                .id(2)
                .nome("encryptedNome2")
                .numero("encryptedNumero2")
                .email("encryptedEmail2")
                .user(testUser)
                .build();

        List<Contato> contatos = Arrays.asList(contato1, contato2);

        when(contatoService.findContactsByUserId(testUserId)).thenReturn(contatos);

        mockMvc.perform(get("/agenda/list/user/contacts")
                        .param("userId", testUserId.toString())
                        .param("stringPrivateKey", mockPrivateKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(contatoService, times(1)).findContactsByUserId(testUserId);
    }

    @Test
    void testFindUserContacts_Failure() throws Exception {
        when(contatoService.findContactsByUserId(testUserId)).thenThrow(new RuntimeException("Usuário não encontrado"));

        mockMvc.perform(get("/agenda/list/user/contacts")
                        .param("userId", testUserId.toString())
                        .param("stringPrivateKey", mockPrivateKey))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Não foi possível retornar os contatos"));
    }

    @Test
    void testBuscarContatoPeloNumero_Success() throws Exception {
        Long numero = 123456789L;

        Contato contato = Contato.builder()
                .id(1)
                .nome("encryptedNome")
                .numero("encryptedNumero")
                .email("encryptedEmail")
                .build();

        when(contatoService.buscarContatoUsuarioPorNumero(eq(numero), eq(testUserId), anyString()))
                .thenReturn(contato);

        mockMvc.perform(get("/agenda/list/user/contact/number")
                        .param("numero", numero.toString())
                        .param("userId", testUserId.toString())
                        .param("stringPrivateKey", mockPrivateKey))
                .andExpect(status().isOk());

        verify(contatoService, times(1)).buscarContatoUsuarioPorNumero(numero, testUserId, mockPrivateKey);
    }

    @Test
    void testBuscarContatoPeloNumero_Failure() throws Exception {
        Long numero = 123456789L;

        when(contatoService.buscarContatoUsuarioPorNumero(eq(numero), eq(testUserId), anyString()))
                .thenThrow(new RuntimeException("Contato não encontrado"));

        mockMvc.perform(get("/agenda/list/user/contact/number")
                        .param("numero", numero.toString())
                        .param("userId", testUserId.toString())
                        .param("stringPrivateKey", mockPrivateKey))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Não foi possível encontrar o contato pelo número"));
    }

    @Test
    void testBuscarContatoPeloEmail_Success() throws Exception {
        String email = "joao@gmail.com";

        Contato contato = Contato.builder()
                .id(1)
                .nome("encryptedNome")
                .numero("encryptedNumero")
                .email("encryptedEmail")
                .build();

        when(contatoService.buscarContatoUsuarioPeloEmail(eq(email), eq(testUserId), anyString()))
                .thenReturn(contato);

        mockMvc.perform(get("/agenda/list/user/contact/email")
                        .param("email", email)
                        .param("userId", testUserId.toString())
                        .param("stringPrivateKey", mockPrivateKey))
                .andExpect(status().isOk());

        verify(contatoService, times(1)).buscarContatoUsuarioPeloEmail(email, testUserId, mockPrivateKey);
    }

    @Test
    void testBuscarContatoPeloEmail_Failure() throws Exception {
        String email = "joao@gmail.com";

        when(contatoService.buscarContatoUsuarioPeloEmail(eq(email), eq(testUserId), anyString()))
                .thenThrow(new RuntimeException("Contato não encontrado"));

        mockMvc.perform(get("/agenda/list/user/contact/email")
                        .param("email", email)
                        .param("userId", testUserId.toString())
                        .param("stringPrivateKey", mockPrivateKey))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Não foi possível encontrar o contato pelo e-mail"));
    }

    @Test
    void testDeletarContatoPeloNumero_Success() throws Exception {
        Long numero = 123456789L;

        doNothing().when(contatoService).deletarUsuarioContatoPorNumero(numero, testUserId, mockPrivateKey);

        mockMvc.perform(delete("/agenda")
                        .param("numero", numero.toString())
                        .param("userId", testUserId.toString())
                        .param("stringPrivateKey", mockPrivateKey))
                .andExpect(status().isOk());

        verify(contatoService, times(1)).deletarUsuarioContatoPorNumero(numero, testUserId, mockPrivateKey);
    }

    @Test
    void testDeletarContatoPeloNumero_Failure() throws Exception {
        Long numero = 123456789L;

        doThrow(new RuntimeException("Erro ao deletar")).when(contatoService)
                .deletarUsuarioContatoPorNumero(numero, testUserId, mockPrivateKey);

        mockMvc.perform(delete("/agenda")
                        .param("numero", numero.toString())
                        .param("userId", testUserId.toString())
                        .param("stringPrivateKey", mockPrivateKey))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Não foi possível deletar o contato"));
    }

    @Test
    void testAtualizarContatoPeloNumero_Success() throws Exception {
        Long numero = 123456789L;
        ContatoDTO contatoDTO = new ContatoDTO("João Pedro Silva", 987654321L, "joaonovo@gmail.com", testUserId);

        doNothing().when(contatoService).atualizarContatoPorNumero(eq(numero), any(ContatoDTO.class), anyString());

        mockMvc.perform(put("/agenda")
                        .param("numero", numero.toString())
                        .param("stringPrivateKey", mockPrivateKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contatoDTO)))
                .andExpect(status().isOk());

        verify(contatoService, times(1)).atualizarContatoPorNumero(eq(numero), any(ContatoDTO.class), eq(mockPrivateKey));
    }

    @Test
    void testAtualizarContatoPeloNumero_Failure() throws Exception {
        Long numero = 123456789L;
        ContatoDTO contatoDTO = new ContatoDTO("João Pedro Silva", 987654321L, "joaonovo@gmail.com", testUserId);

        doThrow(new RuntimeException("Erro ao atualizar")).when(contatoService)
                .atualizarContatoPorNumero(eq(numero), any(ContatoDTO.class), anyString());

        mockMvc.perform(put("/agenda")
                        .param("numero", numero.toString())
                        .param("stringPrivateKey", mockPrivateKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contatoDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Não foi possível atualizar o contato"));
    }
}