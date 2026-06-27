package apitests.tests.users;

import apitests.clients.UserClient;
import apitests.models.User;
import apitests.utils.DataFactory;
import io.qameta.allure.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Epic("Usuários")
@Feature("CRUD de Usuários")
public class UserTests {

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
     * INÍCIO DE BLOCO DOS TESTES
     * TESTE DE CRUD
     */

    @Test
    @Story("Criar usuário administrador")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valida criação de admin com dados dinâmicos e verifica o JSON Schema da resposta")
    public void shouldCreateAdminUserSuccessfully() {
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
    }

    @Test
    @Story("Criar usuário comum")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valida criação de usuário comum com dados dinâmicos e verifica o JSON Schema da resposta")
    public void shouldCreateRegularUserSuccessfully() {
        User user = DataFactory.generateRegularUser();

        var response = userClient.createUser(user)
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/users/create-user-schema.json"))
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .body("_id", notNullValue())
                .extract().response();

        userIdToCleanUp = response.jsonPath().getString("_id");
        assertNotNull(userIdToCleanUp);
    }

    @Test
    @Story("Pesquisar usuário pelo Id")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Valida pesquisa de dados de usuário comum por ID e verifica o JSON Schema da resposta")
    public void shouldGetUserById() {
        User user = DataFactory.generateRegularUser();

        var response = userClient.createUser(user)
                .then()
                .statusCode(201)
                .extract().response();

        userIdToCleanUp = response.jsonPath().getString("_id");
        assertNotNull(userIdToCleanUp);

        userClient.getUserById(userIdToCleanUp)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/users/get-user-by-id-schema.json"))
                .body("_id", equalTo(userIdToCleanUp));
    }

    @Test
    @Story("Atualizar usuário")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Valida edição de dados do usuário comum com dados dinâmicos e verifica o JSON Schema da resposta")
    public void shouldUpdateUserSuccessfully() {
        User initialUser = DataFactory.generateRegularUser();
        User updatedUser = DataFactory.generateRegularUser();

        // 1. Cria o usuário inicial para atualizar
        var response = userClient.createUser(initialUser)
                .then()
                .statusCode(201)
                .extract().response();

        userIdToCleanUp = response.jsonPath().getString("_id");
        assertNotNull(userIdToCleanUp);

        // 2. Edita o usuário
        userClient.updateUser(userIdToCleanUp, updatedUser, "")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/users/update-user-schema.json"))
                .body("message", equalTo("Registro alterado com sucesso"));
    }

    @Test
    @Story("Deletar usuário")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Valida exclusão de dados do usuário comum com dados dinâmicos e verifica o JSON Schema da resposta")
    public void shouldDeleteUserSuccessfully() {
        User user = DataFactory.generateRegularUser();

        var response = userClient.createUser(user)
                .then()
                .statusCode(201)
                .extract().response();

        String userIdToDelete = response.jsonPath().getString("_id");
        assertNotNull(userIdToDelete);

        // Como esse teste já valida a exclusão com sucesso, não precisa delegar ao @AfterEach
        userClient.deleteUser(userIdToDelete, "")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/users/delete-user-schema.json"))
                .body("message", equalTo("Registro excluído com sucesso"));
    }
}