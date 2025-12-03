# Documento de Requisitos - Agenda Application

## Informações do Projeto

| Atributo | Valor |
|----------|-------|
| **Nome do Projeto** | Agenda Application |
| **Versão** | 0.0.1-SNAPSHOT |
| **Tecnologia Base** | Spring Boot 3.5.6 |
| **Linguagem** | Java 17 |
| **Tipo de Aplicação** | API REST com Criptografia End-to-End |
| **Grupo** | com.borborema |
| **Artefato** | agenda |

---

## Índice

1. [Visão Geral do Sistema](#visão-geral-do-sistema)
2. [Objetivos do Sistema](#objetivos-do-sistema)
3. [Requisitos Funcionais](#requisitos-funcionais)
   - [RF01 - RF08: Gestão de Usuários](#gestão-de-usuários)
   - [RF09 - RF18: Gestão de Contatos](#gestão-de-contatos)
   - [RF19 - RF22: Criptografia e Segurança](#criptografia-e-segurança)
4. [Requisitos Não-Funcionais](#requisitos-não-funcionais)
5. [Regras de Negócio](#regras-de-negócio)
6. [Modelo de Dados](#modelo-de-dados)
7. [Casos de Uso](#casos-de-uso)
8. [Especificação de APIs](#especificação-de-apis)
9. [Matriz de Rastreabilidade](#matriz-de-rastreabilidade)
10. [Validação de Requisitos via Testes](#validação-de-requisitos-via-testes)

---

## Visão Geral do Sistema

### Descrição

A **Agenda Application** é um sistema de gerenciamento de contatos com **criptografia end-to-end (E2E)** que garante a privacidade total dos dados pessoais armazenados. O sistema utiliza **criptografia RSA** para proteger informações sensíveis (nome, telefone, email) e **Cifra de César** para gerar tags de busca, permitindo que apenas o proprietário dos dados (usuário) possa descriptografá-los.

### Propósito

Fornecer uma agenda de contatos **segura e privada**, onde:
- Dados sensíveis são **criptografados antes de serem salvos** no banco de dados
- Apenas o usuário possui a **chave privada** para descriptografar seus dados
- Nem mesmo administradores do sistema podem acessar os dados em texto claro
- Autenticação via **JWT (JSON Web Token)**
- Controle de acesso baseado em **roles** (USER, ADMIN)

### Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│                     CLIENTE                             │
│  (Armazena chave privada RSA localmente)               │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ HTTPS + JWT
                     ↓
┌─────────────────────────────────────────────────────────┐
│                 API REST (Spring Boot)                  │
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │ Controllers  │  │  Services    │  │  Security    │ │
│  │ - User       │→ │ - User       │→ │ - JWT        │ │
│  │ - Contato    │  │ - Contato    │  │ - BCrypt     │ │
│  │              │  │ - Cripto     │  │ - Filters    │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
│                          ↓                              │
│                  ┌──────────────┐                       │
│                  │ Repositories │                       │
│                  └──────────────┘                       │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ↓
┌─────────────────────────────────────────────────────────┐
│            BANCO DE DADOS (H2)                          │
│  - Dados criptografados (RSA)                           │
│  - Senhas com hash (BCrypt)                             │
│  - Chaves públicas RSA                                  │
└─────────────────────────────────────────────────────────┘
```

---

## Objetivos do Sistema

### Objetivos Primários

1. **Privacidade Total**: Garantir que dados pessoais sejam armazenados de forma criptografada
2. **Autenticação Segura**: Implementar autenticação via JWT com senhas hash (BCrypt)
3. **Gestão de Contatos**: Permitir CRUD completo de contatos com criptografia E2E
4. **Controle de Acesso**: Implementar roles (USER, ADMIN) para controle de permissões

### Objetivos Secundários

1. **Performance**: Sistema deve criar 50 contatos em menos de 5 segundos
2. **Testabilidade**: Cobertura de testes > 85% do código
3. **Observabilidade**: Métricas via Actuator + Prometheus
4. **Documentação**: Documentação completa de requisitos e testes

---

## Requisitos Funcionais

### Gestão de Usuários

#### RF01: Cadastro de Usuário

**Descrição**: O sistema deve permitir o cadastro de novos usuários com geração automática de par de chaves RSA.

**Prioridade**: Alta

**Entrada**:
- Email (único)
- Senha (texto plano)
- Role (USER ou ADMIN)

**Processamento**:
1. Validar que email não existe no sistema
2. Gerar par de chaves RSA (pública/privada)
3. Criptografar senha com BCrypt
4. Salvar usuário no banco com chave pública
5. Retornar chave privada (Base64) para o cliente

**Saída**:
- Chave privada RSA (String Base64)

**Regras de Negócio**: RN01, RN02, RN03

**Testes**:
- `UserControllerTest#testCreateUser_Success` (unitário)
- `UserServiceIntegrationTest#testCreateUser_DeveGerarChavePrivada` (integração)
- `DatabaseSystemTest#test01_FluxoCompleto_VerificarUsuario` (sistema)

---

#### RF02: Autenticação de Usuário (Login)

**Descrição**: O sistema deve autenticar usuários e retornar token JWT válido.

**Prioridade**: Alta

**Entrada**:
- Email
- Senha

**Processamento**:
1. Buscar usuário por email
2. Validar senha usando BCrypt
3. Gerar token JWT com expiração
4. Retornar token

**Saída**:
- Token JWT (String)

**Regras de Negócio**: RN04, RN05

**Testes**:
- `UserControllerTest#testLogin_Success` (unitário)
- `UserServiceIntegrationTest#testAuthenticate_ComCredenciaisValidas` (integração)

---

#### RF03: Listagem de Usuários

**Descrição**: O sistema deve permitir listar todos os usuários cadastrados.

**Prioridade**: Média

**Entrada**: Nenhuma

**Processamento**:
1. Buscar todos os usuários do banco
2. Converter para UserDTO (sem senha)

**Saída**:
- Lista de UserDTO (userId, email, contatos)

**Regras de Negócio**: RN06

**Testes**:
- `UserControllerTest#testGetUsers_Success` (unitário)
- `UserServiceIntegrationTest#testListUsers_DeveRetornarTodosUsuarios` (integração)

---

#### RF04: Validação de Credenciais Inválidas

**Descrição**: O sistema deve rejeitar tentativas de login com credenciais inválidas.

**Prioridade**: Alta

**Entrada**:
- Email válido
- Senha incorreta

**Processamento**:
1. Buscar usuário por email
2. Tentar validar senha
3. Lançar exceção se senha incorreta

**Saída**:
- Mensagem de erro: "Não foi possível realizar o login"
- HTTP Status: 400 Bad Request

**Regras de Negócio**: RN04

**Testes**:
- `UserControllerTest#testLogin_Failure` (unitário)
- `UserServiceIntegrationTest#testAuthenticate_ComCredenciaisInvalidas_DeveLancarException` (integração)

---

#### RF05: Tratamento de Erro ao Criar Usuário

**Descrição**: O sistema deve tratar erros durante criação de usuário (email duplicado, erro de geração de chaves).

**Prioridade**: Alta

**Entrada**:
- UserRegisterDTO com email já existente

**Processamento**:
1. Tentar criar usuário
2. Capturar exceção (ex: email duplicado)
3. Retornar mensagem amigável

**Saída**:
- Mensagem: "Não foi possível cadastrar o usuario"
- HTTP Status: 400 Bad Request

**Regras de Negócio**: RN01

**Testes**:
- `UserControllerTest#testCreateUser_Failure` (unitário)

---

#### RF06: Listagem de Usuários com Banco Vazio

**Descrição**: O sistema deve retornar lista vazia quando não há usuários cadastrados.

**Prioridade**: Baixa

**Entrada**: Nenhuma

**Processamento**:
1. Buscar usuários (banco vazio)
2. Retornar lista vazia

**Saída**:
- Lista vazia `[]`

**Testes**:
- `UserControllerTest#testGetUsers_EmptyList` (unitário)
- `UserServiceIntegrationTest#testListUsers_ComBaseDeDadosVazia` (integração)

---

#### RF07: Geração de Chaves RSA Únicas

**Descrição**: O sistema deve gerar pares de chaves RSA únicos para cada usuário.

**Prioridade**: Alta

**Entrada**: Solicitação de criação de usuário

**Processamento**:
1. Gerar par de chaves RSA (2048 bits)
2. Garantir que chaves são diferentes para cada usuário

**Saída**:
- Par de chaves RSA único

**Regras de Negócio**: RN02

**Testes**:
- `CriptoServiceTest#testGenerateRSAKeyPar_GeraChavesDiferentes` (unitário)
- `UserServiceIntegrationTest#testCreateMultipleUsers_DevesGerarChavesRSADiferentes` (integração)

---

#### RF08: Criptografia de Senha com BCrypt

**Descrição**: O sistema deve criptografar senhas usando BCrypt antes de salvar no banco.

**Prioridade**: Alta

**Entrada**: Senha em texto plano

**Processamento**:
1. Aplicar BCrypt na senha
2. Salvar hash no banco

**Saída**:
- Senha hash iniciando com `$2a$`

**Regras de Negócio**: RN03

**Testes**:
- `UserServiceIntegrationTest#testCreateUser_DeveCriptografarSenha` (integração)

---

### Gestão de Contatos

#### RF09: Criar Contato

**Descrição**: O sistema deve permitir criar novos contatos com criptografia RSA de todos os campos sensíveis.

**Prioridade**: Alta

**Entrada**:
- ContatoDTO (nome, número, email, userId)

**Processamento**:
1. Buscar usuário por userId
2. Obter chave pública do usuário
3. Criptografar nome com RSA
4. Criptografar número (convertido para String) com RSA
5. Criptografar email com RSA
6. Gerar tag usando Cifra de César (chave=3) no nome
7. Salvar contato criptografado no banco

**Saída**:
- HTTP Status: 200 OK

**Regras de Negócio**: RN07, RN08, RN09, RN10

**Testes**:
- `ContatoServiceTest#testSalvarContato` (unitário)
- `ContatoControllerTest#testSalvarContato_Success` (unitário)
- `DatabaseSystemTest#test02_FluxoCompleto_CriarContatos` (sistema)

---

#### RF10: Listar Contatos do Usuário

**Descrição**: O sistema deve listar todos os contatos de um usuário, descriptografando os dados com a chave privada fornecida.

**Prioridade**: Alta

**Entrada**:
- userId (UUID)
- stringPrivateKey (chave privada em Base64)

**Processamento**:
1. Buscar contatos do usuário
2. Descriptografar nome, número e email com chave privada
3. Retornar lista de ContatoDAO (dados descriptografados)

**Saída**:
- Lista de ContatoDAO (nome, numero, email, modiefiedDate)

**Regras de Negócio**: RN11

**Testes**:
- `ContatoServiceTest#testFindContactsByUserId_Sucesso` (unitário)
- `ContatoControllerTest#testFindUserContacts_Success` (unitário)
- `DatabaseSystemTest#test03_FluxoCompleto_ListarContatos` (sistema)

---

#### RF11: Buscar Contato por Número

**Descrição**: O sistema deve permitir buscar um contato específico por número de telefone.

**Prioridade**: Média

**Entrada**:
- numero (Long)
- userId (UUID)
- stringPrivateKey

**Processamento**:
1. Criptografar número de busca com chave pública do usuário
2. Buscar contato com número criptografado
3. Descriptografar dados do contato encontrado
4. Retornar ContatoDAO

**Saída**:
- ContatoDAO (dados descriptografados)

**Regras de Negócio**: RN12

**Testes**:
- `ContatoControllerTest#testBuscarContatoPeloNumero_Success` (unitário)
- `DatabaseSystemTest#test04_FluxoCompleto_BuscarContato` (sistema)

---

#### RF12: Buscar Contato por Email

**Descrição**: O sistema deve permitir buscar um contato específico por email.

**Prioridade**: Média

**Entrada**:
- email (String)
- userId (UUID)
- stringPrivateKey

**Processamento**:
1. Criptografar email de busca com chave pública do usuário
2. Buscar contato com email criptografado
3. Descriptografar dados do contato encontrado
4. Retornar ContatoDAO

**Saída**:
- ContatoDAO (dados descriptografados)

**Regras de Negócio**: RN12

**Testes**:
- `ContatoControllerTest#testBuscarContatoPeloEmail_Success` (unitário)

---

#### RF13: Atualizar Contato

**Descrição**: O sistema deve permitir atualizar dados de um contato existente.

**Prioridade**: Alta

**Entrada**:
- numero (Long) - identificador do contato
- ContatoDTO (novos dados)
- stringPrivateKey

**Processamento**:
1. Buscar contato por número criptografado
2. Criptografar novos dados (nome, numero, email) com RSA
3. Gerar nova tag com Cifra de César
4. Atualizar contato no banco

**Saída**:
- HTTP Status: 200 OK

**Regras de Negócio**: RN13

**Testes**:
- `ContatoControllerTest#testAtualizarContatoPeloNumero_Success` (unitário)
- `DatabaseSystemTest#test05_FluxoCompleto_AtualizarContato` (sistema)

---

#### RF14: Deletar Contato

**Descrição**: O sistema deve permitir deletar um contato por número.

**Prioridade**: Alta

**Entrada**:
- numero (Long)
- userId (UUID)
- stringPrivateKey

**Processamento**:
1. Buscar contato por número criptografado
2. Validar que contato pertence ao usuário
3. Deletar contato do banco

**Saída**:
- HTTP Status: 200 OK

**Regras de Negócio**: RN14

**Testes**:
- `ContatoControllerTest#testDeletarContatoPeloNumero_Success` (unitário)
- `DatabaseSystemTest#test06_FluxoCompleto_DeletarContato` (sistema)

---

#### RF15: Tratamento de Erro - Usuário Não Encontrado ao Criar Contato

**Descrição**: O sistema deve retornar erro quando tentar criar contato para usuário inexistente.

**Prioridade**: Alta

**Entrada**:
- ContatoDTO com userId inválido

**Processamento**:
1. Tentar buscar usuário
2. Lançar exceção se não encontrado

**Saída**:
- Exceção: "Usuario não encontrado"

**Regras de Negócio**: RN07

**Testes**:
- `ContatoServiceTest#testSalvarContato_UsuarioNaoEncontrado` (unitário)

---

#### RF16: Tratamento de Erro - Contato Não Encontrado

**Descrição**: O sistema deve retornar erro amigável quando contato não for encontrado.

**Prioridade**: Média

**Entrada**:
- Número ou email inexistente

**Processamento**:
1. Tentar buscar contato
2. Capturar exceção
3. Retornar mensagem amigável

**Saída**:
- "Não foi possível encontrar o contato pelo número"
- "Não foi possível encontrar o contato pelo e-mail"

**Testes**:
- `ContatoControllerTest#testBuscarContatoPeloNumero_Failure` (unitário)
- `ContatoControllerTest#testBuscarContatoPeloEmail_Failure` (unitário)

---

#### RF17: Tratamento de Erro ao Deletar Contato

**Descrição**: O sistema deve tratar erros ao deletar contato.

**Prioridade**: Média

**Entrada**:
- Número inválido ou contato não pertencente ao usuário

**Processamento**:
1. Tentar deletar contato
2. Capturar exceção
3. Retornar mensagem amigável

**Saída**:
- "Não foi possível deletar o contato"

**Testes**:
- `ContatoControllerTest#testDeletarContatoPeloNumero_Failure` (unitário)

---

#### RF18: Tratamento de Erro ao Atualizar Contato

**Descrição**: O sistema deve tratar erros ao atualizar contato.

**Prioridade**: Média

**Entrada**:
- Dados inválidos ou contato não encontrado

**Processamento**:
1. Tentar atualizar contato
2. Capturar exceção
3. Retornar mensagem amigável

**Saída**:
- "Não foi possível atualizar o contato"

**Testes**:
- `ContatoControllerTest#testAtualizarContatoPeloNumero_Failure` (unitário)

---

### Criptografia e Segurança

#### RF19: Criptografia RSA

**Descrição**: O sistema deve implementar criptografia/descriptografia RSA para proteger dados sensíveis.

**Prioridade**: Alta

**Entrada**:
- Texto plano
- Chave pública (para criptografar) ou privada (para descriptografar)

**Processamento**:
1. Aplicar algoritmo RSA/ECB/PKCS1Padding
2. Codificar resultado em Base64

**Saída**:
- Texto criptografado (Base64)

**Regras de Negócio**: RN15

**Testes**:
- `CriptoServiceTest#testRsaEncrypt_EncriptaTexto` (unitário)
- `CriptoServiceTest#testRsaDecrypt_DecriptaTextoEncriptado` (unitário)
- `CriptoServiceTest#testRsaEncryptDecrypt_ComDiferentesTextos` (unitário)
- `CriptoServiceIntegrationTest#testIntegracaoRSA_EncriptarEDescriptografar` (integração)

---

#### RF20: Cifra de César

**Descrição**: O sistema deve implementar Cifra de César para gerar tags de busca.

**Prioridade**: Média

**Entrada**:
- Nome do contato
- Chave de deslocamento (padrão: 3)

**Processamento**:
1. Remover acentos e caracteres especiais
2. Converter para lowercase
3. Aplicar deslocamento César no alfabeto

**Saída**:
- Tag criptografada (String)

**Regras de Negócio**: RN16

**Testes**:
- `CriptoServiceTest#testCesarEncrypt_ComNomeSimples` (unitário)
- `CriptoServiceTest#testCesarEncrypt_ComNomeComplexo` (unitário)
- `CriptoServiceTest#testCesarEncrypt_ComCaracteresEspeciais` (unitário)
- `CriptoServiceTest#testCesarDecrypt_DecriptaCesarEncrypt` (unitário)
- `CriptoServiceTest#testCesarEncryptDecrypt_ComDiferentesChaves` (unitário)
- `CriptoServiceIntegrationTest#testIntegracaoCesar_EncriptarEDescriptografar` (integração)

---

#### RF21: Validação de Chaves RSA

**Descrição**: O sistema deve validar chaves RSA e rejeitar chaves inválidas/nulas.

**Prioridade**: Alta

**Entrada**:
- Chave pública/privada inválida ou nula

**Processamento**:
1. Validar chave antes de usar
2. Lançar exceção se inválida

**Saída**:
- RuntimeException para chaves inválidas

**Regras de Negócio**: RN17

**Testes**:
- `CriptoServiceTest#testRsaEncrypt_ComChaveInvalida_DeveLancarException` (unitário)
- `CriptoServiceTest#testRsaDecrypt_ComChaveInvalida_DeveLancarException` (unitário)

---

#### RF22: Isolamento de Chaves entre Usuários

**Descrição**: O sistema deve garantir que chaves privadas de um usuário não possam descriptografar dados de outro usuário.

**Prioridade**: Alta

**Entrada**:
- Dados criptografados com chave pública do usuário A
- Chave privada do usuário B

**Processamento**:
1. Tentar descriptografar
2. Lançar exceção por incompatibilidade de chaves

**Saída**:
- RuntimeException

**Regras de Negócio**: RN18

**Testes**:
- `CriptoServiceTest#testRsaEncryptDecrypt_ChavesDiferentesNaoFuncionam` (unitário)

---

## Requisitos Não-Funcionais

### RNF01: Performance

**Descrição**: O sistema deve ter performance adequada para operações em massa.

**Critério de Aceitação**:
- Criar 50 contatos em menos de 5 segundos

**Prioridade**: Média

**Testes**:
- `DatabaseSystemTest#test08_FluxoCompleto_TesteDePerformance` (sistema)

---

### RNF02: Segurança - Criptografia de Dados

**Descrição**: Todos os dados sensíveis (nome, email, telefone) devem ser criptografados com RSA antes de salvar no banco.

**Critério de Aceitação**:
- Dados no banco estão em formato Base64 (criptografados)
- Apenas chave privada do usuário pode descriptografar

**Prioridade**: Alta

**Implementação**:
- Algoritmo: RSA/ECB/PKCS1Padding
- Tamanho da chave: 2048 bits

**Testes**: Todos os testes de `CriptoServiceTest` e `ContatoServiceTest`

---

### RNF03: Segurança - Autenticação JWT

**Descrição**: O sistema deve usar tokens JWT para autenticação com expiração configurável.

**Critério de Aceitação**:
- Token JWT gerado após login bem-sucedido
- Token contém subject (email do usuário)
- Token tem expiração configurável

**Prioridade**: Alta

**Implementação**:
- Biblioteca: com.auth0:java-jwt:4.4.0
- Algoritmo: HMAC256

---

### RNF04: Segurança - Hash de Senhas

**Descrição**: Senhas devem ser armazenadas usando BCrypt (nunca em texto plano).

**Critério de Aceitação**:
- Senha no banco começa com `$2a$` (prefixo BCrypt)
- Senha original não pode ser recuperada

**Prioridade**: Alta

**Testes**:
- `UserServiceIntegrationTest#testCreateUser_DeveCriptografarSenha` (integração)

---

### RNF05: Disponibilidade - Banco de Dados

**Descrição**: O sistema deve usar banco de dados em memória (H2) para testes e desenvolvimento.

**Critério de Aceitação**:
- Banco H2 configurado
- Schema criado/destruído automaticamente (`create-drop`)

**Prioridade**: Baixa

**Implementação**:
- URL: `jdbc:h2:mem:testdb`
- Dialect: Hibernate H2Dialect

---

### RNF06: Testabilidade

**Descrição**: O sistema deve ter alta cobertura de testes (>85%).

**Critério de Aceitação**:
- Testes unitários > 60%
- Testes de integração cobrindo fluxos críticos
- Testes de sistema validando E2E

**Prioridade**: Alta

**Implementação**:
- JUnit 5
- Mockito
- Spring Boot Test
- JaCoCo para cobertura

**Evidências**: 50+ testes implementados em 7 classes

---

### RNF07: Observabilidade

**Descrição**: O sistema deve expor métricas via Actuator e Prometheus.

**Critério de Aceitação**:
- Endpoint `/actuator/prometheus` disponível
- Métricas de JVM, HTTP, banco de dados expostas

**Prioridade**: Média

**Implementação**:
- Spring Boot Actuator
- Micrometer Registry Prometheus

---

### RNF08: Manutenibilidade

**Descrição**: O código deve seguir boas práticas e padrões.

**Critério de Aceitação**:
- Arquitetura em camadas (Controller → Service → Repository)
- Uso de DTOs para transferência de dados
- Separação de responsabilidades
- Código documentado

**Prioridade**: Alta

**Evidências**:
- Estrutura clara de pacotes
- Uso de Lombok para reduzir boilerplate
- Testes como documentação viva

---

### RNF09: Usabilidade - Mensagens de Erro

**Descrição**: O sistema deve retornar mensagens de erro amigáveis ao usuário.

**Critério de Aceitação**:
- Erros retornam mensagens em português
- Mensagens não expõem detalhes técnicos (stack trace)
- HTTP Status adequado (400 para erros de validação)

**Prioridade**: Média

**Exemplos**:
- "Não foi possível cadastrar o usuario"
- "Não foi possível realizar o login"
- "Não foi possível encontrar o contato pelo número"

**Testes**: Todos os testes `*_Failure` nos Controllers

---

### RNF10: Portabilidade

**Descrição**: O sistema deve ser executável em qualquer ambiente com Java 17+.

**Critério de Aceitação**:
- Build via Maven
- Sem dependências de SO específico
- Banco H2 em memória (sem instalação externa)

**Prioridade**: Média

**Implementação**:
- Spring Boot 3.5.6
- Java 17
- Maven wrapper incluído

---

## Regras de Negócio

### RN01: Unicidade de Email

**Descrição**: O sistema não pode permitir cadastro de emails duplicados.

**Validação**:
- Coluna `email` tem constraint `UNIQUE`
- Tentativa de cadastro com email existente lança exceção

**Impacto**: RF01, RF05

---

### RN02: Geração Automática de Chaves RSA

**Descrição**: Ao criar um usuário, o sistema deve gerar automaticamente um par de chaves RSA único.

**Detalhes**:
- Tamanho da chave: 2048 bits
- Chave pública salva no banco (Base64)
- Chave privada retornada ao cliente (Base64)
- Cliente é responsável por armazenar chave privada

**Impacto**: RF01, RF07

---

### RN03: Criptografia de Senha

**Descrição**: Senhas devem ser criptografadas com BCrypt antes de salvar.

**Detalhes**:
- Algoritmo: BCrypt
- Força: padrão (10 rounds)
- Senha original nunca é salva

**Impacto**: RF01, RF08

---

### RN04: Validação de Credenciais

**Descrição**: Login só é bem-sucedido se email existe E senha está correta.

**Detalhes**:
- Comparação usa BCrypt.checkpw()
- Falha em qualquer validação retorna erro genérico

**Impacto**: RF02, RF04

---

### RN05: Geração de Token JWT

**Descrição**: Token JWT deve conter subject (email) e expiração.

**Detalhes**:
- Subject: email do usuário
- Issuer: configureável
- Algoritmo: HMAC256
- Expiração: configureável (ex: 2 horas)

**Impacto**: RF02

---

### RN06: Conversão para DTO

**Descrição**: Listagem de usuários nunca deve retornar senhas.

**Detalhes**:
- Usar UserDTO ao invés de User entity
- UserDTO não contém campo password

**Impacto**: RF03

---

### RN07: Validação de Usuário ao Criar Contato

**Descrição**: Só é possível criar contato para usuário existente.

**Detalhes**:
- Buscar usuário por userId antes de criar contato
- Lançar exceção "Usuario não encontrado" se não existir

**Impacto**: RF09, RF15

---

### RN08: Criptografia de Nome

**Descrição**: Nome do contato deve ser criptografado com RSA usando chave pública do usuário.

**Detalhes**:
- Algoritmo: RSA/ECB/PKCS1Padding
- Encoding: Base64
- Armazenado como TEXT (CLOB)

**Impacto**: RF09

---

### RN09: Criptografia de Número

**Descrição**: Número de telefone deve ser convertido para String e criptografado com RSA.

**Detalhes**:
- Converter Long → String
- Criptografar com RSA
- Armazenar como TEXT (CLOB)
- Coluna `numero` tem constraint `UNIQUE`

**Impacto**: RF09

---

### RN10: Geração de Tag com Cifra de César

**Descrição**: Uma tag de busca deve ser gerada usando Cifra de César no nome.

**Detalhes**:
- Entrada: nome original (antes de criptografar)
- Chave de deslocamento: 3
- Processo: remover acentos → lowercase → aplicar César
- Armazenar em campo `tag`

**Propósito**: Permitir buscas aproximadas sem descriptografar todos os nomes

**Impacto**: RF09

---

### RN11: Descriptografia ao Listar Contatos

**Descrição**: Ao listar contatos, sistema deve descriptografar dados usando chave privada fornecida.

**Detalhes**:
- Cliente envia chave privada via query param
- Sistema descriptografa nome, numero, email
- Retorna ContatoDAO (dados em texto plano)
- Se descriptografia falhar, retorna dados criptografados

**Impacto**: RF10

---

### RN12: Busca por Dados Criptografados

**Descrição**: Para buscar por número/email, sistema deve criptografar o valor de busca primeiro.

**Detalhes**:
1. Cliente fornece número/email em texto plano
2. Sistema criptografa valor com chave pública do usuário
3. Busca no banco pelo valor criptografado
4. Descriptografa resultado antes de retornar

**Impacto**: RF11, RF12

---

### RN13: Atualização de Contato

**Descrição**: Ao atualizar contato, todos os campos devem ser re-criptografados.

**Detalhes**:
- Buscar contato existente
- Criptografar novos valores (nome, numero, email)
- Gerar nova tag
- Atualizar campo `modiefiedDate`

**Impacto**: RF13

---

### RN14: Validação de Propriedade ao Deletar

**Descrição**: Usuário só pode deletar seus próprios contatos.

**Detalhes**:
- Validar que contato pertence ao userId fornecido
- Lançar exceção se não pertencer

**Impacto**: RF14

---

### RN15: Algoritmo RSA

**Descrição**: Criptografia RSA deve usar configuração segura.

**Detalhes**:
- Algoritmo: RSA
- Modo: ECB
- Padding: PKCS1Padding
- Tamanho da chave: 2048 bits

**Impacto**: RF19, RNF02

---

### RN16: Normalização de Nome para Tag

**Descrição**: Antes de aplicar Cifra de César, nome deve ser normalizado.

**Processo**:
1. Remover acentos (á→a, ç→c, etc)
2. Remover caracteres especiais e números
3. Remover espaços
4. Converter para lowercase

**Exemplo**: "João Silva" → "joosilva"

**Impacto**: RF20

---

### RN17: Validação de Chaves

**Descrição**: Todas as operações de criptografia devem validar chaves antes de usar.

**Validações**:
- Chave não pode ser null
- Chave deve ser válida (formato correto)
- Lançar RuntimeException se inválida

**Impacto**: RF21, RNF02

---

### RN18: Isolamento de Dados entre Usuários

**Descrição**: Dados criptografados com chave de um usuário não podem ser descriptografados com chave de outro.

**Garantia**:
- Cada usuário tem par de chaves único
- Tentativa de descriptografar com chave errada lança exceção
- Mesmo administradores não podem acessar dados de outros usuários

**Impacto**: RF22, RNF02

---

## Modelo de Dados

### Entidade: User

```sql
CREATE TABLE users (
    user_id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- Hash BCrypt
    role VARCHAR(20) NOT NULL,        -- 'USER' ou 'ADMIN'
    public_key TEXT                   -- Chave pública RSA (Base64)
);
```

**Relacionamentos**:
- Um User pode ter vários Contatos (OneToMany)

**Constraints**:
- `email` deve ser único
- `password` nunca é null
- `public_key` armazena chave RSA em Base64

---

### Entidade: Contato

```sql
CREATE TABLE contato (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    nome TEXT UNIQUE,               -- Nome criptografado (RSA + Base64)
    numero TEXT UNIQUE,             -- Telefone criptografado (RSA + Base64)
    email TEXT,                     -- Email criptografado (RSA + Base64)
    tag VARCHAR(255),               -- Tag gerada com Cifra de César
    modiefied_date TIMESTAMP,       -- Data de última modificação
    user_id UUID,                   -- FK para users
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

**Relacionamentos**:
- Muitos Contatos pertencem a um User (ManyToOne)
- Cascade: ALL (deletar usuário deleta seus contatos)
- Orphan removal: true

**Constraints**:
- `nome` e `numero` são únicos (valores criptografados)
- `user_id` não pode ser null

---

### Diagrama ER

```
┌─────────────────────────┐
│         User            │
├─────────────────────────┤
│ user_id (PK) UUID       │
│ email UNIQUE            │
│ password (BCrypt)       │
│ role (ENUM)             │
│ public_key (TEXT)       │
└───────────┬─────────────┘
            │
            │ 1:N
            │
            ↓
┌─────────────────────────┐
│       Contato           │
├─────────────────────────┤
│ id (PK) INTEGER         │
│ nome TEXT (criptografado)│
│ numero TEXT (criptografado)│
│ email TEXT (criptografado)│
│ tag VARCHAR             │
│ modiefied_date TIMESTAMP│
│ user_id (FK) UUID       │
└─────────────────────────┘
```

---

## Casos de Uso

### UC01: Cadastro de Novo Usuário

**Ator**: Cliente (aplicação frontend/mobile)

**Pré-condições**:
- Email ainda não existe no sistema

**Fluxo Principal**:
1. Cliente envia POST `/user/create` com UserRegisterDTO (email, senha, role)
2. Sistema valida que email não existe
3. Sistema gera par de chaves RSA (2048 bits)
4. Sistema criptografa senha com BCrypt
5. Sistema salva usuário com chave pública no banco
6. Sistema retorna chave privada (Base64) para o cliente
7. Cliente armazena chave privada localmente de forma segura

**Fluxo Alternativo 1a: Email Duplicado**:
- 2a. Sistema detecta email duplicado
- 2b. Sistema retorna erro 400: "Não foi possível cadastrar o usuario"
- Fim do caso de uso

**Pós-condições**:
- Usuário criado no banco
- Cliente possui chave privada
- Chave privada NUNCA é salva no servidor

**Requisitos**: RF01

**Testes**: `testCreateUser_Success`, `testCreateUser_DeveGerarChavePrivada`

---

### UC02: Login de Usuário

**Ator**: Usuário cadastrado

**Pré-condições**:
- Usuário já cadastrado
- Credenciais válidas

**Fluxo Principal**:
1. Cliente envia POST `/user/login` com UserAuthenticationDTO (email, senha)
2. Sistema busca usuário por email
3. Sistema valida senha usando BCrypt
4. Sistema gera token JWT com expiração
5. Sistema retorna token JWT
6. Cliente armazena token para usar em requisições futuras

**Fluxo Alternativo 2a: Credenciais Inválidas**:
- 3a. Senha não confere ou email não existe
- 3b. Sistema retorna erro 400: "Não foi possível realizar o login"
- Fim do caso de uso

**Pós-condições**:
- Cliente possui token JWT válido
- Token pode ser usado para autenticação em endpoints protegidos

**Requisitos**: RF02, RF04

**Testes**: `testLogin_Success`, `testLogin_Failure`

---

### UC03: Criar Contato Criptografado

**Ator**: Usuário autenticado

**Pré-condições**:
- Usuário autenticado com JWT
- Usuário possui chave privada armazenada

**Fluxo Principal**:
1. Cliente envia POST `/agenda` com ContatoDTO (nome, numero, email, userId)
2. Sistema busca usuário por userId
3. Sistema obtém chave pública do usuário
4. Sistema criptografa nome com RSA usando chave pública
5. Sistema criptografa numero (Long→String) com RSA
6. Sistema criptografa email com RSA
7. Sistema gera tag aplicando Cifra de César (chave=3) no nome original
8. Sistema salva contato criptografado no banco
9. Sistema retorna 200 OK

**Fluxo Alternativo 2a: Usuário Não Encontrado**:
- 2a. userId não existe no banco
- 2b. Sistema lança exceção "Usuario não encontrado"
- 2c. Sistema retorna erro 400: "Não foi possível cadastrar o contato"
- Fim do caso de uso

**Pós-condições**:
- Contato salvo no banco com dados criptografados
- Apenas chave privada do usuário pode descriptografar

**Requisitos**: RF09, RF15

**Testes**: `testSalvarContato`, `testSalvarContato_Success`, `test02_FluxoCompleto_CriarContatos`

---

### UC04: Listar Contatos Descriptografados

**Ator**: Usuário autenticado

**Pré-condições**:
- Usuário possui contatos cadastrados
- Cliente possui chave privada do usuário

**Fluxo Principal**:
1. Cliente envia GET `/agenda/list/user/contacts?userId={uuid}&stringPrivateKey={key}`
2. Sistema busca contatos do usuário no banco (dados criptografados)
3. Sistema descriptografa nome usando chave privada fornecida
4. Sistema descriptografa numero usando chave privada
5. Sistema descriptografa email usando chave privada
6. Sistema retorna lista de ContatoDAO (dados em texto plano)

**Fluxo Alternativo 3a: Chave Privada Inválida**:
- 3a. Chave privada não corresponde aos dados
- 3b. Descriptografia falha
- 3c. Sistema retorna dados criptografados (fallback)

**Pós-condições**:
- Cliente recebe lista de contatos descriptografados

**Requisitos**: RF10

**Testes**: `testFindContactsByUserId_Sucesso`, `testFindUserContacts_Success`, `test03_FluxoCompleto_ListarContatos`

---

### UC05: Buscar Contato por Número

**Ator**: Usuário autenticado

**Pré-condições**:
- Usuário possui contatos cadastrados
- Cliente sabe o número do contato (texto plano)

**Fluxo Principal**:
1. Cliente envia GET `/agenda/list/user/contact/number?numero={numero}&userId={uuid}&stringPrivateKey={key}`
2. Sistema busca usuário e obtém chave pública
3. Sistema criptografa número de busca com chave pública
4. Sistema busca contato no banco pelo número criptografado
5. Sistema descriptografa dados do contato com chave privada fornecida
6. Sistema retorna ContatoDAO

**Fluxo Alternativo 4a: Contato Não Encontrado**:
- 4a. Nenhum contato com aquele número criptografado
- 4b. Sistema retorna erro 400: "Não foi possível encontrar o contato pelo número"
- Fim do caso de uso

**Pós-condições**:
- Cliente recebe contato específico descriptografado

**Requisitos**: RF11

**Testes**: `testBuscarContatoPeloNumero_Success`, `test04_FluxoCompleto_BuscarContato`

---

### UC06: Atualizar Contato

**Ator**: Usuário autenticado

**Pré-condições**:
- Contato existe
- Cliente possui chave privada

**Fluxo Principal**:
1. Cliente envia PUT `/agenda?numero={numero}&stringPrivateKey={key}` + ContatoDTO (novos dados)
2. Sistema busca contato existente pelo número criptografado
3. Sistema obtém chave pública do usuário
4. Sistema criptografa novos dados (nome, numero, email) com RSA
5. Sistema gera nova tag com Cifra de César
6. Sistema atualiza campo `modiefiedDate`
7. Sistema salva contato atualizado
8. Sistema retorna 200 OK

**Pós-condições**:
- Contato atualizado no banco com novos dados criptografados
- Data de modificação atualizada

**Requisitos**: RF13

**Testes**: `testAtualizarContatoPeloNumero_Success`, `test05_FluxoCompleto_AtualizarContato`

---

### UC07: Deletar Contato

**Ator**: Usuário autenticado

**Pré-condições**:
- Contato existe
- Contato pertence ao usuário

**Fluxo Principal**:
1. Cliente envia DELETE `/agenda?numero={numero}&userId={uuid}&stringPrivateKey={key}`
2. Sistema busca contato pelo número criptografado
3. Sistema valida que contato pertence ao userId fornecido
4. Sistema deleta contato do banco
5. Sistema retorna 200 OK

**Pós-condições**:
- Contato removido do banco permanentemente

**Requisitos**: RF14

**Testes**: `testDeletarContatoPeloNumero_Success`, `test06_FluxoCompleto_DeletarContato`

---

### UC08: Fluxo End-to-End Completo

**Ator**: Novo usuário

**Fluxo Principal**:
1. **Registro**: Cliente se cadastra (UC01) e recebe chave privada
2. **Login**: Cliente faz login (UC02) e recebe token JWT
3. **Criar Contatos**: Cliente cria vários contatos (UC03)
4. **Listar**: Cliente lista todos os contatos (UC04)
5. **Buscar**: Cliente busca contato específico (UC05)
6. **Atualizar**: Cliente atualiza dados de um contato (UC06)
7. **Deletar**: Cliente remove um contato (UC07)

**Pós-condições**:
- Sistema mantém integridade dos dados
- Performance adequada (50 contatos < 5s)
- Todos os dados criptografados corretamente

**Requisitos**: RF01-RF22, RNF01

**Testes**: `DatabaseSystemTest` (8 testes ordenados)

---

## Especificação de APIs

### Endpoints de Usuário

#### POST /user/create

**Descrição**: Cria novo usuário e retorna chave privada RSA

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "senha123",
  "userRole": "USER"
}
```

**Response (200 OK)**:
```text
MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...
```
(Chave privada RSA em Base64)

**Response (400 Bad Request)**:
```text
Não foi possível cadastrar o usuario
```

**Testes**: `testCreateUser_Success`, `testCreateUser_Failure`

---

#### POST /user/login

**Descrição**: Autentica usuário e retorna token JWT

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "senha123"
}
```

**Response (200 OK)**:
```text
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```
(Token JWT)

**Response (400 Bad Request)**:
```text
Não foi possível realizar o login
```

**Testes**: `testLogin_Success`, `testLogin_Failure`

---

#### GET /user/list

**Descrição**: Lista todos os usuários cadastrados

**Response (200 OK)**:
```json
[
  {
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "email": "user1@example.com",
    "contatos": [...]
  },
  {
    "userId": "123e4567-e89b-12d3-a456-426614174001",
    "email": "user2@example.com",
    "contatos": []
  }
]
```

**Response (200 OK - Lista vazia)**:
```json
[]
```

**Testes**: `testGetUsers_Success`, `testGetUsers_EmptyList`

---

### Endpoints de Contato

#### POST /agenda

**Descrição**: Cria novo contato criptografado

**Request Body**:
```json
{
  "nome": "João Silva",
  "numero": 123456789,
  "email": "joao@gmail.com",
  "userId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Response (200 OK)**: Corpo vazio

**Response (400 Bad Request)**:
```text
Não foi possível cadastrar o contato
```

**Testes**: `testSalvarContato_Success`, `testSalvarContato_Failure`

---

#### GET /agenda/list/user/contacts

**Descrição**: Lista contatos do usuário (descriptografados)

**Query Parameters**:
- `userId` (UUID): ID do usuário
- `stringPrivateKey` (String): Chave privada em Base64

**Response (200 OK)**:
```json
[
  {
    "nome": "João Silva",
    "numero": "123456789",
    "email": "joao@gmail.com",
    "modiefiedDate": "2025-01-15T10:30:00"
  },
  {
    "nome": "Maria Santos",
    "numero": "987654321",
    "email": "maria@gmail.com",
    "modiefiedDate": "2025-01-15T11:00:00"
  }
]
```

**Response (400 Bad Request)**:
```text
Não foi possível retornar os contatos
```

**Testes**: `testFindUserContacts_Success`, `testFindUserContacts_Failure`

---

#### GET /agenda/list/user/contact/number

**Descrição**: Busca contato por número de telefone

**Query Parameters**:
- `numero` (Long): Número do telefone
- `userId` (UUID): ID do usuário
- `stringPrivateKey` (String): Chave privada em Base64

**Response (200 OK)**:
```json
{
  "nome": "João Silva",
  "numero": "123456789",
  "email": "joao@gmail.com",
  "modiefiedDate": "2025-01-15T10:30:00"
}
```

**Response (400 Bad Request)**:
```text
Não foi possível encontrar o contato pelo número
```

**Testes**: `testBuscarContatoPeloNumero_Success`, `testBuscarContatoPeloNumero_Failure`

---

#### GET /agenda/list/user/contact/email

**Descrição**: Busca contato por email

**Query Parameters**:
- `email` (String): Email do contato
- `userId` (UUID): ID do usuário
- `stringPrivateKey` (String): Chave privada em Base64

**Response (200 OK)**:
```json
{
  "nome": "João Silva",
  "numero": "123456789",
  "email": "joao@gmail.com",
  "modiefiedDate": "2025-01-15T10:30:00"
}
```

**Response (400 Bad Request)**:
```text
Não foi possível encontrar o contato pelo e-mail
```

**Testes**: `testBuscarContatoPeloEmail_Success`, `testBuscarContatoPeloEmail_Failure`

---

#### PUT /agenda

**Descrição**: Atualiza contato existente

**Query Parameters**:
- `numero` (Long): Número do contato a atualizar
- `stringPrivateKey` (String): Chave privada em Base64

**Request Body**:
```json
{
  "nome": "João Pedro Silva",
  "numero": 987654321,
  "email": "joaonovo@gmail.com",
  "userId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Response (200 OK)**: Corpo vazio

**Response (400 Bad Request)**:
```text
Não foi possível atualizar o contato
```

**Testes**: `testAtualizarContatoPeloNumero_Success`, `testAtualizarContatoPeloNumero_Failure`

---

#### DELETE /agenda

**Descrição**: Deleta contato por número

**Query Parameters**:
- `numero` (Long): Número do contato a deletar
- `userId` (UUID): ID do usuário
- `stringPrivateKey` (String): Chave privada em Base64

**Response (200 OK)**: Corpo vazio

**Response (400 Bad Request)**:
```text
Não foi possível deletar o contato
```

**Testes**: `testDeletarContatoPeloNumero_Success`, `testDeletarContatoPeloNumero_Failure`

---

## Matriz de Rastreabilidade

| Requisito | Regra de Negócio | Caso de Uso | Testes | Status |
|-----------|------------------|-------------|--------|--------|
| RF01 | RN01, RN02, RN03 | UC01 | testCreateUser_Success, testCreateUser_DeveGerarChavePrivada | ✅ Validado |
| RF02 | RN04, RN05 | UC02 | testLogin_Success, testAuthenticate_ComCredenciaisValidas | ✅ Validado |
| RF03 | RN06 | - | testGetUsers_Success, testListUsers_DeveRetornarTodosUsuarios | ✅ Validado |
| RF04 | RN04 | UC02 (alternativo) | testLogin_Failure, testAuthenticate_ComCredenciaisInvalidas | ✅ Validado |
| RF05 | RN01 | UC01 (alternativo) | testCreateUser_Failure | ✅ Validado |
| RF06 | - | - | testGetUsers_EmptyList, testListUsers_ComBaseDeDadosVazia | ✅ Validado |
| RF07 | RN02 | UC01 | testGenerateRSAKeyPar_GeraChavesDiferentes | ✅ Validado |
| RF08 | RN03 | UC01 | testCreateUser_DeveCriptografarSenha | ✅ Validado |
| RF09 | RN07, RN08, RN09, RN10 | UC03 | testSalvarContato, test02_FluxoCompleto_CriarContatos | ✅ Validado |
| RF10 | RN11 | UC04 | testFindContactsByUserId_Sucesso, test03_FluxoCompleto_ListarContatos | ✅ Validado |
| RF11 | RN12 | UC05 | testBuscarContatoPeloNumero_Success, test04_FluxoCompleto_BuscarContato | ✅ Validado |
| RF12 | RN12 | - | testBuscarContatoPeloEmail_Success | ✅ Validado |
| RF13 | RN13 | UC06 | testAtualizarContatoPeloNumero_Success, test05_FluxoCompleto_AtualizarContato | ✅ Validado |
| RF14 | RN14 | UC07 | testDeletarContatoPeloNumero_Success, test06_FluxoCompleto_DeletarContato | ✅ Validado |
| RF15 | RN07 | UC03 (alternativo) | testSalvarContato_UsuarioNaoEncontrado | ✅ Validado |
| RF16 | - | UC05 (alternativo) | testBuscarContatoPeloNumero_Failure, testBuscarContatoPeloEmail_Failure | ✅ Validado |
| RF17 | - | UC07 (alternativo) | testDeletarContatoPeloNumero_Failure | ✅ Validado |
| RF18 | - | UC06 (alternativo) | testAtualizarContatoPeloNumero_Failure | ✅ Validado |
| RF19 | RN15 | UC03, UC04 | testRsaEncrypt_EncriptaTexto, testRsaDecrypt_DecriptaTextoEncriptado (10 testes) | ✅ Validado |
| RF20 | RN16 | UC03 | testCesarEncrypt_ComNomeSimples, testCesarEncryptDecrypt_ComDiferentesChaves (5 testes) | ✅ Validado |
| RF21 | RN17 | - | testRsaEncrypt_ComChaveInvalida, testRsaDecrypt_ComChaveInvalida | ✅ Validado |
| RF22 | RN18 | - | testRsaEncryptDecrypt_ChavesDiferentesNaoFuncionam | ✅ Validado |
| RNF01 | - | UC08 | test08_FluxoCompleto_TesteDePerformance | ✅ Validado |
| RNF02 | RN15, RN17, RN18 | UC03-UC07 | CriptoServiceTest (13 testes), ContatoServiceTest (5 testes) | ✅ Validado |
| RNF03 | RN05 | UC02 | testLogin_Success, testAuthenticate_ComCredenciaisValidas | ✅ Validado |
| RNF04 | RN03 | UC01 | testCreateUser_DeveCriptografarSenha | ✅ Validado |
| RNF06 | - | - | 50+ testes em 7 classes | ✅ Validado |
| RNF09 | - | - | Todos os testes *_Failure | ✅ Validado |

---

## Validação de Requisitos via Testes

### Resumo de Cobertura

| Categoria | Total de Requisitos | Requisitos Testados | Cobertura |
|-----------|---------------------|---------------------|-----------|
| **Funcionais** | 22 | 22 | 100% |
| **Não-Funcionais** | 10 | 6 | 60% |
| **Regras de Negócio** | 18 | 18 | 100% |
| **TOTAL** | 50 | 46 | **92%** |

### Requisitos Não-Funcionais Não Testados

| Requisito | Motivo |
|-----------|--------|
| RNF05 (Disponibilidade - Banco H2) | Configuração de infraestrutura, não requer teste |
| RNF07 (Observabilidade) | Métricas não testadas automaticamente |
| RNF08 (Manutenibilidade) | Avaliação qualitativa do código |
| RNF10 (Portabilidade) | Build Maven validado em CI/CD |

### Casos de Teste por Requisito

#### RF01: Cadastro de Usuário (3 testes)
1. `UserControllerTest#testCreateUser_Success` - Testa endpoint REST
2. `UserServiceIntegrationTest#testCreateUser_DeveGerarChavePrivada` - Testa geração de chaves
3. `DatabaseSystemTest#test01_FluxoCompleto_VerificarUsuario` - Testa persistência

#### RF02: Autenticação (2 testes)
1. `UserControllerTest#testLogin_Success` - Testa endpoint
2. `UserServiceIntegrationTest#testAuthenticate_ComCredenciaisValidas` - Testa lógica de autenticação

#### RF09: Criar Contato (3 testes)
1. `ContatoServiceTest#testSalvarContato` - Testa lógica de negócio
2. `ContatoControllerTest#testSalvarContato_Success` - Testa endpoint
3. `DatabaseSystemTest#test02_FluxoCompleto_CriarContatos` - Testa E2E

#### RF19: Criptografia RSA (10 testes)
1. `CriptoServiceTest#testRsaEncrypt_EncriptaTexto`
2. `CriptoServiceTest#testRsaDecrypt_DecriptaTextoEncriptado`
3. `CriptoServiceTest#testRsaEncryptDecrypt_ComDiferentesTextos` (4 textos)
4. `CriptoServiceTest#testRsaEncrypt_ComChaveInvalida_DeveLancarException`
5. `CriptoServiceTest#testRsaDecrypt_ComChaveInvalida_DeveLancarException`
6. `CriptoServiceTest#testGenerateRSAKeyPar_GeraParDeChaves`
7. `CriptoServiceTest#testGenerateRSAKeyPar_GeraChavesDiferentes`
8. `CriptoServiceTest#testRsaEncryptDecrypt_ChavesDiferentesNaoFuncionam`
9. `CriptoServiceIntegrationTest#testIntegracaoRSA_EncriptarEDescriptografar`
10. `CriptoServiceIntegrationTest#testIntegracaoCompleta_MultiplasCriptografias` (3 endereços)

#### RF20: Cifra de César (6 testes)
1. `CriptoServiceTest#testCesarEncrypt_ComNomeSimples`
2. `CriptoServiceTest#testCesarEncrypt_ComNomeComplexo`
3. `CriptoServiceTest#testCesarEncrypt_ComCaracteresEspeciais`
4. `CriptoServiceTest#testCesarDecrypt_DecriptaCesarEncrypt`
5. `CriptoServiceTest#testCesarEncryptDecrypt_ComDiferentesChaves` (25 chaves)
6. `CriptoServiceIntegrationTest#testIntegracaoCesar_EncriptarEDescriptografar`

### Testes End-to-End (UC08)

**DatabaseSystemTest** valida o fluxo completo em 8 testes ordenados:

1. `test01_FluxoCompleto_VerificarUsuario` - Verifica usuário existe
2. `test02_FluxoCompleto_CriarContatos` - Cria 2 contatos
3. `test03_FluxoCompleto_ListarContatos` - Lista todos os contatos
4. `test04_FluxoCompleto_BuscarContato` - Busca por número
5. `test05_FluxoCompleto_AtualizarContato` - Atualiza nome e número
6. `test06_FluxoCompleto_DeletarContato` - Deleta um contato
7. `test07_FluxoCompleto_VerificarIntegridadeReferencial` - Valida relacionamento User-Contato
8. `test08_FluxoCompleto_TesteDePerformance` - Cria 50 contatos < 5s

---

## Glossário

| Termo | Definição |
|-------|-----------|
| **BCrypt** | Algoritmo de hash criptográfico usado para armazenar senhas de forma segura |
| **Base64** | Esquema de codificação que representa dados binários em formato ASCII |
| **Cifra de César** | Técnica de criptografia por substituição onde cada letra é deslocada um número fixo de posições no alfabeto |
| **ContatoDAO** | Data Access Object com dados de contato descriptografados |
| **ContatoDTO** | Data Transfer Object para criação/atualização de contato |
| **E2E** | End-to-End, criptografia onde apenas remetente e destinatário podem ler |
| **JWT** | JSON Web Token, padrão para criação de tokens de acesso |
| **Par de Chaves RSA** | Conjunto de chave pública e privada geradas pelo algoritmo RSA |
| **PKCS1Padding** | Esquema de padding para criptografia RSA (RFC 2313) |
| **Role** | Papel do usuário no sistema (USER ou ADMIN) |
| **RSA** | Algoritmo de criptografia assimétrica (Rivest-Shamir-Adleman) |
| **Tag** | Identificador de busca gerado com Cifra de César |
| **UserDTO** | Data Transfer Object para resposta de usuário (sem senha) |

---

## Histórico de Versões

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2025-01-15 | Equipe de Desenvolvimento | Versão inicial do documento de requisitos |

---

## Aprovações

| Stakeholder | Papel | Data | Assinatura |
|-------------|-------|------|------------|
| - | Product Owner | - | - |
| - | Tech Lead | - | - |
| - | QA Lead | - | - |

---

## Referências

1. [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
2. [RFC 2313 - PKCS #1: RSA Encryption](https://www.rfc-editor.org/rfc/rfc2313)
3. [RFC 7519 - JSON Web Token (JWT)](https://www.rfc-editor.org/rfc/rfc7519)
4. [BCrypt Algorithm](https://en.wikipedia.org/wiki/Bcrypt)
5. Guia de Testes - `guia-testes.md`
