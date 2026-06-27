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
        // Criação do usuário
        User user = DataFactory.generateAdminUser();

        String userEmail = user.getEmail();
        String userPassword = user.getPassword();

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
        authClient.login(userEmail, userPassword)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/auth/login-schema.json"))
                .body("message", equalTo("Login realizado com sucesso"))
                .body("authorization", startsWith("Bearer "))
                .body("authorization", notNullValue());

        String token = authClient.getToken(userEmail, userPassword);

        Assertions.assertNotNull(token);
        Assertions.assertTrue(token.startsWith("Bearer "), "Token deve iniciar com 'Bearer '");
    }

    // FIM -----------
}
