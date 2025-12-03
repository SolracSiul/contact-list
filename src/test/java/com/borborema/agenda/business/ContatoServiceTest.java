package com.borborema.agenda.business;

import com.borborema.agenda.infrastructure.entitys.Contato;
import com.borborema.agenda.infrastructure.entitys.user.User;
import com.borborema.agenda.infrastructure.entitys.user.UserRole;
import com.borborema.agenda.infrastructure.models.ContatoDTO;
import com.borborema.agenda.infrastructure.repository.ContatoRepository;
import com.borborema.agenda.infrastructure.repository.UserRepository;
import com.borborema.agenda.infrastructure.util.CriptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


class ContatoServiceTest {

    @Mock
    private ContatoRepository contatoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CriptoService criptoService;

    private ContatoService contatoService;

    private User testUser;
    private UUID testUserId;
    private KeyPair keyPair;
    private String publicKeyString;
    private String privateKeyString;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        testUserId = UUID.randomUUID();

        // Gerar um par de chaves RSA para testes
        keyPair = new CriptoService().generateRSAKeyPar();
        publicKeyString = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        privateKeyString = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        testUser = User.builder()
                .userId(testUserId)
                .email("test@example.com")
                .password("password123")
                .role(UserRole.USER)
                .publicKey(publicKeyString)
                .contatos(Arrays.asList())
                .build();

        // Criar instância manualmente e injetar os mocks
        contatoService = new ContatoService(contatoRepository);
        // Usar reflection para injetar os mocks que são @Autowired
        java.lang.reflect.Field userRepoField = ContatoService.class.getDeclaredField("userRepository");
        userRepoField.setAccessible(true);
        userRepoField.set(contatoService, userRepository);

        java.lang.reflect.Field criptoField = ContatoService.class.getDeclaredField("criptoService");
        criptoField.setAccessible(true);
        criptoField.set(contatoService, criptoService);
    }

    @Test
    void testSalvarContato() throws Exception {
        ContatoDTO contatoDTO = new ContatoDTO("João Silva", 123456789L, "joao@gmail.com", testUserId);
        String emailEncriptado = "emailEncriptado123";
        String nomeEncriptado = "nomeEncriptado456";
        String numeroEncriptado = "numeroEncriptado789";
        String tagEncriptada = "mrrvloyd";

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(criptoService.rsaEncrypt(eq("joao@gmail.com"), any(PublicKey.class))).thenReturn(emailEncriptado);
        when(criptoService.rsaEncrypt(eq("João Silva"), any(PublicKey.class))).thenReturn(nomeEncriptado);
        when(criptoService.rsaEncrypt(eq("123456789"), any(PublicKey.class))).thenReturn(numeroEncriptado);
        when(criptoService.cesarEncrypt(anyString(), anyInt())).thenReturn(tagEncriptada);
        when(contatoRepository.saveAndFlush(any(Contato.class))).thenAnswer(invocation -> invocation.getArgument(0));

        contatoService.salvarContato(contatoDTO);

        verify(userRepository, times(1)).findById(testUserId);
        verify(criptoService, times(1)).rsaEncrypt(eq("joao@gmail.com"), any(PublicKey.class));
        verify(criptoService, times(1)).rsaEncrypt(eq("João Silva"), any(PublicKey.class));
        verify(criptoService, times(1)).rsaEncrypt(eq("123456789"), any(PublicKey.class));
        verify(criptoService, times(1)).cesarEncrypt(eq("João Silva"), eq(3));
        verify(contatoRepository, times(1)).saveAndFlush(any(Contato.class));
    }

    @Test
    void testListarContatos() {
        List<Contato> contatos = Arrays.asList(
                Contato.builder().nome("encryptedNome1").numero("encryptedNumero1").build(),
                Contato.builder().nome("encryptedNome2").numero("encryptedNumero2").build()
        );

        when(contatoRepository.findAll()).thenReturn(contatos);

        List<Contato> resultado = contatoService.listarcontatos();

        assertEquals(2, resultado.size());
        assertEquals("encryptedNome1", resultado.get(0).getNome());
        assertEquals("encryptedNome2", resultado.get(1).getNome());
    }

    @Test
    void testFindContactsByUserId_Sucesso() {
        Contato contato1 = Contato.builder()
                .id(1)
                .nome("João Silva")
                .numero("123456789")
                .user(testUser)
                .build();

        Contato contato2 = Contato.builder()
                .id(2)
                .nome("Maria Santos")
                .numero("987654321")
                .user(testUser)
                .build();

        testUser.setContatos(Arrays.asList(contato1, contato2));

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        List<Contato> resultado = contatoService.findContactsByUserId(testUserId);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("João Silva", resultado.get(0).getNome());
        assertEquals("Maria Santos", resultado.get(1).getNome());
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    void testFindContactsByUserId_UsuarioNaoEncontrado() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            contatoService.findContactsByUserId(testUserId);
        });

        assertEquals("Usuario não encontrado", exception.getMessage());
    }

    @Test
    void testSalvarContato_UsuarioNaoEncontrado() {
        ContatoDTO contatoDTO = new ContatoDTO("João Silva", 123456789L, "joao@gmail.com", testUserId);

        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            contatoService.salvarContato(contatoDTO);
        });

        assertEquals("Usuario não encontrado", exception.getMessage());
    }
}