package apitests.tests.products;

import apitests.clients.AuthClient;
import apitests.clients.ProductClient;
import apitests.clients.UserClient;
import apitests.models.Product;
import apitests.models.User;
import apitests.utils.DataFactory;
import io.qameta.allure.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Epic("Produtos")
@Feature("CRUD de Produtos")
public class ProductTests {

    private final ProductClient productClient = new ProductClient();
    private final AuthClient authClient = new AuthClient();
    private final UserClient userClient = new UserClient();

    private String productIdToCleanUp;
    private String userIdToCleanUp;
    private String token;

    @BeforeEach
    public void setUp() {
        // 1. Cria o usuário admin
        User user = DataFactory.generateAdminUser();

        var response = userClient.createUser(user)
                .then()
                .statusCode(201)
                .extract().response();

        userIdToCleanUp = response.jsonPath().getString("_id");

        // 2. Faz login e salva o token para todos os testes da classe
        token = authClient.getToken(user.getEmail(), user.getPassword());
    }

    @AfterEach
    public void tearDown() {
        if (productIdToCleanUp != null) {
            try {
                productClient.deleteProduct(productIdToCleanUp, token);
            } catch (Exception e) {
                System.err.println("Failed to clean up product: " + e.getMessage());
            } finally {
                productIdToCleanUp = null;
            }
        }

        if (userIdToCleanUp != null) {
            try {
                userClient.deleteUser(userIdToCleanUp, token);
            } catch (Exception e) {
                System.err.println("Failed to clean up user: " + e.getMessage());
            } finally {
                userIdToCleanUp = null;
            }
        }
    }

    /**
     * INÍCIO DE BLOCO DE TESTES POSITIVOS
     */

    @Test
    @Owner("Geovane")
    @Story("Criar produto")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valida criação de produto com dados dinâmicos e verifica o JSON Schema da resposta")
    public void shouldCreateProductSuccessfully() {
        Product product = DataFactory.generateProduct();

        var response = productClient.createProduct(product, token)
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/products/create-product-schema.json"))
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .body("_id", notNullValue())
                .extract().response();

        productIdToCleanUp = response.jsonPath().getString("_id");
        assertNotNull(productIdToCleanUp);
    }
}
