# Guia de Testes - Agenda Application

## Visão Geral

Este documento descreve **todos os testes** implementados para a aplicação Agenda (branch `feature/criptografia`).

A aplicação possui **3 tipos de testes** conforme solicitado:
1. **Testes Unitários** - 23 testes
2. **Testes de Integração** - 4 testes
3. **Testes de Sistema Automatizados** - 8 testes

**Total**: **35 testes** - 100% passando

---

## Resultado da Última Execução

```bash
./mvnw test
```

```
[INFO] Tests run: 35, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time:  18.863 s
```

**Status**: Todos os 35 testes passando

---

## 1. TESTES UNITÁRIOS (23 testes)

### Definição
**Testes unitários** testam componentes individuais de forma isolada, utilizando mocks para simular dependências externas. São rápidos e focados em lógica de negócio.

### Arquivos de Teste

#### 1.1 ContatoServiceTest.java (9 testes)
**Localização**: `src/test/java/com/borborema/agenda/business/ContatoServiceTest.java`

**Objetivo**: Testar a lógica de negócio do serviço de contatos

**Dependências Mockadas**:
- `ContatoRepository`
- `UserRepository`
- `CriptoService`

**Testes Implementados**:

| # | Nome do Teste | Descrição |
|---|---------------|-----------|
| 1 | `testSalvarContato()` | Valida criação de contato com criptografia |
| 2 | `testListarContatos()` | Valida listagem de todos os contatos |
| 3 | `testFindContactsByUserId_Sucesso()` | Busca contatos de um usuário específico |
| 4 | `testFindContactsByUserId_UsuarioNaoEncontrado()` | Valida exceção quando usuário não existe |
| 5 | `testBuscarContatoUsuarioPorNumero_Sucesso()` | Busca contato pelo número |
| 6 | `testBuscarContatoUsuarioPorNumero_UsuarioNaoEncontrado()` | Valida exceção na busca |
| 7 | `testDeletarUsuarioContatoPorNumero()` | Valida deleção de contato |
| 8 | `testAtualizarContatoPorNumero_AtualizaNomeENumero()` | Atualiza nome e número |
| 9 | `testAtualizarContatoPorNumero_AtualizaSoNome()` | Atualiza apenas o nome |

**Exemplo de Teste**:
```java
@Test
void testSalvarContato() throws Exception {
    ContatoDTO contatoDTO = new ContatoDTO("João Silva", 123456789L, "Rua A", testUserId);
    String publicKeyString = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(criptoService.rsaEncrypt(anyString(), any(PublicKey.class))).thenReturn("encrypted");
    when(criptoService.cesarEncrypt(anyString(), anyInt())).thenReturn("tag");

    contatoService.salvarContato(contatoDTO, publicKeyString);

    verify(contatoRepository, times(1)).saveAndFlush(any(Contato.class));
}
```

---

#### 1.2 CriptoServiceTest.java (13 testes)
**Localização**: `src/test/java/com/borborema/agenda/infrastructure/util/CriptoServiceTest.java`

**Objetivo**: Testar algoritmos de criptografia (Cifra de César e RSA)

**Testes Implementados**:

| # | Nome do Teste | Descrição |
|---|---------------|-----------|
| 1 | `testCesarEncrypt_ComNomeSimples()` | Encripta nome simples com César |
| 2 | `testCesarEncrypt_ComNomeComplexo()` | Encripta nome com acentos |
| 3 | `testCesarEncrypt_ComCaracteresEspeciais()` | Remove caracteres especiais |
| 4 | `testCesarDecrypt_DecriptaCesarEncrypt()` | Valida decrypt(encrypt(x)) = x |
| 5 | `testCesarEncryptDecrypt_ComDiferentesChaves()` | Testa chaves de 1 a 25 |
| 6 | `testRsaEncrypt_EncriptaTexto()` | Encripta texto com RSA |
| 7 | `testRsaDecrypt_DecriptaTextoEncriptado()` | Decrypt(Encrypt(x)) = x com RSA |
| 8 | `testRsaEncryptDecrypt_ComDiferentesTextos()` | Vários tipos de conteúdo |
| 9 | `testRsaEncrypt_ComChaveInvalida_DeveLancarException()` | Valida exceções |
| 10 | `testRsaDecrypt_ComChaveInvalida_DeveLancarException()` | Valida exceções |
| 11 | `testGenerateRSAKeyPar_GeraParDeChaves()` | Gera par de chaves RSA |
| 12 | `testGenerateRSAKeyPar_GeraChavesDiferentes()` | Chaves únicas |
| 13 | `testRsaEncryptDecrypt_ChavesDiferentesNaoFuncionam()` | Valida segurança RSA |

**Exemplo de Teste**:
```java
@Test
void testRsaEncryptDecrypt_ComDiferentesTextos() throws Exception {
    String[] textos = {"Texto curto", "Texto com números 123456", "Rua das Flores, 123"};

    for (String texto : textos) {
        String encriptado = criptoService.rsaEncrypt(texto, keyPair.getPublic());
        String decriptado = CriptoService.rsaDecrypt(encriptado, keyPair.getPrivate());
        assertEquals(texto, decriptado);
    }
}
```

