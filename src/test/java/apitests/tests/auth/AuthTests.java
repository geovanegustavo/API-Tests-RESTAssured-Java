package apitests.tests.auth;

import apitests.clients.AuthClient;
import apitests.clients.UserClient;
import apitests.models.User;
import apitests.utils.DataFactory;
import io.qameta.allure.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Login")
@Feature("Autenticação e Token")
public class AuthTests {

    private final AuthClient authClient = new AuthClient();
    private final UserClient userClient = new UserClient();

    private String userIdToCleanUp;

    @AfterEach
    public void tearDown() {
        if (userIdToCleanUp != null) {
            try {
                userClient.deleteUser(userIdToCleanUp, "");
            } catch (Exception e) {
                System.err.println("Failed to clean up test user: " + e.getMessage());
            } finally {
                userIdToCleanUp = null; // Reseta para o próximo teste da suíte
            }
        }
    }

    /**
     * INÍCIO DE BLOCO DE TESTES POSITIVOS
     */

    @Test
    @Owner("Geovane")
    @Story("Login com credenciais válidas")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Deve realizar login com sucesso e retornar token Bearer")
    public void shouldLoginWithValidCredentials() {
        // Criação do usuário
        User user = DataFactory.generateAdminUser();

        var response = userClient.createUser(user)
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/users/create-user-schema.json"))
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .body("_id", notNullValue())
                .extract().response();

        userIdToCleanUp = response.jsonPath().getString("_id");
        assertNotNull(userIdToCleanUp);

        // Autenticação do usuário
        authClient.login(user.getEmail(), user.getPassword())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/auth/login-schema.json"))
                .body("message", equalTo("Login realizado com sucesso"))
                .body("authorization", startsWith("Bearer "))
                .body("authorization", notNullValue());
    }

    @Test
    @Owner("Gustavo")
    @Story("Token deve ser válido")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Token retornado deve ser utilizável em endpoints protegidos")
    public void tokenShouldBeValid() {
        User user = DataFactory.generateAdminUser();

        var response = userClient.createUser(user)
                .then()
                .statusCode(201)
                .extract().response();

        userIdToCleanUp = response.jsonPath().getString("_id");
        assertNotNull(userIdToCleanUp);

        // Login único — extrai o token diretamente
        String token = authClient.getToken(user.getEmail(), user.getPassword());

        assertNotNull(token);
        assertTrue(token.startsWith("Bearer "), "Token deve iniciar com 'Bearer '");
    }

    /**
     * INÍCIO DE BLOCO DE TESTES NEGATIVOS
     */

    @Test
    @Story("Tentar login com email inválido")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Deve retornar 401 ao tentar login com email inválido")
    public void shouldReturn401WithInvalidEmail() {
        String userEmail = DataFactory.generateEmail();
        authClient.login(userEmail, "Test@12345")
                .then()
                .statusCode(401)
                .body(matchesJsonSchemaInClasspath("schemas/auth/unauthorized-schema.json"))
                .body("message", equalTo("Email e/ou senha inválidos"));
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar login com senha inválida")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Deve retornar 401 ao tentar login com senha incorreta")
    public void shouldReturn401WithInvalidPassword() {
        // 1. Cria o usuário para garantir que o email existe na base
        User user = DataFactory.generateAdminUser();

        var response = userClient.createUser(user)
                .then()
                .statusCode(201)
                .extract().response();

        userIdToCleanUp = response.jsonPath().getString("_id");
        assertNotNull(userIdToCleanUp);

        // 2. Tenta login com o email correto mas senha errada
        authClient.login(user.getEmail(), "SenhaErrada@999")
                .then()
                .statusCode(401)
                .body(matchesJsonSchemaInClasspath("schemas/auth/unauthorized-schema.json"))
                .body("message", equalTo("Email e/ou senha inválidos"));
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar login com email em branco")
    @Severity(SeverityLevel.MINOR)
    @Description("Deve retornar 400 ao tentar login com email em branco")
    public void shouldReturn400WhenEmailIsBlank() {
        authClient.login("", "Test@12345")
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/auth/email-is-blank-schema.json"))
                .body("email", equalTo("email não pode ficar em branco"));
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar login com senha em branco")
    @Severity(SeverityLevel.MINOR)
    @Description("Deve retornar 400 ao tentar login com senha em branco")
    public void shouldReturn400WhenPasswordIsBlank() {
        String userEmail = DataFactory.generateEmail();

        authClient.login(userEmail, "")
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/auth/password-is-blank-schema.json"))
                .body("password", equalTo("password não pode ficar em branco"));
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar login com email desformatado")
    @Severity(SeverityLevel.MINOR)
    @Description("Deve retornar 400 ao tentar login com email inválido")
    public void shouldReturn400WithInvalidEmail() {
        authClient.login("email-errado", "Test@12345")
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/auth/invalid-email-schema.json"))
                .body("email", equalTo("email deve ser um email válido"));
    }

}
