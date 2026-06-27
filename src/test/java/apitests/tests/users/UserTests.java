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
     * INÍCIO DE BLOCO DE TESTES POSITIVOS
     */

    @Test
    @Owner("Geovane")
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
    @Owner("Geovane")
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
    @Owner("Geovane")
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

        userClient.getUserById(userIdToCleanUp)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/users/get-user-by-id-schema.json"))
                .body("_id", equalTo(userIdToCleanUp));
    }

    @Test
    @Owner("Geovane")
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
    @Owner("Geovane")
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

        // Como esse teste já valida a exclusão, não precisa delegar ao @AfterEach
        userClient.deleteUser(userIdToDelete, "")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/users/delete-user-schema.json"))
                .body("message", equalTo("Registro excluído com sucesso"));
    }

    /**
     * INÍCIO DE BLOCO DE TESTES NEGATIVOS
     */

    @Test
    @Owner("Geovane")
    @Story("Tentar cadastrar usuário com email em branco")
    @Severity(SeverityLevel.MINOR)
    @Description("Valida criação de usuário comum com email em branco e verifica o JSON Schema da resposta")
    public void shouldReturn400WhenEmailIsBlank() {
        User user = DataFactory.generateRegularUserWithoutEmail();

        userClient.createUser(user)
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/users/email-is-blank-schema.json"))
                .body("email", equalTo("email não pode ficar em branco"));
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar cadastrar usuário com senha em branco")
    @Severity(SeverityLevel.MINOR)
    @Description("Valida criação de usuário comum com senha em branco e verifica o JSON Schema da resposta")
    public void shouldReturn400WhenPasswordIsBlank() {
        User user = DataFactory.generateRegularUserWithoutPassword();

        userClient.createUser(user)
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/users/password-is-blank-schema.json"))
                .body("password", equalTo("password não pode ficar em branco"));
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar cadastrar usuário com email e senha em branco")
    @Severity(SeverityLevel.MINOR)
    @Description("Valida criação de usuário comum com email e senha em branco e verifica o JSON Schema da resposta")
    public void shouldReturn400WhenEmailAndPasswordAreBlank() {
        User user = DataFactory.generateRegularUserWithoutEmailPassword();

        userClient.createUser(user)
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/users/email-password-are-blank-schema.json"))
                .body("email", equalTo("email não pode ficar em branco"))
                .body("password", equalTo("password não pode ficar em branco"));
    }

    @Test
    @Owner("Geovane")
    @Story("Editar usuário inexistente")
    @Severity(SeverityLevel.NORMAL)
    @Description("Valida edição de dados de usuário inexistente com dados dinâmicos e verifica o JSON Schema da resposta")
    public void shouldUpdateNonExistentUser() {
        User user = DataFactory.generateRegularUser();
        String nonExistentId = DataFactory.generateInvalidUserId();

        var response = userClient.updateUser(nonExistentId, user, "")
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/users/create-user-schema.json"))
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .extract().response();

        userIdToCleanUp = response.jsonPath().getString("_id");
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar pesquisar usuário por Id inexistente")
    @Severity(SeverityLevel.MINOR)
    @Description("Valida pesquisa de dados de usuário comum por ID inexistente e verifica o JSON Schema da resposta")
    public void shouldReturn400WithInvalidUserId() {
        String invalidUserId = DataFactory.generateInvalidUserId();

        userClient.getUserById(invalidUserId)
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/users/get-user-by-non-existent-id-schema.json"))
                .body("message", equalTo("Usuário não encontrado"));
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar criar usuário comum com email de usuário já existente")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valida criação de usuário comum com email já existente na base e verifica o JSON Schema da resposta")
    public void shouldReturn400WhenEmailAlreadyExists() {
        User user = DataFactory.generateRegularUser();

        // 1. Cria o usuário inicial
        var response = userClient.createUser(user)
                .then()
                .statusCode(201)
                .extract().response();

        userIdToCleanUp = response.jsonPath().getString("_id");
        assertNotNull(userIdToCleanUp);

        // 2. Tenta criar um segundo usuário com o mesmo e-mail
        User duplicateUser = DataFactory.generateUserWithEmail(user.getEmail());
        userClient.createUser(duplicateUser)
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/users/email-already-exists-schema.json"))
                .body("message", equalTo("Este email já está sendo usado"));
    }

    @Test
    @Owner("Gustavo")
    @Story("Não deletar usuário inexistente")
    @Severity(SeverityLevel.MINOR)
    @Description("Valida a não exclusão de dados do usuário inexistente e verifica o JSON Schema da resposta")
    public void shouldNotDeleteUser() {
        String nonExistentId = DataFactory.generateInvalidUserId();

        userClient.deleteUser(nonExistentId, "")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/users/not-delete-user-schema.json"))
                .body("message", equalTo("Nenhum registro excluído"));
    }

    /**
     * INÍCIO DE BLOCO DE TESTES "CAMINHO FELIZ"
     */

    @Test
    @Owner("Geovane")
    @Story("Executar fluxo feliz do ciclo de vida do usuário")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valida a completa execução do ciclo de vida do usuário: criação, busca, edição, exclusão e confirmação de exclusão")
    public void shouldExecuteFullUserLifecycle() {
        User user = DataFactory.generateRegularUser();

        String userId = createUser(user);
        returnUserById(userId);
        updateUser(userId);
        deleteUser(userId);
        confirmDeletion(userId);
    }

    @Step("Criar usuário")
    private String createUser(User user) {
        return userClient.createUser(user)
                .then().statusCode(201)
                .extract().jsonPath().getString("_id");
    }

    @Step("Buscar usuário por ID")
    private void returnUserById(String userId) {
        userClient.getUserById(userId)
                .then().statusCode(200)
                .body("_id", equalTo(userId));
    }

    @Step("Editar usuário")
    private void updateUser(String userId) {
        User updatedUser = DataFactory.generateRegularUser();
        userClient.updateUser(userId, updatedUser, "")
                .then().statusCode(200);
    }

    @Step("Deletar usuário")
    private void deleteUser(String userId) {
        userClient.deleteUser(userId, "")
                .then().statusCode(200);
    }

    @Step("Confirmar exclusão do usuário")
    private void confirmDeletion(String userId) {
        userClient.getUserById(userId)
                .then().statusCode(400)
                .body("message", equalTo("Usuário não encontrado"));
    }
}