---

#### 1.3 AgendaApplicationTests.java (1 teste)
**Localização**: `src/test/java/com/borborema/agenda/AgendaApplicationTests.java`

**Objetivo**: Testar inicialização do contexto Spring

| # | Nome do Teste | Descrição |
|---|---------------|-----------|
| 1 | `contextLoads()` | Valida que o contexto Spring inicia sem erros |

---

## 2. TESTES DE INTEGRAÇÃO (4 testes)

### Definição
**Testes de integração** verificam como diferentes componentes funcionam **juntos**. Testam a integração entre camadas (Service + Repository, Beans do Spring, etc).

### Arquivos de Teste

#### 2.1 CriptoServiceIntegrationTest.java (4 testes)
**Localização**: `src/test/java/com/borborema/agenda/integration/CriptoServiceIntegrationTest.java`

**Objetivo**: Testar integração do CriptoService com o contexto Spring

**O que é testado**:
- Injeção de dependências do Spring
- Integração entre componentes reais (sem mocks)
- Fluxos completos de criptografia

**Testes Implementados**:

| # | Nome do Teste | Descrição |
|---|---------------|-----------|
| 1 | `testCriptoServiceBean_DeveEstarInjetado()` | Valida injeção do Spring |
| 2 | `testIntegracaoRSA_EncriptarEDescriptografar()` | Fluxo completo RSA |
| 3 | `testIntegracaoCesar_EncriptarEDescriptografar()` | Fluxo completo César |
| 4 | `testIntegracaoCompleta_MultiplasCriptografias()` | Múltiplas encriptações |

**Exemplo de Teste**:
```java
@SpringBootTest
class CriptoServiceIntegrationTest {
    @Autowired
    private CriptoService criptoService; // Bean injetado pelo Spring

    @Test
    void testIntegracaoRSA_EncriptarEDescriptografar() throws Exception {
        String mensagemOriginal = "Teste de integração RSA";

        // Usa o serviço REAL (não mockado)
        String encriptado = criptoService.rsaEncrypt(mensagemOriginal, keyPair.getPublic());
        String decriptado = CriptoService.rsaDecrypt(encriptado, keyPair.getPrivate());

        assertEquals(mensagemOriginal, decriptado);
    }
}
```

**Diferença para Teste Unitário**:
- Usa @SpringBootTest (carrega contexto completo)
- Beans reais (não mocks)
- Testa integração entre componentes
- Mais lento que testes unitários

---

## 3. TESTES DE SISTEMA AUTOMATIZADOS (8 testes)

### Definição
**Testes de sistema** (também chamados de **End-to-End** ou **E2E**) testam a aplicação **completa** funcionando em conjunto. Simulam cenários reais de uso do sistema.

### Arquivos de Teste

#### 3.1 DatabaseSystemTest.java (8 testes)
**Localização**: `src/test/java/com/borborema/agenda/system/DatabaseSystemTest.java`

**Objetivo**: Simular fluxos completos end-to-end do sistema

**O que é testado**:
- Aplicação completa rodando
- Banco de dados H2 em memória
- Persistência de dados entre testes
- Fluxos de negócio completos
- Performance básica

**Testes Implementados** (executados em ordem):

| Ordem | Nome do Teste | Fluxo Simulado |
|-------|---------------|----------------|
| 1 | `test01_FluxoCompleto_VerificarUsuario()` | Cadastro de usuário |
| 2 | `test02_FluxoCompleto_CriarContatos()` | Criação de 2 contatos |
| 3 | `test03_FluxoCompleto_ListarContatos()` | Listagem de contatos |
| 4 | `test04_FluxoCompleto_BuscarContato()` | Busca por número |
| 5 | `test05_FluxoCompleto_AtualizarContato()` | Atualização de dados |
| 6 | `test06_FluxoCompleto_DeletarContato()` | Remoção de contato |
| 7 | `test07_FluxoCompleto_VerificarIntegridadeReferencial()` | Integridade FK |
| 8 | `test08_FluxoCompleto_TesteDePerformance()` | Criação de 50 contatos |

**Exemplo de Teste**:
```java
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseSystemTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContatoRepository contatoRepository;

    @Test
    @Order(2)
    void test02_FluxoCompleto_CriarContatos() {
        // Simula usuário REAL criando contatos no sistema
        Contato contato1 = Contato.builder()
                .nome("Maria Silva")
                .numero(111222333L)
                .endereco("Rua A, 123")
                .user(usuarioTeste)
                .build();

        contatoRepository.save(contato1); // Salva NO BANCO REAL

        var contatos = contatoRepository.findAll();
        assertTrue(contatos.size() >= 2);
        System.out.println("✓ Fluxo 2: Contatos criados no sistema");
    }
}
```

