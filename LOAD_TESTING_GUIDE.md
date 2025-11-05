# Guia de Testes de Carga - Agenda API

Este guia explica como executar testes de carga na API de Agenda usando JMeter e monitorar os resultados usando Prometheus e Grafana.

## Sumário

1. [Pré-requisitos](#pré-requisitos)
2. [Configuração do Ambiente](#configuração-do-ambiente)
3. [Executando os Testes de Carga](#executando-os-testes-de-carga)
4. [Monitoramento com Grafana](#monitoramento-com-grafana)
5. [Como Validar os Resultados](#como-validar-os-resultados)
6. [Troubleshooting](#troubleshooting)

---

## Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- **Java 17+** (para rodar a aplicação Spring Boot)
- **Maven** (para buildar o projeto)
- **Apache JMeter 5.6+** ([Download](https://jmeter.apache.org/download_jmeter.cgi))
- **Docker e Docker Compose** (para Prometheus e Grafana)

---

## Configuração do Ambiente

### 1. Compilar e Executar a Aplicação

```bash
# Compilar o projeto
mvn clean install

# Executar a aplicação
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

**Endpoints importantes:**
- API: `http://localhost:8080/agenda`
- Métricas Prometheus: `http://localhost:8080/actuator/prometheus`
- Health Check: `http://localhost:8080/actuator/health`

### 2. Iniciar Prometheus e Grafana

```bash
# Iniciar os containers
docker-compose up -d

# Verificar se estão rodando
docker-compose ps
```

**URLs de acesso:**
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (usuário: `admin`, senha: `admin`)

### 3. Verificar Configuração

Acesse o Prometheus (`http://localhost:9090`) e verifique se o target "agenda-api" está com status "UP":
- Vá em: Status → Targets
- Procure por `agenda-api` e verifique se está verde (UP)

---

## Executando os Testes de Carga

### Opção 1: Modo GUI (Recomendado para desenvolvimento)

1. **Abrir o JMeter:**
   ```bash
   # Windows
   jmeter.bat

   # Linux/Mac
   ./jmeter.sh
   ```

2. **Carregar o plano de teste:**
   - File → Open
   - Navegue até: `jmeter/AgendaLoadTest.jmx`

3. **Revisar os cenários de teste:**
   - **Cenário 1 - Criar Contatos**: 10 threads, 5 loops (50 requisições POST)
   - **Cenário 2 - Buscar Contatos**: 20 threads, 10 loops (200 requisições GET)
   - **Cenário 3 - Atualizar Contatos**: 5 threads, 3 loops (15 requisições PUT)
   - **Cenário 4 - Deletar Contatos**: 5 threads, 2 loops (10 requisições DELETE)

4. **Executar o teste:**
   - Clique no botão verde (▶️) ou pressione `Ctrl+R`

5. **Visualizar resultados em tempo real:**
   - Abra o "View Results Tree" para ver requisições individuais
   - Abra o "Summary Report" para métricas agregadas
   - Abra o "Graph Results" para gráficos de desempenho

### Opção 2: Modo CLI (Recomendado para produção)

```bash
# Executar teste em modo non-GUI
jmeter -n -t jmeter/AgendaLoadTest.jmx -l results/test-results.jtl -e -o results/html-report

# Parâmetros:
# -n: modo non-GUI
# -t: arquivo de teste
# -l: arquivo de log de resultados
# -e: gerar relatório HTML
# -o: diretório de saída do relatório
```

Após a execução, abra o relatório HTML:
```bash
# Windows
start results/html-report/index.html

# Linux/Mac
open results/html-report/index.html
```

### Personalizar Parâmetros do Teste

Você pode modificar variáveis do teste diretamente na linha de comando:

```bash
jmeter -n -t jmeter/AgendaLoadTest.jmx \
  -JHOST=localhost \
  -JPORT=8080 \
  -l results/custom-test.jtl
```

---

## Monitoramento com Grafana

### 1. Acessar o Grafana

1. Abra: `http://localhost:3000`
2. Login: `admin` / `admin`
3. (Opcional) Altere a senha quando solicitado

### 2. Acessar o Dashboard

O dashboard "Agenda API - Performance Dashboard" já está pré-configurado:
- No menu lateral, clique em "Dashboards" → "Browse"
- Selecione "Agenda API - Performance Dashboard"

### 3. Métricas Disponíveis no Dashboard

O dashboard exibe 7 painéis principais:

1. **Taxa de Requisições (req/s)**
   - Requisições por segundo agrupadas por método, URI e status
   - Útil para ver o throughput da aplicação

2. **Latência P95 (ms)**
   - Gauge mostrando o percentil 95 de latência
   - Verde (<100ms), Amarelo (100-500ms), Vermelho (>500ms)

3. **Latência por Endpoint (P50, P90, P99)**
   - Compara percentis de latência entre diferentes endpoints
   - Ajuda a identificar endpoints lentos

4. **Status HTTP das Requisições**
   - Total de respostas 2xx (sucesso), 4xx (cliente) e 5xx (servidor)
   - Importante para identificar taxa de erros

5. **Uso de Memória JVM**
   - Heap e non-heap memory usage
   - Identifica possíveis memory leaks

6. **Uso de CPU**
   - CPU da aplicação e CPU do sistema
   - Mostra se a aplicação está CPU-bound

7. **GC (Garbage Collection)**
   - Tempo gasto em coleta de lixo
   - Pode indicar problemas de memória

### 4. Executar Teste e Observar Métricas

1. **Antes do teste:**
   - Verifique que as métricas estão sendo coletadas (gráficos não vazios)

2. **Durante o teste:**
   - Execute o JMeter conforme seção anterior
   - Observe em tempo real as métricas no Grafana
   - O dashboard atualiza a cada 5 segundos

3. **Depois do teste:**
   - Analise os picos de latência
   - Verifique se houve erros HTTP (4xx/5xx)
   - Observe o comportamento da memória e CPU

---

## Como Validar os Resultados

### ✅ Critérios de Sucesso

Um teste de carga é considerado bem-sucedido quando:

#### 1. Taxa de Erro < 1%
```
Taxa de Erro = (Requisições com erro / Total de requisições) × 100
```

**Como verificar:**
- **JMeter (GUI):** Summary Report → coluna "Error %"
- **JMeter (HTML Report):** Statistics → Error %
- **Grafana:** Painel "Status HTTP" → compare 2xx vs 4xx/5xx

**Exemplo de sucesso:**
```
Total de requisições: 1000
Erros: 5
Taxa de erro: 0.5% ✅
```

#### 2. Latência P95 < 500ms

**Como verificar:**
- **JMeter (GUI):** Summary Report → coluna "95th pct"
- **JMeter (HTML Report):** Response Times Percentiles → 95th percentile
- **Grafana:** Painel "Latência P95" → deve estar verde

**Exemplo de sucesso:**
```
P50: 45ms
P90: 120ms
P95: 230ms ✅
P99: 450ms
```

#### 3. Throughput Esperado

O throughput (requisições/segundo) deve ser consistente:

**Como verificar:**
- **JMeter (GUI):** Summary Report → coluna "Throughput"
- **Grafana:** Painel "Taxa de Requisições"

**Exemplo:**
```
Cenário 2 (Buscar Contatos):
- 20 threads simultâneas
- Throughput esperado: ~15-25 req/s
- Throughput obtido: 18.5 req/s ✅
```

#### 4. Ausência de Degradação

O desempenho deve ser estável durante todo o teste:

**Como verificar:**
- **Grafana:** Compare início vs final do teste
- A latência não deve aumentar significativamente com o tempo

**Sinais de problema:**
- Latência cresce progressivamente (possível memory leak)
- CPU em 100% constante (possível deadlock)
- Taxa de erro aumenta com o tempo (possível resource exhaustion)

### 📊 Exemplo de Validação Completa

```bash
# 1. Execute o teste
jmeter -n -t jmeter/AgendaLoadTest.jmx -l results/test.jtl -e -o results/html-report

# 2. Verifique o resumo no terminal
# Procure por linhas como:
# summary = 275 in 00:00:25 = 11.0/s Avg: 89 Min: 12 Max: 450 Err: 0 (0.00%)

# 3. Abra o relatório HTML
# Verifique Statistics table:
# - Error %: < 1% ✅
# - 95th pct: < 500ms ✅
# - Throughput: > valor esperado ✅

# 4. Abra o Grafana
# Verifique se:
# - Latência P95 ficou na zona verde
# - Status HTTP mostra predominância de 2xx
# - CPU e memória estáveis
```

### 📋 Checklist de Validação

- [ ] Taxa de erro < 1%
- [ ] Latência P95 < 500ms
- [ ] Latência P99 < 1000ms
- [ ] Throughput atende expectativa
- [ ] Sem erros 5xx (erro do servidor)
- [ ] CPU < 80% em média
- [ ] Memória não cresce indefinidamente
- [ ] Sem outliers extremos (Max < 5000ms)

### ❌ Problemas Comuns e Soluções

| Problema | Possível Causa | Solução |
|----------|---------------|---------|
| Taxa de erro alta (>5%) | Aplicação não está respondendo | Verificar logs, aumentar timeout |
| Latência P95 > 1000ms | Banco de dados lento | Otimizar queries, adicionar índices |
| CPU em 100% | Processamento excessivo | Revisar algoritmos, adicionar cache |
| Memória crescente | Memory leak | Analisar heap dump, corrigir leaks |
| Erros 404 | Endpoints incorretos | Revisar configuração do JMeter |
| Erros de conexão | JMeter sobrecarregado | Reduzir threads ou aumentar ramp-up |

---

## Troubleshooting

### Aplicação não inicia

```bash
# Verificar se a porta 8080 está em uso
netstat -ano | findstr :8080  # Windows
lsof -i :8080                  # Linux/Mac

# Limpar e recompilar
mvn clean install -DskipTests
```

### Prometheus não está coletando métricas

1. **Verificar se o Actuator está expondo métricas:**
   ```bash
   curl http://localhost:8080/actuator/prometheus
   ```
   Deve retornar várias métricas em formato Prometheus.

2. **Verificar configuração do Prometheus:**
   - Edite `monitoring/prometheus/prometheus.yml`
   - Certifique-se de que `host.docker.internal:8080` está correto
   - No Windows/Mac com Docker Desktop, use `host.docker.internal`
   - No Linux, use o IP da máquina host

3. **Reiniciar Prometheus:**
   ```bash
   docker-compose restart prometheus
   ```

### Grafana não exibe dados

1. **Verificar data source:**
   - Configuration → Data Sources → Prometheus
   - Clique em "Test" e verifique se retorna sucesso

2. **Ajustar intervalo de tempo:**
   - No canto superior direito, selecione "Last 15 minutes"
   - Clique em refresh

3. **Verificar se há dados:**
   - Vá para Explore
   - Execute query simples: `up{job="agenda-api"}`
   - Deve retornar valor 1

### JMeter reporta muitos erros

1. **Criar dados antes de buscar:**
   - Execute primeiro o "Cenário 1 - Criar Contatos"
   - Depois execute os outros cenários

2. **Ajustar delays entre cenários:**
   - No JMeter, Thread Group → Thread Properties
   - Aumente o "Startup delay (seconds)"

3. **Reduzir carga:**
   - Diminua o número de threads
   - Aumente o ramp-up time

### Docker Compose falha ao iniciar

```bash
# Verificar logs
docker-compose logs

# Limpar volumes e reiniciar
docker-compose down -v
docker-compose up -d
```

---

## Recursos Adicionais

### Estrutura de Arquivos

```
agenda/
├── jmeter/
│   └── AgendaLoadTest.jmx          # Plano de teste JMeter
├── monitoring/
│   ├── prometheus/
│   │   └── prometheus.yml          # Configuração Prometheus
│   └── grafana/
│       ├── provisioning/
│       │   ├── datasources/        # Data sources automáticos
│       │   └── dashboards/         # Configuração de dashboards
│       └── dashboards/
│           └── agenda-api-dashboard.json  # Dashboard principal
├── docker-compose.yml              # Orquestração dos containers
└── LOAD_TESTING_GUIDE.md          # Este guia
```

### Próximos Passos

1. **Testes de estresse:** Aumente progressivamente o número de threads até encontrar o limite
2. **Testes de duração:** Execute por períodos mais longos (ex: 1 hora) para detectar memory leaks
3. **Testes de pico:** Simule picos súbitos de tráfego
4. **Configurar alertas:** Configure alertas no Prometheus para notificar quando métricas excederem limites

### Referências

- [JMeter Documentation](https://jmeter.apache.org/usermanual/index.html)
- [Prometheus Documentation](https://prometheus.io/docs/introduction/overview/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Metrics](https://micrometer.io/docs)

---

## Contato e Suporte

Para dúvidas ou problemas, consulte:
- Documentação do Spring Boot Actuator
- Comunidade JMeter
- Issues do projeto no GitHub