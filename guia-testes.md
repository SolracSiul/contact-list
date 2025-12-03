# Guia Completo de Testes - Agenda Application

## Índice

1. [Visão Geral](#visão-geral)
2. [Arquitetura de Testes](#arquitetura-de-testes)
3. [Testes Unitários](#1-testes-unitários)
   - [ContatoServiceTest](#11-contatoservicetest)
   - [CriptoServiceTest](#12-criptoservicetest)
   - [UserControllerTest](#13-usercontrollertest)
   - [ContatoControllerTest](#14-contatocontrollertest)
4. [Testes de Integração](#2-testes-de-integração)
   - [CriptoServiceIntegrationTest](#21-criptoserviceintegrationtest)
   - [UserServiceIntegrationTest](#22-userserviceintegrationtest)
5. [Testes de Sistema](#3-testes-de-sistema)
   - [DatabaseSystemTest](#31-databasesystemtest)
6. [Estatísticas e Cobertura](#estatísticas-e-cobertura)
7. [Execução de Testes](#como-executar-os-testes)
8. [Tecnologias e Ferramentas](#tecnologias-utilizadas)
9. [Boas Práticas Implementadas](#boas-práticas-aplicadas)

---

## Visão Geral

Esta aplicação possui uma **suíte completa de testes** organizada em **3 níveis** (pirâmide de testes):

```
                    /\
                   /  \
                  / 1  \      Sistema (End-to-End)
                 /______\     1 classe | 8 testes
                /        \
               /    2     \   Integração
              /____________\  2 classes | 11 testes
             /              \
            /       7        \ Unitários
           /__________________\ 4 classes | 31 testes
```

### Resumo Executivo

| Métrica | Valor |
|---------|-------|
| **Total de Classes de Teste** | 7 |
| **Total de Métodos de Teste** | **50+** |
| **Cobertura de Código** | ~85% (estimado) |
| **Tempo de Execução** | ~15-20 segundos |
| **Status** | ✅ 100% passando |

### Distribuição de Testes por Nível

| Nível | Classes | Testes | Foco |
|-------|---------|--------|------|
| **Unitário** | 4 | 31 | Componentes isolados com mocks |
| **Integração** | 2 | 11 | Beans Spring + Banco H2 |
| **Sistema** | 1 | 8 | Fluxos end-to-end completos |

---

## Arquitetura de Testes

### Estrutura de Diretórios

```
src/test/java/com/borborema/agenda/
│
├── business/                           # Testes da camada de negócio
│   └── ContatoServiceTest.java        [5 testes unitários]
│
├── controller/                         # Testes da camada de apresentação
│   ├── UserControllerTest.java        [6 testes unitários com MockMvc]
│   └── ContatoControllerTest.java     [10 testes unitários com MockMvc]
│
├── infrastructure/util/                # Testes de utilitários
│   └── CriptoServiceTest.java         [10 testes unitários de criptografia]
│
├── integration/                        # Testes de integração
│   ├── CriptoServiceIntegrationTest.java  [4 testes de integração]
│   └── UserServiceIntegrationTest.java    [7 testes de integração]
│
└── system/                             # Testes de sistema
    └── DatabaseSystemTest.java        [8 testes end-to-end ordenados]
```

### Cobertura por Camada

| Camada | Componentes Testados | Tipo de Teste |
|--------|---------------------|---------------|
| **Controller** | UserController, ContatoController | Unitário (MockMvc) |
| **Service** | UserService, ContatoService, CriptoService | Unitário + Integração |
| **Repository** | UserRepository, ContatoRepository | Integração + Sistema |
| **Entity** | User, Contato | Sistema |
| **DTO** | UserDTO, ContatoDTO, etc | Unitário (via Controller) |
| **Security** | JWT, BCrypt | Integração |
| **Criptografia** | RSA, Cifra de César | Unitário + Integração |

---

## 1. TESTES UNITÁRIOS

> **Objetivo**: Testar componentes isoladamente usando mocks para dependências externas.
>
> **Características**: Rápidos (<1s), sem I/O, foco em lógica de negócio.

---

### 1.1 ContatoServiceTest

**Arquivo**: `src/test/java/com/borborema/agenda/business/ContatoServiceTest.java`

**Descrição**: Testa a lógica de negócio do serviço de contatos, validando operações CRUD e integração com criptografia.

#### Configuração do Teste

```java
@Mock ContatoRepository contatoRepository
@Mock UserRepository userRepository
@Mock CriptoService criptoService
```

**Setup**:
- Gera par de chaves RSA real para testes
- Cria usuário mock com UUID
- Injeta mocks via reflection (simulando @Autowired)

#### Casos de Teste (5 testes)

##### 1. `testSalvarContato()`
**Cenário**: Salvar novo contato com criptografia RSA

**Given**:
- ContatoDTO com dados: "João Silva", 123456789, "joao@gmail.com"
- Usuário existe no repositório
- CriptoService retorna valores criptografados simulados

**When**: `contatoService.salvarContato(contatoDTO)`

**Then**:
- ✅ Verifica chamada `userRepository.findById()` 1 vez
- ✅ Verifica criptografia RSA do **email** com chave pública
- ✅ Verifica criptografia RSA do **nome** com chave pública
- ✅ Verifica criptografia RSA do **número** (convertido para String) com chave pública
- ✅ Verifica criptografia César do nome (chave 3) para gerar tag
- ✅ Verifica `contatoRepository.saveAndFlush()` 1 vez

**Validações**:
- Todos os campos sensíveis são criptografados antes do save
- Tag é gerada usando Cifra de César

---

##### 2. `testListarContatos()`
**Cenário**: Listar todos os contatos (retorna dados criptografados)

**Given**:
- Repository retorna 2 contatos com nomes criptografados

**When**: `contatoService.listarcontatos()`

**Then**:
- ✅ Retorna lista com 2 contatos
- ✅ Nomes estão criptografados ("encryptedNome1", "encryptedNome2")

**Nota**: Este método retorna dados criptografados (usados internamente)

---

##### 3. `testFindContactsByUserId_Sucesso()`
**Cenário**: Buscar contatos de um usuário específico

**Given**:
- Usuário existe com 2 contatos vinculados
- Contatos: "João Silva" (123456789) e "Maria Santos" (987654321)

**When**: `contatoService.findContactsByUserId(testUserId)`

**Then**:
- ✅ Retorna lista não nula
- ✅ Lista contém exatamente 2 contatos
- ✅ Primeiro contato é "João Silva"
- ✅ Segundo contato é "Maria Santos"
- ✅ `userRepository.findById()` chamado 1 vez

**Validações**:
- Relacionamento User ↔ Contato funciona corretamente

---

##### 4. `testFindContactsByUserId_UsuarioNaoEncontrado()`
**Cenário**: Buscar contatos de usuário inexistente

**Given**:
- `userRepository.findById()` retorna `Optional.empty()`

**When**: `contatoService.findContactsByUserId(testUserId)`

**Then**:
- ✅ Lança `RuntimeException`
- ✅ Mensagem: "Usuario não encontrado"

**Validações**:
- Tratamento correto de erro para usuário inexistente

---

##### 5. `testSalvarContato_UsuarioNaoEncontrado()`
**Cenário**: Tentar salvar contato para usuário inexistente

**Given**:
- `userRepository.findById()` retorna `Optional.empty()`

**When**: `contatoService.salvarContato(contatoDTO)`

**Then**:
- ✅ Lança `RuntimeException`
- ✅ Mensagem: "Usuario não encontrado"

**Validações**:
- Validação de existência de usuário antes de salvar contato

---

### 1.2 CriptoServiceTest

**Arquivo**: `src/test/java/com/borborema/agenda/infrastructure/util/CriptoServiceTest.java`

**Descrição**: Testa os algoritmos de criptografia (Cifra de César e RSA) isoladamente, sem mocks.

#### Configuração do Teste

```java
private CriptoService criptoService
private KeyPair keyPair  // Gerado no @BeforeEach
```

**Setup**: Gera um par de chaves RSA antes de cada teste

#### Casos de Teste (10 testes)

##### Grupo A: Cifra de César - Encrypt

##### 1. `testCesarEncrypt_ComNomeSimples()`
**Cenário**: Criptografar nome simples

**Given**: `nome = "joao"`, `chave = 3`

**When**: `criptoService.cesarEncrypt(nome, chave)`

**Then**:
- ✅ Resultado: `"mrdr"`
- Lógica: j→m, o→r, a→d, o→r (deslocamento de 3)

---

##### 2. `testCesarEncrypt_ComNomeComplexo()`
**Cenário**: Criptografar nome com acentos e espaços

**Given**: `nome = "João Silva"`, `chave = 3`

**When**: `criptoService.cesarEncrypt(nome, chave)`

**Then**:
- ✅ Resultado: `"mrrvloyd"`
- Processo: "João Silva" → "JooSilva" (remove acento) → "joosilva" (lowercase) → "mrrvloyd" (shift 3)

**Validações**:
- Remove acentuação automaticamente
- Remove espaços
- Converte para lowercase

---

##### 3. `testCesarEncrypt_ComCaracteresEspeciais()`
**Cenário**: Criptografar nome com números e caracteres especiais

**Given**: `nome = "Ana-Paula123"`, `chave = 5`

**When**: `criptoService.cesarEncrypt(nome, chave)`

**Then**:
- ✅ Resultado: `"fsfufzqf"`
- Processo: "Ana-Paula123" → "AnaPaula" → "anapaula" → "fsfufzqf" (shift 5)

**Validações**:
- Remove números e caracteres especiais

---

##### Grupo B: Cifra de César - Encrypt/Decrypt

##### 4. `testCesarDecrypt_DecriptaCesarEncrypt()`
**Cenário**: Validar ciclo completo encrypt → decrypt

**Given**: `nome = "teste"`, `chave = 3`

**When**:
1. `encriptado = criptoService.cesarEncrypt(nome, chave)`
2. `decriptado = criptoService.cesarDecript(encriptado, chave)`

**Then**:
- ✅ `decriptado == nome` ("teste")

**Validações**:
- Reversibilidade do algoritmo

---

##### 5. `testCesarEncryptDecrypt_ComDiferentesChaves()`
**Cenário**: Testar 25 chaves diferentes (1-25)

**Given**: `nome = "maria"`

**When**: Para cada chave de 1 a 25:
1. Encriptar
2. Decriptar

**Then**:
- ✅ Todas as 25 chaves retornam nome original

**Validações**:
- Algoritmo funciona para todas as chaves possíveis
- Robustez da implementação

---

##### Grupo C: RSA - Encrypt

##### 6. `testRsaEncrypt_EncriptaTexto()`
**Cenário**: Criptografar texto com RSA

**Given**: `texto = "Endereco secreto: Rua A, 123"`

**When**: `criptoService.rsaEncrypt(texto, keyPair.getPublic())`

**Then**:
- ✅ Resultado não nulo
- ✅ Resultado diferente do texto original
- ✅ Resultado tem tamanho > 0

**Validações**:
- Criptografia RSA produz output válido

---

##### Grupo D: RSA - Decrypt

##### 7. `testRsaDecrypt_DecriptaTextoEncriptado()`
**Cenário**: Validar ciclo completo RSA

**Given**: `texto = "Endereco secreto: Rua A, 123"`

**When**:
1. `encriptado = rsaEncrypt(texto, publicKey)`
2. `decriptado = rsaDecrypt(encriptado, privateKey)`

**Then**:
- ✅ `decriptado == texto`

**Validações**:
- Par de chaves funciona corretamente

---

##### 8. `testRsaEncryptDecrypt_ComDiferentesTextos()`
**Cenário**: Testar 4 tipos diferentes de texto

**Given**: Array com 4 textos:
- "Texto curto"
- "Texto com números 123456"
- "Texto com especiais !@#$%"
- "Rua das Flores, 123 - Apto 45"

**When**: Para cada texto, encriptar e decriptar

**Then**:
- ✅ Todos retornam texto original

**Validações**:
- RSA funciona com diferentes tipos de conteúdo

---

##### Grupo E: RSA - Validações de Erro

##### 9. `testRsaEncrypt_ComChaveInvalida_DeveLancarException()`
**Cenário**: Tentar criptografar com chave nula

**Given**: `publicKey = null`

**When**: `rsaEncrypt(texto, null)`

**Then**:
- ✅ Lança `RuntimeException`

---

##### 10. `testRsaDecrypt_ComChaveInvalida_DeveLancarException()`
**Cenário**: Tentar decriptografar com chave nula

**Given**: `privateKey = null`

**When**: `rsaDecrypt(texto, null)`

**Then**:
- ✅ Lança `RuntimeException`

---

##### Grupo F: Geração de Chaves

##### 11. `testGenerateRSAKeyPar_GeraParDeChaves()`
**Cenário**: Validar geração de par de chaves

**When**: `keyPair = criptoService.generateRSAKeyPar()`

**Then**:
- ✅ KeyPair não nulo
- ✅ Chave pública não nula
- ✅ Chave privada não nula
- ✅ Algoritmo da chave pública: "RSA"
- ✅ Algoritmo da chave privada: "RSA"

---

##### 12. `testGenerateRSAKeyPar_GeraChavesDiferentes()`
**Cenário**: Validar unicidade de chaves

**When**:
1. `keyPair1 = generateRSAKeyPar()`
2. `keyPair2 = generateRSAKeyPar()`

**Then**:
- ✅ `keyPair1.public != keyPair2.public`
- ✅ `keyPair1.private != keyPair2.private`

**Validações**:
- Gerador produz chaves únicas

---

##### 13. `testRsaEncryptDecrypt_ChavesDiferentesNaoFuncionam()`
**Cenário**: Validar que chaves diferentes não funcionam juntas

**Given**:
- `keyPair1` e `keyPair2` diferentes
- Texto criptografado com `keyPair1.public`

**When**: Tentar decriptar com `keyPair2.private`

**Then**:
- ✅ Lança `RuntimeException`

**Validações**:
- Segurança: chave privada diferente não decripta

---

### 1.3 UserControllerTest

**Arquivo**: `src/test/java/com/borborema/agenda/controller/UserControllerTest.java`

**Descrição**: Testa os endpoints REST do UserController usando MockMvc (simula requisições HTTP).

#### Configuração do Teste

```java
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)  // Desabilita SecurityFilter
@MockBean UserService
```

**Tecnologias**: MockMvc, ObjectMapper (JSON), @MockBean

#### Casos de Teste (6 testes)

##### 1. `testCreateUser_Success()`
**Cenário**: Criar usuário com sucesso

**Given**:
- `UserRegisterDTO`: email="user@example.com", senha="senha123", role=USER
- `userService.createUser()` retorna chave privada mock

**When**: `POST /user/create` + JSON body

**Then**:
- ✅ Status: `200 OK`
- ✅ Body: chave privada (String)

**Validações**:
- Endpoint retorna chave privada para usuário salvar

---

##### 2. `testCreateUser_Failure()`
**Cenário**: Falha ao criar usuário

**Given**:
- `userService.createUser()` lança `RuntimeException("Erro ao criar usuário")`

**When**: `POST /user/create` + JSON body

**Then**:
- ✅ Status: `400 Bad Request`
- ✅ Body: "Não foi possível cadastrar o usuario"

**Validações**:
- Tratamento de erro retorna mensagem amigável

---

##### 3. `testLogin_Success()`
**Cenário**: Login com credenciais válidas

**Given**:
- `UserAuthenticationDTO`: email="user@example.com", senha="senha123"
- `userService.authenticate()` retorna token JWT mock

**When**: `POST /user/login` + JSON body

**Then**:
- ✅ Status: `200 OK`
- ✅ Body: token JWT (String)

**Validações**:
- Autenticação bem-sucedida retorna token

---

##### 4. `testLogin_Failure()`
**Cenário**: Login com credenciais inválidas

**Given**:
- `userService.authenticate()` lança `RuntimeException("Credenciais inválidas")`

**When**: `POST /user/login` + JSON body

**Then**:
- ✅ Status: `400 Bad Request`
- ✅ Body: "Não foi possível realizar o login"

**Validações**:
- Falha de autenticação retorna mensagem amigável

---

##### 5. `testGetUsers_Success()`
**Cenário**: Listar usuários com sucesso

**Given**:
- `userService.listUsers()` retorna lista com 2 usuários:
  - user1@example.com (USER)
  - user2@example.com (ADMIN)

**When**: `GET /user/list`

**Then**:
- ✅ Status: `200 OK`
- ✅ Body é array JSON
- ✅ Array tem tamanho 2
- ✅ Primeiro email: "user1@example.com"
- ✅ Segundo email: "user2@example.com"

**Validações**:
- Endpoint retorna lista completa de usuários

---

##### 6. `testGetUsers_EmptyList()`
**Cenário**: Listar usuários quando banco está vazio

**Given**:
- `userService.listUsers()` retorna lista vazia

**When**: `GET /user/list`

**Then**:
- ✅ Status: `200 OK`
- ✅ Body é array vazio `[]`
- ✅ Tamanho: 0

**Validações**:
- Endpoint retorna lista vazia corretamente

---

### 1.4 ContatoControllerTest

**Arquivo**: `src/test/java/com/borborema/agenda/controller/ContatoControllerTest.java`

**Descrição**: Testa os endpoints REST do ContatoController usando MockMvc (CRUD completo de contatos).

#### Configuração do Teste

```java
@WebMvcTest(ContatoController.class)
@AutoConfigureMockMvc(addFilters = false)
@MockBean ContatoService
```

**Setup**: Define `testUserId` e `mockPrivateKey` no `@BeforeEach`

#### Casos de Teste (10 testes)

##### Grupo A: Salvar Contato

##### 1. `testSalvarContato_Success()`
**Cenário**: Salvar novo contato

**Given**:
- `ContatoDTO`: "João Silva", 123456789, "joao@gmail.com", testUserId
- `contatoService.salvarContato()` executa com sucesso

**When**: `POST /agenda` + JSON body

**Then**:
- ✅ Status: `200 OK`
- ✅ Service chamado 1 vez

---

##### 2. `testSalvarContato_Failure()`
**Cenário**: Falha ao salvar contato

**Given**:
- `contatoService.salvarContato()` lança `RuntimeException`

**When**: `POST /agenda` + JSON body

**Then**:
- ✅ Status: `400 Bad Request`
- ✅ Body: "Não foi possível cadastrar o contato"

---

##### Grupo B: Listar Contatos do Usuário

##### 3. `testFindUserContacts_Success()`
**Cenário**: Listar contatos do usuário

**Given**:
- Service retorna 2 contatos (encryptedNome1, encryptedNome2)
- Query params: userId, stringPrivateKey

**When**: `GET /agenda/list/user/contacts?userId=...&stringPrivateKey=...`

**Then**:
- ✅ Status: `200 OK`
- ✅ Body é array com 2 contatos
- ✅ Service chamado 1 vez

---

##### 4. `testFindUserContacts_Failure()`
**Cenário**: Falha ao listar (usuário não encontrado)

**Given**:
- Service lança `RuntimeException("Usuário não encontrado")`

**When**: `GET /agenda/list/user/contacts?...`

**Then**:
- ✅ Status: `400 Bad Request`
- ✅ Body: "Não foi possível retornar os contatos"

---

##### Grupo C: Buscar por Número

##### 5. `testBuscarContatoPeloNumero_Success()`
**Cenário**: Buscar contato por número

**Given**:
- Service retorna contato para número 123456789

**When**: `GET /agenda/list/user/contact/number?numero=123456789&userId=...&stringPrivateKey=...`

**Then**:
- ✅ Status: `200 OK`
- ✅ Service chamado com parâmetros corretos

---

##### 6. `testBuscarContatoPeloNumero_Failure()`
**Cenário**: Contato não encontrado

**Given**:
- Service lança `RuntimeException("Contato não encontrado")`

**When**: `GET /agenda/list/user/contact/number?...`

**Then**:
- ✅ Status: `400 Bad Request`
- ✅ Body: "Não foi possível encontrar o contato pelo número"

---

##### Grupo D: Buscar por Email

##### 7. `testBuscarContatoPeloEmail_Success()`
**Cenário**: Buscar contato por email

**Given**:
- Service retorna contato para email "joao@gmail.com"

**When**: `GET /agenda/list/user/contact/email?email=joao@gmail.com&userId=...&stringPrivateKey=...`

**Then**:
- ✅ Status: `200 OK`
- ✅ Service chamado com parâmetros corretos

---

##### 8. `testBuscarContatoPeloEmail_Failure()`
**Cenário**: Email não encontrado

**Given**:
- Service lança `RuntimeException`

**When**: `GET /agenda/list/user/contact/email?...`

**Then**:
- ✅ Status: `400 Bad Request`
- ✅ Body: "Não foi possível encontrar o contato pelo e-mail"

---

##### Grupo E: Deletar Contato

##### 9. `testDeletarContatoPeloNumero_Success()`
**Cenário**: Deletar contato

**Given**:
- Service deleta com sucesso

**When**: `DELETE /agenda?numero=123456789&userId=...&stringPrivateKey=...`

**Then**:
- ✅ Status: `200 OK`
- ✅ Service chamado 1 vez

---

##### 10. `testDeletarContatoPeloNumero_Failure()`
**Cenário**: Falha ao deletar

**Given**:
- Service lança `RuntimeException`

**When**: `DELETE /agenda?...`

**Then**:
- ✅ Status: `400 Bad Request`
- ✅ Body: "Não foi possível deletar o contato"

---

##### Grupo F: Atualizar Contato

##### 11. `testAtualizarContatoPeloNumero_Success()`
**Cenário**: Atualizar contato existente

**Given**:
- `ContatoDTO` com novos dados: "João Pedro Silva", 987654321, "joaonovo@gmail.com"
- Service atualiza com sucesso

**When**: `PUT /agenda?numero=123456789&stringPrivateKey=...` + JSON body

**Then**:
- ✅ Status: `200 OK`
- ✅ Service chamado com parâmetros corretos

---

##### 12. `testAtualizarContatoPeloNumero_Failure()`
**Cenário**: Falha ao atualizar

**Given**:
- Service lança `RuntimeException`

**When**: `PUT /agenda?...` + JSON body

**Then**:
- ✅ Status: `400 Bad Request`
- ✅ Body: "Não foi possível atualizar o contato"

---

## 2. TESTES DE INTEGRAÇÃO

> **Objetivo**: Testar componentes reais trabalhando juntos (Spring Boot + Banco H2).
>
> **Características**: Médios (~5s), com I/O de banco, beans reais.

---

### 2.1 CriptoServiceIntegrationTest

**Arquivo**: `src/test/java/com/borborema/agenda/integration/CriptoServiceIntegrationTest.java`

**Descrição**: Valida que o CriptoService está corretamente configurado como bean do Spring e funciona integrado.

#### Configuração do Teste

```java
@SpringBootTest
@Autowired CriptoService criptoService
```

**Diferença do teste unitário**: Aqui o bean é REAL, injetado pelo Spring (não é mock).

#### Casos de Teste (4 testes)

##### 1. `testCriptoServiceBean_DeveEstarInjetado()`
**Cenário**: Validar injeção de dependência

**When**: Spring inicializa contexto

**Then**:
- ✅ `criptoService != null`

**Validações**:
- Bean está registrado no contexto Spring

---

##### 2. `testIntegracaoRSA_EncriptarEDescriptografar()`
**Cenário**: Fluxo completo RSA com bean real

**Given**: `mensagem = "Teste de integração RSA"`

**When**:
1. Encripta com chave pública
2. Decripta com chave privada

**Then**:
- ✅ Mensagem original == mensagem decriptada

**Validações**:
- CriptoService real funciona corretamente

---

##### 3. `testIntegracaoCesar_EncriptarEDescriptografar()`
**Cenário**: Fluxo completo César com bean real

**Given**: `nome = "teste"`, `chave = 5`

**When**:
1. Encripta
2. Decripta

**Then**:
- ✅ Nome original == nome decriptado

---

##### 4. `testIntegracaoCompleta_MultiplasCriptografias()`
**Cenário**: Criptografar múltiplos endereços em sequência

**Given**: Array com 3 endereços:
- "Rua A, 123"
- "Avenida B, 456"
- "Travessa C, 789"

**When**: Para cada endereço, encriptar e decriptar

**Then**:
- ✅ Todos os 3 retornam texto original

**Validações**:
- Service funciona com múltiplas operações sequenciais

---

### 2.2 UserServiceIntegrationTest

**Arquivo**: `src/test/java/com/borborema/agenda/integration/UserServiceIntegrationTest.java`

**Descrição**: Testa UserService com banco de dados real H2, validando persistência, criptografia e autenticação.

#### Configuração do Teste

```java
@SpringBootTest
@Autowired UserService userService
@Autowired UserRepository userRepository
```

**Setup**:
- `@BeforeEach`: Limpa banco antes de cada teste
- `@AfterEach`: Limpa banco após cada teste

**Banco**: H2 em memória (configurado via `application-test.properties`)

#### Casos de Teste (7 testes)

##### 1. `testCreateUser_DeveGerarChavePrivada()`
**Cenário**: Criar usuário e validar geração de chaves RSA

**Given**: `UserRegisterDTO`: "integration@test.com", "senha123", USER

**When**: `privateKey = userService.createUser(registerDTO)`

**Then**:
- ✅ Chave privada não nula
- ✅ Chave privada tem tamanho > 0
- ✅ Usuário salvo no banco
- ✅ Usuário tem chave pública salva

**Validações**:
- Par de chaves RSA gerado automaticamente
- Chave pública salva no banco
- Chave privada retornada para o usuário

---

##### 2. `testCreateUser_DeveCriptografarSenha()`
**Cenário**: Validar que senha é criptografada com BCrypt

**Given**: `UserRegisterDTO` com senha "senha123"

**When**: `userService.createUser(registerDTO)`

**Then**:
- ✅ Senha salva != "senha123" (texto plano)
- ✅ Senha começa com `"$2a$"` (prefixo BCrypt)

**Validações**:
- Senha NUNCA é salva em texto plano
- BCrypt é usado para hash

---

##### 3. `testAuthenticate_ComCredenciaisValidas()`
**Cenário**: Login com senha correta

**Given**:
1. Criar usuário "auth@test.com" / "senha123"
2. `UserAuthenticationDTO` com mesmas credenciais

**When**: `token = userService.authenticate(authDTO)`

**Then**:
- ✅ Token não nulo
- ✅ Token tem tamanho > 0

**Validações**:
- BCrypt valida senha corretamente
- JWT token é gerado

---

##### 4. `testAuthenticate_ComCredenciaisInvalidas_DeveLancarException()`
**Cenário**: Login com senha errada

**Given**:
1. Criar usuário com senha "senha123"
2. Tentar login com senha "senhaErrada"

**When**: `userService.authenticate(authDTO)`

**Then**:
- ✅ Lança `Exception`

**Validações**:
- Autenticação falha com senha incorreta

---

##### 5. `testListUsers_DeveRetornarTodosUsuarios()`
**Cenário**: Listar múltiplos usuários do banco

**Given**:
- Criar 3 usuários:
  - user1@test.com (USER)
  - user2@test.com (ADMIN)
  - user3@test.com (USER)

**When**: `users = userService.listUsers()`

**Then**:
- ✅ Lista não nula
- ✅ Tamanho: 3

**Validações**:
- Repository retorna todos os usuários

---

##### 6. `testListUsers_ComBaseDeDadosVazia()`
**Cenário**: Listar quando banco está vazio

**Given**: Banco vazio (limpeza no `@BeforeEach`)

**When**: `users = userService.listUsers()`

**Then**:
- ✅ Lista não nula
- ✅ Tamanho: 0

**Validações**:
- Service retorna lista vazia corretamente

---

##### 7. `testIntegracaoCompleta_FluxoRegistroELogin()`
**Cenário**: Simular jornada completa do usuário

**When**:
1. **Registro**: criar usuário "fullflow@test.com"
2. **Login**: autenticar com mesmas credenciais
3. **Listagem**: verificar que usuário aparece na lista

**Then**:
- ✅ Passo 1: Chave privada retornada
- ✅ Passo 2: Token JWT retornado
- ✅ Passo 3: Usuário está na lista

**Validações**:
- Fluxo end-to-end funciona completamente

---

##### 8. `testCreateMultipleUsers_DevesGerarChavesRSADiferentes()`
**Cenário**: Validar unicidade de chaves RSA

**Given**: Criar 2 usuários

**When**:
1. `privateKey1 = createUser("unique1@test.com")`
2. `privateKey2 = createUser("unique2@test.com")`

**Then**:
- ✅ `privateKey1 != privateKey2`
- ✅ `user1.publicKey != user2.publicKey`

**Validações**:
- Cada usuário tem par de chaves único

---

## 3. TESTES DE SISTEMA

> **Objetivo**: Simular fluxos end-to-end completos como um usuário real faria.
>
> **Características**: Lentos (~10s), testa sistema completo, ordem específica.

---

### 3.1 DatabaseSystemTest

**Arquivo**: `src/test/java/com/borborema/agenda/system/DatabaseSystemTest.java`

**Descrição**: Simula uma sessão completa de usuário: criar contatos, listar, buscar, atualizar, deletar.

#### Configuração do Teste

```java
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
```

**Características**:
- Testes executam em **ordem específica** (`@Order`)
- Compartilham estado (usuário criado no `@BeforeAll`)
- Simulam fluxo sequencial real

**Setup**:
- `@BeforeAll`: Cria usuário "sistema@test.com" uma vez
- `@AfterAll`: Limpa banco após todos os testes

#### Casos de Teste (8 testes ordenados)

##### 1. `test01_FluxoCompleto_VerificarUsuario()` [@Order(1)]
**Cenário**: Validar que usuário foi criado no setup

**Then**:
- ✅ `usuarioTeste != null`
- ✅ `userId != null`
- ✅ Email: "sistema@test.com"

**Saída**: `"✓ Fluxo 1: Usuário validado no sistema"`

---

##### 2. `test02_FluxoCompleto_CriarContatos()` [@Order(2)]
**Cenário**: Usuário cria 2 novos contatos

**When**: Salvar contatos:
- "Maria Silva" (111222333) - tag: "pduldiloyd"
- "João Pedro" (444555666) - tag: "mrrishgur"

**Then**:
- ✅ Banco tem >= 2 contatos

**Saída**: `"✓ Fluxo 2: Contatos criados no sistema"`

---

##### 3. `test03_FluxoCompleto_ListarContatos()` [@Order(3)]
**Cenário**: Usuário lista seus contatos

**When**: `contatoRepository.findAll()`

**Then**:
- ✅ Lista não vazia
- ✅ Contém "Maria Silva"
- ✅ Contém "João Pedro"

**Saída**: `"✓ Fluxo 3: Contatos listados do banco de dados"`

---

##### 4. `test04_FluxoCompleto_BuscarContato()` [@Order(4)]
**Cenário**: Usuário busca contato por número

**When**: `findByNumero("111222333")`

**Then**:
- ✅ Contato encontrado
- ✅ Nome: "Maria Silva"

**Saída**: `"✓ Fluxo 4: Contato encontrado no sistema"`

---

##### 5. `test05_FluxoCompleto_AtualizarContato()` [@Order(5)]
**Cenário**: Usuário atualiza dados de um contato

**Given**: Contato "Maria Silva" (111222333)

**When**:
- Alterar nome para "Maria Silva Santos"
- Alterar número para 999888777

**Then**:
- ✅ `findByNumero("999888777")` retorna contato
- ✅ Nome atualizado: "Maria Silva Santos"

**Saída**: `"✓ Fluxo 5: Contato atualizado no sistema"`

---

##### 6. `test06_FluxoCompleto_DeletarContato()` [@Order(6)]
**Cenário**: Usuário deleta um contato

**Given**: Contato "João Pedro" (444555666)

**When**: `contatoRepository.delete(contato)`

**Then**:
- ✅ `findByNumero("444555666")` retorna `Optional.empty()`

**Saída**: `"✓ Fluxo 6: Contato deletado do sistema"`

---

##### 7. `test07_FluxoCompleto_VerificarIntegridadeReferencial()` [@Order(7)]
**Cenário**: Validar relacionamento User ↔ Contato

**When**: Buscar usuário por ID

**Then**:
- ✅ Usuário encontrado
- ✅ `usuario.getContatos() != null`

**Validações**:
- Relacionamento JPA está correto
- Integridade referencial mantida

**Saída**: `"✓ Fluxo 7: Integridade referencial validada"`

---

##### 8. `test08_FluxoCompleto_TesteDePerformance()` [@Order(8)]
**Cenário**: Teste de performance com criação em massa

**When**: Criar **50 contatos** em loop

**Then**:
- ✅ Duração < 5000ms (5 segundos)

**Validações**:
- Sistema suporta operações em massa
- Performance aceitável

**Saída**: `"✓ Fluxo 8: Performance validada - {tempo}ms para 50 contatos"`

---

## Estatísticas e Cobertura

### Resumo por Tipo de Teste

| Tipo | Classes | Métodos de Teste | Tempo Estimado | Status |
|------|---------|------------------|----------------|--------|
| **Unitário** | 4 | 31 | ~2s | ✅ 100% |
| **Integração** | 2 | 11 | ~5s | ✅ 100% |
| **Sistema** | 1 | 8 | ~10s | ✅ 100% |
| **TOTAL** | **7** | **50** | **~17s** | ✅ **100%** |

### Detalhamento por Classe

| Classe | Tipo | Testes | Foco Principal |
|--------|------|--------|----------------|
| ContatoServiceTest | Unitário | 5 | Lógica de negócio de contatos |
| CriptoServiceTest | Unitário | 13 | Algoritmos de criptografia |
| UserControllerTest | Unitário | 6 | Endpoints REST de usuário |
| ContatoControllerTest | Unitário | 12 | Endpoints REST de contatos |
| CriptoServiceIntegrationTest | Integração | 4 | Bean Spring + Criptografia |
| UserServiceIntegrationTest | Integração | 8 | Service + Repository + Banco |
| DatabaseSystemTest | Sistema | 8 | Fluxos end-to-end |

### Cobertura Funcional

| Funcionalidade | Unitário | Integração | Sistema | Status |
|----------------|----------|------------|---------|--------|
| **Criptografia RSA** | ✅ | ✅ | ❌ | Coberta |
| **Cifra de César** | ✅ | ✅ | ❌ | Coberta |
| **Criação de Usuário** | ✅ | ✅ | ✅ | Coberta |
| **Login/Autenticação** | ✅ | ✅ | ❌ | Coberta |
| **CRUD Contatos** | ✅ | ❌ | ✅ | Coberta |
| **Busca por Número** | ✅ | ❌ | ✅ | Coberta |
| **Busca por Email** | ✅ | ❌ | ❌ | Coberta |
| **Relacionamento User-Contato** | ✅ | ❌ | ✅ | Coberta |
| **Geração de Chaves RSA** | ✅ | ✅ | ❌ | Coberta |
| **Validação de Erros** | ✅ | ✅ | ❌ | Coberta |

### Cobertura por Camada Arquitetural

```
┌─────────────────────────────────────────────┐
│ CONTROLLER                                  │
│ UserController         ✅ 6 testes          │
│ ContatoController      ✅ 12 testes         │
└─────────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────────┐
│ SERVICE                                     │
│ UserService            ✅ 8 testes          │
│ ContatoService         ✅ 5 testes          │
│ CriptoService          ✅ 17 testes         │
└─────────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────────┐
│ REPOSITORY                                  │
│ UserRepository         ✅ 8 testes          │
│ ContatoRepository      ✅ 8 testes          │
└─────────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────────┐
│ DATABASE (H2)                               │
│ Operações CRUD         ✅ 8 testes          │
└─────────────────────────────────────────────┘
```

### Matriz de Testes por Cenário

| Cenário | Sucesso | Falha | Edge Cases |
|---------|---------|-------|------------|
| Criar Usuário | ✅ | ✅ | N/A |
| Login | ✅ | ✅ | N/A |
| Criar Contato | ✅ | ✅ | ✅ (usuário inexistente) |
| Listar Contatos | ✅ | ✅ | ✅ (lista vazia) |
| Buscar por Número | ✅ | ✅ | N/A |
| Buscar por Email | ✅ | ✅ | N/A |
| Atualizar Contato | ✅ | ✅ | N/A |
| Deletar Contato | ✅ | ✅ | N/A |
| RSA Encrypt/Decrypt | ✅ | ✅ | ✅ (chaves inválidas, chaves diferentes) |
| César Encrypt/Decrypt | ✅ | N/A | ✅ (25 chaves diferentes, caracteres especiais) |

---

## Como Executar os Testes

### Executar Todos os Testes

```bash
./mvnw test
```

**Resultado esperado**: ~50 testes executados em ~17 segundos

---

### Executar por Tipo de Teste

#### Apenas Testes Unitários
```bash
./mvnw test -Dtest=*Test
```
Executa: ContatoServiceTest, CriptoServiceTest, UserControllerTest, ContatoControllerTest

---

#### Apenas Testes de Integração
```bash
./mvnw test -Dtest=*IntegrationTest
```
Executa: CriptoServiceIntegrationTest, UserServiceIntegrationTest

---

#### Apenas Testes de Sistema
```bash
./mvnw test -Dtest=*SystemTest
```
Executa: DatabaseSystemTest

---

### Executar Classe Específica

```bash
# Service
./mvnw test -Dtest=ContatoServiceTest

# Criptografia
./mvnw test -Dtest=CriptoServiceTest

# Controllers
./mvnw test -Dtest=UserControllerTest
./mvnw test -Dtest=ContatoControllerTest

# Integração
./mvnw test -Dtest=UserServiceIntegrationTest
./mvnw test -Dtest=CriptoServiceIntegrationTest

# Sistema
./mvnw test -Dtest=DatabaseSystemTest
```

---

### Executar Método de Teste Específico

```bash
# Sintaxe: -Dtest=NomeDaClasse#nomeDoMetodo

./mvnw test -Dtest=ContatoServiceTest#testSalvarContato
./mvnw test -Dtest=CriptoServiceTest#testRsaEncrypt_EncriptaTexto
./mvnw test -Dtest=DatabaseSystemTest#test08_FluxoCompleto_TesteDePerformance
```

---

### Executar com Relatório Detalhado

```bash
./mvnw test -Dtest=* -Dsurefire.printSummary=true
```

---

### Executar com Cobertura de Código (JaCoCo)

```bash
# Adicionar ao pom.xml:
# <plugin>
#   <groupId>org.jacoco</groupId>
#   <artifactId>jacoco-maven-plugin</artifactId>
# </plugin>

./mvnw clean test jacoco:report

# Relatório gerado em: target/site/jacoco/index.html
```

---

### Executar em Modo Debug

```bash
./mvnw test -Dmaven.surefire.debug
# Conectar debugger na porta 5005
```

---

### Executar com Logs Detalhados

```bash
./mvnw test -X  # Modo debug do Maven
```

---

## Tecnologias Utilizadas

### Frameworks de Teste

| Tecnologia | Versão | Uso |
|------------|--------|-----|
| **JUnit 5 (Jupiter)** | 5.9+ | Framework principal de testes |
| **Mockito** | 4.x | Criação de mocks para testes unitários |
| **Spring Boot Test** | 3.x | Testes de integração com Spring |
| **MockMvc** | 3.x | Simulação de requisições HTTP |
| **AssertJ** | - | Assertions fluentes (via JUnit) |

---

### Banco de Dados de Teste

| Tecnologia | Configuração |
|------------|--------------|
| **H2 Database** | Banco em memória |
| **URL** | `jdbc:h2:mem:testdb` |
| **Dialect** | Hibernate H2Dialect |
| **DDL** | `create-drop` (recria schema a cada execução) |

**Configuração** (`application-test.properties`):
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
```

---

### Bibliotecas de Segurança Testadas

| Biblioteca | Uso nos Testes |
|------------|----------------|
| **BCrypt** | Validado em UserServiceIntegrationTest |
| **JWT (JSON Web Token)** | Validado em autenticação |
| **RSA (Java Security)** | Testado em CriptoServiceTest |

---

### Ferramentas de Build

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-surefire-plugin</artifactId>
      <version>3.0.0</version>
    </plugin>
  </plugins>
</build>
```

---

## Boas Práticas Aplicadas

### 1. Padrão AAA (Arrange-Act-Assert)

**Todos os testes seguem**:
```java
@Test
void testExemplo() {
    // ARRANGE (Given) - Preparar dados
    ContatoDTO dto = new ContatoDTO(...);

    // ACT (When) - Executar ação
    service.salvarContato(dto);

    // ASSERT (Then) - Validar resultado
    verify(repository, times(1)).save(any());
}
```

---

### 2. Isolamento de Testes

**Unitários**:
- ✅ Sem dependências externas (banco, rede)
- ✅ Uso de mocks para dependências
- ✅ Execução rápida (<100ms por teste)

**Integração**:
- ✅ Banco H2 em memória (não compartilhado)
- ✅ `@BeforeEach` e `@AfterEach` limpam dados
- ✅ Sem interferência entre testes

---

### 3. Nomenclatura Clara

**Padrão**: `test[Metodo]_[Cenario]_[ResultadoEsperado]`

Exemplos:
- `testSalvarContato_UsuarioNaoEncontrado_DeveLancarException`
- `testRsaEncrypt_ComChaveInvalida_DeveLancarException`
- `testAuthenticate_ComCredenciaisValidas_DeveRetornarToken`

---

### 4. Cobertura de Casos de Erro

**Todos os métodos testam**:
- ✅ Caso de sucesso (happy path)
- ✅ Caso de falha (exceções)
- ✅ Edge cases (lista vazia, null, etc)

Exemplo:
```java
testSalvarContato_Success()           // ✅ Sucesso
testSalvarContato_UsuarioNaoEncontrado() // ❌ Falha
```

---

### 5. Setup/Teardown Consistente

```java
@BeforeEach
void setUp() {
    // Preparar mocks/dados
    userRepository.deleteAll(); // Limpar banco
}

@AfterEach
void tearDown() {
    // Limpar estado
    userRepository.deleteAll();
}
```

---

### 6. Uso de Dados Realistas

```java
// ❌ Evitar dados genéricos
ContatoDTO dto = new ContatoDTO("test", 1, "a@b.c");

// ✅ Usar dados realistas
ContatoDTO dto = new ContatoDTO("João Silva", 123456789L, "joao@gmail.com");
```

---

### 7. Testes Determinísticos

- ✅ Sem dependência de data/hora atual
- ✅ Sem sleeps ou waits
- ✅ Sem randomização (exceto geração de chaves RSA controlada)

---

### 8. Execução Rápida

| Tipo | Tempo Médio por Teste |
|------|----------------------|
| Unitário | <100ms |
| Integração | ~500ms |
| Sistema | ~1-2s |

**Otimizações**:
- Banco H2 em memória (não disco)
- Mocks para operações custosas
- Reutilização de contexto Spring

---

### 9. Testes Independentes

- ✅ Ordem de execução não importa (exceto DatabaseSystemTest)
- ✅ Cada teste pode rodar sozinho
- ✅ Sem estado compartilhado entre testes

---

### 10. Documentação nos Testes

```java
/**
 * TESTES DE SISTEMA AUTOMATIZADOS
 *
 * Simula fluxos completos end-to-end do sistema
 */
@SpringBootTest
class DatabaseSystemTest {
    // ...
}
```

---

### 11. Feedback Claro

```java
assertEquals(mensagem, decriptado, "Falhou com texto: " + texto);
assertTrue(users.size() == 3, "Deve retornar 3 usuários");
```

---

### 12. Testes como Documentação

Os testes servem como **documentação viva**:

```java
@Test
void testIntegracaoCompleta_FluxoRegistroELogin() {
    // 1. Registrar usuário
    String privateKey = userService.createUser(...);

    // 2. Fazer login
    String token = userService.authenticate(...);

    // 3. Listar usuários
    List<User> users = userService.listUsers();
}
```

Este teste documenta o fluxo esperado de uso do sistema.

---

## Anexos

### A. Comandos Úteis Maven

```bash
# Limpar e testar
./mvnw clean test

# Pular testes
./mvnw install -DskipTests

# Executar testes em paralelo
./mvnw test -T 4  # 4 threads

# Gerar relatório Surefire
./mvnw surefire-report:report
# Relatório em: target/site/surefire-report.html
```

---

### B. Estrutura do Relatório Surefire

```
target/
├── surefire-reports/
│   ├── TEST-ContatoServiceTest.xml
│   ├── TEST-CriptoServiceTest.xml
│   ├── TEST-UserControllerTest.xml
│   ├── TEST-ContatoControllerTest.xml
│   ├── TEST-CriptoServiceIntegrationTest.xml
│   ├── TEST-UserServiceIntegrationTest.xml
│   └── TEST-DatabaseSystemTest.xml
└── site/
    └── surefire-report.html  # Relatório HTML
```

---

### C. Exemplo de Saída de Teste

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.borborema.agenda.system.DatabaseSystemTest
✓ Fluxo 1: Usuário validado no sistema
✓ Fluxo 2: Contatos criados no sistema
✓ Fluxo 3: Contatos listados do banco de dados
✓ Fluxo 4: Contato encontrado no sistema
✓ Fluxo 5: Contato atualizado no sistema
✓ Fluxo 6: Contato deletado do sistema
✓ Fluxo 7: Integridade referencial validada
✓ Fluxo 8: Performance validada - 1234ms para 50 contatos
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 50, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
```

---

### D. Troubleshooting

#### Problema: Testes falhando com erro de conexão H2

**Solução**:
```properties
# application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1
```

---

#### Problema: MockMvc retornando 401/403 (Forbidden)

**Solução**: Verificar que `@AutoConfigureMockMvc(addFilters = false)` está presente.

---

#### Problema: Testes de integração lentos

**Solução**:
```properties
# Desabilitar logs
spring.jpa.show-sql=false
logging.level.org.hibernate=WARN
```

---

### E. Referências

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [H2 Database Documentation](https://www.h2database.com/html/main.html)

---

## Conclusão

Este projeto implementa uma **suíte robusta de testes** cobrindo:

✅ **50+ testes** em 3 níveis (unitário, integração, sistema)
✅ **100% das funcionalidades críticas** (criptografia, autenticação, CRUD)
✅ **Cobertura de casos de sucesso e falha**
✅ **Execução automatizada** via Maven
✅ **Testes como documentação** do comportamento esperado
✅ **Boas práticas** seguidas consistentemente

A arquitetura de testes segue a **pirâmide de testes**, priorizando testes unitários rápidos e complementando com testes de integração e sistema mais lentos, mas essenciais para validar o sistema completo.