**Saída no Console**:
```
Fluxo 1: Usuário validado no sistema
Fluxo 2: Contatos criados no sistema
Fluxo 3: Contatos listados do banco de dados
Fluxo 4: Contato encontrado no sistema
Fluxo 5: Contato atualizado no sistema
Fluxo 6: Contato deletado do sistema
Fluxo 7: Integridade referencial validada
Fluxo 8: Performance validada - 64ms para 50 contatos
```

**Diferença para outros tipos**:
- Testa toda a aplicação rodando
- Banco de dados real (H2 em memória)
- Persistência de dados entre testes
- Fluxos de negócio completos
- Mais lento que integração e unitários

---

## Resumo Comparativo

| Aspecto | Testes Unitários | Testes de Integração | Testes de Sistema |
|---------|------------------|----------------------|-------------------|
| **Quantidade** | 23 | 4 | 8 |
| **Velocidade** | Rápido (< 3s) | Médio (< 1s) | Lento (~1s) |
| **Escopo** | Componente isolado | Múltiplos componentes | Sistema completo |
| **Mocks** | Sim (muitos) | Não | Não |
| **Banco de Dados** | Não | Sim (H2) | Sim (H2) |
| **Spring Context** | Não | Sim | Sim |
| **Objetivo** | Lógica de negócio | Integração de camadas | Fluxos end-to-end |

---

## Pirâmide de Testes

A aplicação segue a Pirâmide de Testes:

```
        /\
       /E2\     8 testes (Sistema)
      /----\
     /      \
    / Integr \  4 testes (Integração)
   /----------\
  /            \
 /  Unitários   \ 23 testes (Unitários)
/________________\
```

Proporção ideal: Muitos testes unitários, poucos testes de sistema

---

## Executando os Testes

### Executar Todos os Testes
```bash
./mvnw test
```

### Executar Apenas Testes Unitários
```bash
./mvnw test -Dtest="ContatoServiceTest,CriptoServiceTest,AgendaApplicationTests"
```

### Executar Apenas Testes de Integração
```bash
./mvnw test -Dtest="CriptoServiceIntegrationTest"
```

### Executar Apenas Testes de Sistema
```bash
./mvnw test -Dtest="DatabaseSystemTest"
```

### Executar com Relatório de Cobertura (JaCoCo)
```bash
./mvnw clean test jacoco:report
```

O relatório HTML estará em: `target/site/jacoco/index.html`

---

## Métricas de Qualidade

### Cobertura de Código
Execute com JaCoCo para ver a cobertura:
```bash
./mvnw test jacoco:report
```

### Tempo de Execução
```
- Testes Unitários: ~2-3 segundos
- Testes de Integração: ~0.6 segundos
- Testes de Sistema: ~0.7 segundos
- **Total**: ~19 segundos
```

### Assertions por Teste
- **Total de assertions**: 80+
- **Média**: ~2.3 assertions por teste

---

## Boas Práticas Aplicadas

### Nomenclatura Clara
- Formato: `test[Componente]_[Cenário]_[ResultadoEsperado]`
- Exemplo: `testCesarEncrypt_ComNomeSimples()`

### AAA Pattern
Todos os testes seguem **Arrange-Act-Assert**:
```java
@Test
void testExample() {
    // Arrange: Preparação
    ContatoDTO dto = new ContatoDTO(...);

    // Act: Ação
    contatoService.salvarContato(dto);

    // Assert: Verificação
    verify(repository, times(1)).save(...);
}
```

### Independência
- Cada teste roda de forma isolada
- Uso de `@BeforeEach` para setup
- Não há dependência entre testes (exceto Sistema com `@Order`)

### Mocks Apropriados
- Testes Unitários: Usam mocks (Mockito)
- Testes de Integração/Sistema: Sem mocks

### Cenários Positivos e Negativos
- Testa casos de sucesso
- Testa casos de erro/exceção
- Testa validações

---

## Evidências para o Professor

### 1. Print do Console
```
[INFO] Tests run: 35, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 2. Tipos de Teste Implementados
- **Testes Unitários** (23): ContatoServiceTest, CriptoServiceTest
- **Testes de Integração** (4): CriptoServiceIntegrationTest
- **Testes de Sistema** (8): DatabaseSystemTest

### 3. Arquivos de Teste
```
src/test/java/com/borborema/agenda/
├── business/
│   └── ContatoServiceTest.java (Unitário)
├── infrastructure/util/
│   └── CriptoServiceTest.java (Unitário)
├── integration/
│   └── CriptoServiceIntegrationTest.java (Integração)
├── system/
│   └── DatabaseSystemTest.java (Sistema)
└── AgendaApplicationTests.java (Unitário)
```

---

## Conclusão

A aplicação Agenda possui cobertura completa dos 3 tipos de testes:

1. **23 Testes Unitários** - Lógica de negócio isolada
2. **4 Testes de Integração** - Componentes integrados
3. **8 Testes de Sistema** - Fluxos end-to-end completos

**Total**: 35 testes, 100% passando

**Data do Relatório**: 2025-11-18
**Branch**: feature/criptografia
**Status**: BUILD SUCCESS
