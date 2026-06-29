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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Epic("Produtos")
@Feature("Gestão de Produtos")
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

    @Test
    @Owner("Geovane")
    @Story("Pesquisar todos os produtos")
    @Severity(SeverityLevel.NORMAL)
    @Description("Valida pesquisa de dados de todos os produtos e verifica o JSON Schema da resposta")
    public void shouldGetAllProducts() {
        var response = productClient.getAllProducts()
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/products/get-all-products-schema.json"))
                .extract()
                .response();

        int quantidade = response.jsonPath().getInt("quantidade");
        int produtosSize = response.jsonPath().getList("produtos").size();

        assertThat(quantidade, equalTo(produtosSize));
    }

    @Test
    @Owner("Geovane")
    @Story("Pesquisar produto pelo Id")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Valida pesquisa de dados de produto por ID e verifica o JSON Schema da resposta")
    public void shouldGetProductById() {
        Product product = DataFactory.generateProduct();

        var response = productClient.createProduct(product, token)
                .then()
                .statusCode(201)
                .extract().response();

        productIdToCleanUp = response.jsonPath().getString("_id");
        assertNotNull(productIdToCleanUp);

        productClient.getProductById(productIdToCleanUp)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/products/get-product-by-id-schema.json"))
                .body("_id", equalTo(productIdToCleanUp));
    }

    @Test
    @Owner("Geovane")
    @Story("Pesquisar produto pelo Nome")
    @Severity(SeverityLevel.NORMAL)
    @Description("Valida pesquisa de dados de produto por nome e verifica o JSON Schema da resposta")
    public void shouldGetProductByName() {
        Product product = DataFactory.generateProduct();

        var response = productClient.createProduct(product, token)
                .then()
                .statusCode(201)
                .extract().response();

        productIdToCleanUp = response.jsonPath().getString("_id");
        assertNotNull(productIdToCleanUp);

        String productName = product.getNome();

        productClient.getProductByName(productName)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/products/get-all-products-schema.json"))
                .body("quantidade", equalTo(1))
                .body("produtos[0].nome", equalTo(productName))
                .body("produtos[0]._id", equalTo(productIdToCleanUp));
    }

    @Test
    @Owner("Geovane")
    @Story("Atualizar produto")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Valida edição de dados do produto com dados dinâmicos e verifica o JSON Schema da resposta")
    public void shouldUpdateProductSuccessfully() {
        Product initialProduct = DataFactory.generateProduct();
        Product updatedProduct = DataFactory.generateProduct();

        // 1. Cria o produto inicial para atualizar
        var response = productClient.createProduct(initialProduct, token)
                .then()
                .statusCode(201)
                .extract().response();

        productIdToCleanUp = response.jsonPath().getString("_id");
        assertNotNull(productIdToCleanUp);

        // 2. Edita o produto
        productClient.updateProduct(productIdToCleanUp, updatedProduct, token)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/products/update-product-schema.json"))
                .body("message", equalTo("Registro alterado com sucesso"));
    }

    @Test
    @Owner("Geovane")
    @Story("Deletar produto")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Valida exclusão de dados do produto com dados dinâmicos e verifica o JSON Schema da resposta")
    public void shouldDeleteProductSuccessfully() {
        Product product = DataFactory.generateProduct();

        var response = productClient.createProduct(product, token)
                .then()
                .statusCode(201)
                .extract().response();

        String productIdToDelete = response.jsonPath().getString("_id");
        assertNotNull(productIdToDelete);

        // Como esse teste já valida a exclusão, não precisa delegar ao @AfterEach
        productClient.deleteProduct(productIdToDelete, token)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/products/delete-product-schema.json"))
                .body("message", equalTo("Registro excluído com sucesso"));
    }

    /**
     * INÍCIO DE BLOCO DE TESTES NEGATIVOS
     */

    @Test
    @Owner("Geovane")
    @Story("Tentar criar produto com preco inválido")
    @Severity(SeverityLevel.MINOR)
    @Description("Valida que a API rejeita criação de produto com preço negativo e verifica o JSON Schema da resposta")
    public void shouldReturn400WhenPriceIsInvalid() {
        Product product = DataFactory.generateProduct();
        product.setPreco(-200);

        productClient.createProduct(product, token)
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/products/invalid-price-schema.json"))
                .body("preco", equalTo("preco deve ser um número positivo"));
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar criar produto com preco zero")
    @Severity(SeverityLevel.MINOR)
    @Description("Valida que a API rejeita criação de produto com preço zero e verifica o JSON Schema da resposta")
    public void shouldReturn400WhenPriceZero() {
        Product product = DataFactory.generateProduct();
        product.setPreco(0);

        productClient.createProduct(product, token)
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/products/invalid-price-schema.json"))
                .body("preco", equalTo("preco deve ser um número positivo"));
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar criar produto com quantidade inválida")
    @Severity(SeverityLevel.MINOR)
    @Description("Valida que a API rejeita criação de produto com quantidade negativa e verifica o JSON Schema da resposta")
    public void shouldReturn400WhenQuantityIsInvalid() {
        Product product = DataFactory.generateProduct();
        product.setQuantidade(-10);

        productClient.createProduct(product, token)
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/products/invalid-quantity-schema.json"))
                .body("quantidade", equalTo("quantidade deve ser maior ou igual a 0"));
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar atualizar produto sem token")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valida que a API rejeita atualização de produto sem token de autenticação e verifica o JSON Schema da resposta")
    public void shouldReturn401WhenTokenIsMissingOnUpdateProduct() {
        Product initialProduct = DataFactory.generateProduct();
        Product updatedProduct = DataFactory.generateProduct();

        // 1. Cria o produto inicial para atualizar
        var response = productClient.createProduct(initialProduct, token)
                .then()
                .statusCode(201)
                .extract().response();

        productIdToCleanUp = response.jsonPath().getString("_id");
        assertNotNull(productIdToCleanUp);

        // 2. Edita o produto
        productClient.updateProduct(productIdToCleanUp, updatedProduct, "")
                .then()
                .statusCode(401)
                .body(matchesJsonSchemaInClasspath("schemas/products/unauthorized-schema.json"))
                .body("message", equalTo("Token de acesso ausente, inválido, expirado ou usuário do token não existe mais"));
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar deletar produto sem token")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valida que a API rejeita exclusão de produto sem token de autenticação e verifica o JSON Schema da resposta")
    public void shouldReturn401WhenTokenIsMissingOnDeleteProduct() {
        Product product = DataFactory.generateProduct();

        var response = productClient.createProduct(product, token)
                .then()
                .statusCode(201)
                .extract().response();

        productIdToCleanUp = response.jsonPath().getString("_id");
        assertNotNull(productIdToCleanUp);

        productClient.deleteProduct(productIdToCleanUp, "")
                .then()
                .statusCode(401)
                .body(matchesJsonSchemaInClasspath("schemas/products/unauthorized-schema.json"))
                .body("message", equalTo("Token de acesso ausente, inválido, expirado ou usuário do token não existe mais"));
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar criar produto com nome já existente")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valida que a API rejeita criação de produto com nome duplicado e verifica o JSON Schema da resposta")
    public void shouldReturn400WhenProductAlreadyExists() {
        Product product = DataFactory.generateProduct();

        // 1. Cria o produto inicial
        var response = productClient.createProduct(product, token)
                .then()
                .statusCode(201)
                .extract().response();

        productIdToCleanUp = response.jsonPath().getString("_id");
        assertNotNull(productIdToCleanUp);

        // 2. Tenta criar produto com o mesmo nome
        productClient.createProduct(product, token)
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/products/product-already-exists-schema.json"))
                .body("message", equalTo("Já existe produto com esse nome"));
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar criar produto sem token")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valida criação de produto sem token e verifica o JSON Schema da resposta")
    public void shouldReturn401WhenTokenIsMissingOnCreateProduct() {
        Product product = DataFactory.generateProduct();

        productClient.createProduct(product, "")
                .then()
                .statusCode(401)
                .body(matchesJsonSchemaInClasspath("schemas/products/unauthorized-schema.json"))
                .body("message", equalTo("Token de acesso ausente, inválido, expirado ou usuário do token não existe mais"));
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar criar produto com usuário comum")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valida que usuário comum não pode criar produto e verifica o JSON Schema da resposta")
    public void shouldReturn403WhenRegularUserCreatesProduct() {
        // 1. Cria um usuário comum
        User regularUser = DataFactory.generateRegularUser();

        var userResponse = userClient.createUser(regularUser)
                .then()
                .statusCode(201)
                .extract().response();

        String regularUserId = userResponse.jsonPath().getString("_id");
        assertNotNull(regularUserId);

        // 2. Obtém token do usuário comum
        String regularUserToken = authClient.getToken(regularUser.getEmail(), regularUser.getPassword());

        // 3. Tenta criar produto com token de usuário comum
        Product product = DataFactory.generateProduct();

        try {
            productClient.createProduct(product, regularUserToken)
                    .then()
                    .statusCode(403)
                    .body(matchesJsonSchemaInClasspath("schemas/products/forbidden-schema.json"))
                    .body("message", equalTo("Rota exclusiva para administradores"));
        } finally {
            // 4. Garante limpeza do usuário comum independente do resultado
            userClient.deleteUser(regularUserId, token);
        }
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar pesquisar produto por Id inexistente")
    @Severity(SeverityLevel.MINOR)
    @Description("Valida pesquisa de produto por ID inexistente e verifica o JSON Schema da resposta")
    public void shouldReturn400WhenGettingProductByNonExistentId() {
        String nonExistentId = DataFactory.generateInvalidId();

        productClient.getProductById(nonExistentId)
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/products/product-not-found-schema.json"))
                .body("message", equalTo("Produto não encontrado"));
    }

    @Test
    @Owner("Geovane")
    @Story("Editar produto inexistente")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Valida edição de dados de produto inexistente e verifica o JSON Schema da resposta")
    public void shouldUpdateNonExistentProduct() {
        Product product = DataFactory.generateProduct();
        String nonExistentId = DataFactory.generateInvalidId();

        var response = productClient.updateProduct(nonExistentId, product, token)
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/products/create-product-schema.json"))
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .extract().response();

        productIdToCleanUp = response.jsonPath().getString("_id");
        assertNotNull(productIdToCleanUp);
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar deletar produto inexistente")
    @Severity(SeverityLevel.MINOR)
    @Description("Valida tentativa de exclusão de produto com ID inexistente e verifica o JSON Schema da resposta")
    public void shouldReturn200WhenDeletingNonExistentProduct() {
        String nonExistentProductId = DataFactory.generateInvalidId();

        productClient.deleteProduct(nonExistentProductId, token)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/products/delete-non-existent-product-schema.json"))
                .body("message", equalTo("Nenhum registro excluído"));
    }

    /**
     * INÍCIO DE BLOCO DE TESTES E2E
     */

    @Test
    @Owner("Geovane")
    @Story("Executar fluxo feliz do ciclo de vida do produto")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valida a completa execução do ciclo de vida do produto: criação, busca, edição, exclusão e confirmação de exclusão")
    public void shouldExecuteFullProductLifecycle() {
        Product product = DataFactory.generateProduct();

        String productId = createProduct(product);
        getProductById(productId);
        updateProduct(productId);
        deleteProduct(productId);
        confirmDeletion(productId);
    }

    @Step("Criar produto")
    private String createProduct(Product product) {
        return productClient.createProduct(product, token)
                .then().statusCode(201)
                .extract().jsonPath().getString("_id");
    }

    @Step("Buscar produto por ID")
    private void getProductById(String productId) {
        productClient.getProductById(productId)
                .then().statusCode(200)
                .body("_id", equalTo(productId));
    }

    @Step("Editar produto")
    private void updateProduct(String productId) {
        Product updateProduct = DataFactory.generateProduct();
        productClient.updateProduct(productId, updateProduct, token)
                .then().statusCode(200);
    }

    @Step("Deletar produto")
    private void deleteProduct(String productId) {
        productClient.deleteProduct(productId, token)
                .then().statusCode(200);
    }

    @Step("Confirmar exclusão do produto")
    private void confirmDeletion(String productId) {
        productClient.getProductById(productId)
                .then().statusCode(400)
                .body("message", equalTo("Produto não encontrado"));
    }
}
