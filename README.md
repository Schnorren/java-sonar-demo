# Java SonarQube Security Demo

Projeto Spring Boot com análise de segurança via **SonarCloud** e **OWASP Dependency Check**, integrado ao GitHub Actions.

---

## Tecnologias

- **Java 17** + Spring Boot 3.2
- **Spring Security** com JWT (JJWT 0.12)
- **BCrypt** (cost 12) para hash de senhas
- **JaCoCo** para cobertura de código
- **SonarCloud** para análise de qualidade e segurança
- **OWASP Dependency Check** para vulnerabilidades em dependências
- **GitHub Actions** para CI/CD automático

---

## Configuração do SonarCloud

### 1. Criar conta no SonarCloud

Acesse [sonarcloud.io](https://sonarcloud.io) e faça login com sua conta GitHub.

### 2. Criar organização e projeto

1. Clique em **+** > **Analyze new project**
2. Importe seu repositório GitHub
3. Anote o **Project Key** e o **Organization Key**

### 3. Gerar token do SonarCloud

1. Vá em **My Account** > **Security** > **Generate Tokens**
2. Crie um token com nome `SONAR_TOKEN`
3. Copie o valor gerado

### 4. Configurar secrets no GitHub

No repositório GitHub: **Settings** > **Secrets and variables** > **Actions** > **New repository secret**

| Secret | Valor |
|--------|-------|
| `SONAR_TOKEN` | Token gerado no SonarCloud |
| `SONAR_PROJECT_KEY` | Chave do projeto no SonarCloud |
| `SONAR_ORGANIZATION` | Chave da organização no SonarCloud |
| `APP_JWT_SECRET` | Senha segura com 32+ caracteres |

### 5. Atualizar `sonar-project.properties`

```properties
sonar.projectKey=SEU_PROJECT_KEY
sonar.organization=SUA_ORGANIZACAO
```

---

## Como Executar Localmente

### Pré-requisitos
- Java 17+
- Maven 3.8+

### Rodar a aplicação

```bash
# Exportar variável de ambiente (obrigatório)
export APP_JWT_SECRET="sua-chave-secreta-com-32-caracteres!!"

# Compilar e executar
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`

### Rodar os testes com cobertura

```bash
./mvnw clean verify
# Relatório JaCoCo em: target/site/jacoco/index.html
```

### Rodar análise SonarQube localmente

```bash
./mvnw clean verify sonar:sonar \
  -Dsonar.token=SEU_TOKEN \
  -Dsonar.projectKey=SEU_PROJECT_KEY \
  -Dsonar.organization=SUA_ORGANIZACAO
```

### Rodar OWASP Dependency Check

```bash
./mvnw org.owasp:dependency-check-maven:check
# Relatório em: target/dependency-check-report.html
```

---

## Endpoints da API

### Autenticação (público)

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "joao",
  "email": "joao@example.com",
  "password": "minhasenha123"
}
```

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "joao",
  "password": "minhasenha123"
}
```

### Usuários (autenticado)

```http
GET    /api/users        # ADMIN apenas
GET    /api/users/{id}   # ADMIN ou o próprio usuário
PUT    /api/users/{id}   # ADMIN apenas
DELETE /api/users/{id}   # ADMIN apenas

Authorization: Bearer <token>
```

---

## Práticas de Segurança Implementadas

| Prática | Implementação |
|---------|--------------|
| Hash de senha | BCrypt com cost factor 12 |
| Autenticação | JWT stateless com expiração |
| Autorização | Spring Security + `@PreAuthorize` |
| Headers HTTP | CSP, X-Frame-Options, Referrer-Policy |
| Validação | Bean Validation em todos os inputs |
| Erros genéricos | Sem exposição de stack traces |
| Soft delete | Usuários não são removidos do banco |
| Secrets | Via variável de ambiente, nunca no código |

---

## Estrutura do Projeto

```
src/
├── main/java/com/demo/
│   ├── config/          # SecurityConfig, JwtAuthFilter
│   ├── controller/      # AuthController, UserController
│   ├── exception/       # Handlers globais
│   ├── model/           # Entidades JPA
│   ├── repository/      # Spring Data JPA
│   └── service/         # Lógica de negócio, JwtService
└── test/java/com/demo/
    ├── controller/      # Testes de integração
    └── service/         # Testes unitários
```

---

## CI/CD com GitHub Actions

O pipeline `.github/workflows/ci.yml` executa automaticamente em cada push/PR:

1. **Build & Test** — compila e executa todos os testes
2. **JaCoCo** — gera relatório de cobertura (mínimo 70%)
3. **SonarCloud** — analisa qualidade e vulnerabilidades
4. **OWASP** — verifica CVEs nas dependências (falha se CVSS ≥ 7)

---

## Licença

MIT
