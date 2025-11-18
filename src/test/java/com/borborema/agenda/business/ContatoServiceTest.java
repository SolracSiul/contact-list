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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
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

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .userId(testUserId)
                .email("test@example.com")
                .password("password123")
                .role(UserRole.USER)
                .contatos(Arrays.asList())
                .build();

        // Gerar um par de chaves RSA para testes
        keyPair = new CriptoService().generateRSAKeyPar();

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
        ContatoDTO contatoDTO = new ContatoDTO("João Silva", 123456789L, "Rua A, 123", testUserId);
        String publicKeyString = java.util.Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String enderecoEncriptado = "enderecoEncriptado123";
        String tagEncriptada = "mrxrvloyd";

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(criptoService.rsaEncrypt(anyString(), any(PublicKey.class))).thenReturn(enderecoEncriptado);
        when(criptoService.cesarEncrypt(anyString(), anyInt())).thenReturn(tagEncriptada);
        when(contatoRepository.saveAndFlush(any(Contato.class))).thenAnswer(invocation -> invocation.getArgument(0));

        contatoService.salvarContato(contatoDTO, publicKeyString);

        verify(userRepository, times(1)).findById(testUserId);
        verify(criptoService, times(1)).rsaEncrypt(eq("Rua A, 123"), any(PublicKey.class));
        verify(criptoService, times(1)).cesarEncrypt(eq("João Silva"), eq(3));
        verify(contatoRepository, times(1)).saveAndFlush(any(Contato.class));
    }

    @Test
    void testListarContatos() {
        List<Contato> contatos = Arrays.asList(
                Contato.builder().nome("João").numero(111111111L).build(),
                Contato.builder().nome("Maria").numero(222222222L).build()
        );

        when(contatoRepository.findAll()).thenReturn(contatos);

        List<Contato> resultado = contatoService.listarcontatos();

        assertEquals(2, resultado.size());
        assertEquals("João", resultado.get(0).getNome());
        assertEquals("Maria", resultado.get(1).getNome());
    }

    @Test
    void testFindContactsByUserId_Sucesso() {
        Contato contato1 = Contato.builder()
                .id(1)
                .nome("João Silva")
                .numero(123456789L)
                .user(testUser)
                .build();

        Contato contato2 = Contato.builder()
                .id(2)
                .nome("Maria Santos")
                .numero(987654321L)
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
    void testBuscarContatoUsuarioPorNumero_Sucesso() {
        Long numero = 123456789L;
        Contato contato = Contato.builder()
                .id(1)
                .nome("João Silva")
                .numero(numero)
                .user(testUser)
                .build();

        testUser.setContatos(Arrays.asList(contato));

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        Contato resultado = contatoService.buscarContatoUsuarioPorNumero(numero, testUserId);

        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNome());
        assertEquals(numero, resultado.getNumero());
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    void testBuscarContatoUsuarioPorNumero_UsuarioNaoEncontrado() {
        Long numero = 123456789L;
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            contatoService.buscarContatoUsuarioPorNumero(numero, testUserId);
        });

        assertEquals("Usuario não encontrado", exception.getMessage());
    }

    @Test
    void testDeletarUsuarioContatoPorNumero() {
        Long numero = 123456789L;
        doNothing().when(contatoRepository).deleteByNumeroAndUser_UserId(numero, testUserId);

        contatoService.deletarUsuarioContatoPorNumero(numero, testUserId);

        verify(contatoRepository, times(1)).deleteByNumeroAndUser_UserId(numero, testUserId);
    }

    @Test
    void testAtualizarContatoPorNumero_AtualizaNomeENumero() {
        Long numeroAntigo = 123456789L;
        Long numeroNovo = 987654321L;

        Contato contatoExistente = Contato.builder()
                .id(1)
                .nome("João Silva")
                .numero(numeroAntigo)
                .user(testUser)
                .build();

        testUser.setContatos(Arrays.asList(contatoExistente));

        ContatoDTO contatoDTO = new ContatoDTO("João Pedro Silva", numeroNovo, "Rua B, 456", testUserId);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(contatoRepository.saveAndFlush(any(Contato.class))).thenAnswer(invocation -> invocation.getArgument(0));

        contatoService.atualizarContatoPorNumero(numeroAntigo, contatoDTO);

        verify(userRepository, times(1)).findById(testUserId);
        verify(contatoRepository, times(1)).saveAndFlush(any(Contato.class));
    }

    @Test
    void testAtualizarContatoPorNumero_AtualizaSoNome() {
        Long numero = 123456789L;

        Contato contatoExistente = Contato.builder()
                .id(1)
                .nome("João Silva")
                .numero(numero)
                .user(testUser)
                .build();

        testUser.setContatos(Arrays.asList(contatoExistente));

        ContatoDTO contatoDTO = new ContatoDTO("João Pedro Silva", 0L, "Rua B, 456", testUserId);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(contatoRepository.saveAndFlush(any(Contato.class))).thenAnswer(invocation -> invocation.getArgument(0));

        contatoService.atualizarContatoPorNumero(numero, contatoDTO);

        verify(userRepository, times(1)).findById(testUserId);
        verify(contatoRepository, times(1)).saveAndFlush(any(Contato.class));
    }
}