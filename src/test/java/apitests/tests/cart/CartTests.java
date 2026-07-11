package apitests.tests.cart;

import apitests.clients.AuthClient;
import apitests.clients.CartClient;
import apitests.clients.ProductClient;
import apitests.clients.UserClient;
import apitests.models.Cart;
import apitests.models.CartItem;
import apitests.models.Product;
import apitests.models.User;
import apitests.utils.DataFactory;
import io.qameta.allure.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Epic("Carrinho")
@Feature("Gestão de Carrinho")
public class CartTests {

    private final CartClient cartClient = new CartClient();
    private final ProductClient productClient = new ProductClient();
    private final AuthClient authClient = new AuthClient();
    private final UserClient userClient = new UserClient();

    private String userIdToCleanUp;
    private String productIdToCleanUp;
    private String token;

    @BeforeEach
    public void setUp() {
        // 1. Cria usuário admin
        User user = DataFactory.generateAdminUser();
        var userResponse = userClient.createUser(user)
                .then().statusCode(201).extract().response();

        userIdToCleanUp = userResponse.jsonPath().getString("_id");

        // 2. Obtém token
        token = authClient.getToken(user.getEmail(), user.getPassword());

        // 3. Cria produto para usar nos testes de carrinho
        Product product = DataFactory.generateProduct();
        var productResponse = productClient.createProduct(product, token)
                .then().statusCode(201).extract().response();

        productIdToCleanUp = productResponse.jsonPath().getString("_id");
    }

    @AfterEach
    public void tearDown() {
        // Cancela carrinho se ainda existir
        try {
            cartClient.cancelPurchase(token);
        } catch (Exception e) {
            System.err.println("No cart to clean up: " + e.getMessage());
        }

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
    @Story("Criar carrinho")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valida criação de carrinho com produto existente e verifica o JSON Schema da resposta")
    public void shouldCreateCartSuccessfully() {
        Cart cart = new Cart(List.of(new CartItem(productIdToCleanUp, 1)));

        cartClient.createCart(cart, token)
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/carts/create-cart-schema.json"))
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .body("_id", notNullValue());
    }

    @Test
    @Owner("Geovane")
    @Story("Listar carrinhos")
    @Severity(SeverityLevel.NORMAL)
    @Description("Valida pesquisa de dados de todos os carrinhos e verifica o JSON Schema da resposta")
    public void shouldGetAllCarts() {
        Cart cart = new Cart(List.of(new CartItem(productIdToCleanUp, 1)));

        cartClient.createCart(cart, token)
                .then()
                .statusCode(201)
                .body("_id", notNullValue());

        var response = cartClient.getAllCarts()
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/carts/get-all-carts-schema.json"))
                .extract()
                .response();

        int quantidade = response.jsonPath().getInt("quantidade");
        int carrinhosSize = response.jsonPath().getList("carrinhos").size();

        assertThat(quantidade, equalTo(carrinhosSize));
    }

    @Test
    @Owner("Geovane")
    @Story("Pesquisar carrinho pelo Id")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Valida pesquisa de dados de carrinho por ID e verifica o JSON Schema da resposta")
    public void shouldGetCartById() {
        Cart cart = new Cart(List.of(new CartItem(productIdToCleanUp, 1)));

        var response = cartClient.createCart(cart, token)
                .then()
                .statusCode(201)
                .body("_id", notNullValue())
                .extract().response();

        String cartId = response.jsonPath().getString("_id");
        assertNotNull(cartId);

        cartClient.getCartById(cartId)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/carts/get-cart-by-id-schema.json"))
                .body("_id", equalTo(cartId));
    }

    @Test
    @Owner("Geovane")
    @Story("Concluir compra")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valida conclusão de compra e verifica o JSON Schema da resposta")
    public void shouldConcludePurchaseSuccessfully() {
        Cart cart = new Cart(List.of(new CartItem(productIdToCleanUp, 1)));

        cartClient.createCart(cart, token)
                .then()
                .statusCode(201);

        cartClient.concludePurchase(token)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/carts/conclude-cart-schema.json"))
                .body("message", equalTo("Registro excluído com sucesso"));
    }

    @Test
    @Owner("Geovane")
    @Story("Cancelar compra")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Valida cancelamento de compra e verifica o JSON Schema da resposta")
    public void shouldCancelPurchaseSuccessfully() {
        Cart cart = new Cart(List.of(new CartItem(productIdToCleanUp, 1)));

        cartClient.createCart(cart, token)
                .then()
                .statusCode(201);

        cartClient.cancelPurchase(token)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/carts/cancel-cart-schema.json"))
                .body("message", equalTo("Registro excluído com sucesso. Estoque dos produtos reabastecido"));
    }

    /**
     * INÍCIO DE BLOCO DE TESTES NEGATIVOS
     */

    @Test
    @Owner("Geovane")
    @Story("Tentar pesquisar carrinho por Id inexistente")
    @Severity(SeverityLevel.MINOR)
    @Description("Valida pesquisa de carrinho por ID inexistente e verifica o JSON Schema da resposta")
    public void shouldReturn400WhenGetCartByNonExistentId() {
        String cartId = DataFactory.generateInvalidId();

        cartClient.getCartById(cartId)
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/carts/cart-not-found-schema.json"))
                .body("message", equalTo("Carrinho não encontrado"));
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar criar segundo carrinho")
    @Severity(SeverityLevel.NORMAL)
    @Description("Valida que usuário não pode ter mais de um carrinho ativo")
    public void shouldReturn400WhenCartAlreadyExists() {
        Cart cart = new Cart(List.of(new CartItem(productIdToCleanUp, 1)));

        cartClient.createCart(cart, token)
                .then()
                .statusCode(201);

        cartClient.createCart(cart, token)
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/carts/cart-already-exists-schema.json"))
                .body("message", equalTo("Não é permitido ter mais de 1 carrinho"));
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar criar carrinho sem token")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valida que a API rejeita criação de carrinho sem token e verifica o JSON Schema da resposta")
    public void shouldReturn401WhenCreatingCartWithoutToken() {
        Cart cart = new Cart(List.of(new CartItem(productIdToCleanUp, 1)));

        cartClient.createCart(cart, "")
                .then()
                .statusCode(401)
                .body(matchesJsonSchemaInClasspath("schemas/carts/unauthorized-cart-schema.json"))
                .body("message", equalTo("Token de acesso ausente, inválido, expirado ou usuário do token não existe mais"));
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar criar carrinho com produto inexistente")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valida tentativa de criar carrinho com produto inexistente e verifica erro 400")
    public void shouldReturn400WhenCreatingCartWithNonExistentProduct() {
        String productId = DataFactory.generateInvalidId();

        Cart cart = new Cart(List.of(new CartItem(productId, 1)));

        cartClient.createCart(cart, token)
                .then()
                .statusCode(400)
                //.body(matchesJsonSchemaInClasspath("schemas/carts/create-cart-schema.json"))
                .body("message", equalTo("Produto não encontrado"));
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar concluir compra sem token")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valida tentativa de conclusão de compra sem token e verifica erro 401")
    public void shouldReturn401WhenConcludingPurchaseWithoutToken() {
        Cart cart = new Cart(List.of(new CartItem(productIdToCleanUp, 1)));

        cartClient.createCart(cart, token)
                .then()
                .statusCode(201);

        cartClient.concludePurchase("")
                .then()
                .statusCode(401)
                .body(matchesJsonSchemaInClasspath("schemas/carts/unauthorized-cart-schema.json"))
                .body("message", equalTo("Token de acesso ausente, inválido, expirado ou usuário do token não existe mais"));
    }

    @Test
    @Owner("Geovane")
    @Story("Tentar cancelar compra sem token")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Valida tentativa de cancelamento de compra sem token e verifica erro 401")
    public void shouldReturn401WhenCancelingPurchaseWithoutToken() {
        Cart cart = new Cart(List.of(new CartItem(productIdToCleanUp, 1)));

        cartClient.createCart(cart, token)
                .then()
                .statusCode(201);

        cartClient.cancelPurchase("")
                .then()
                .statusCode(401)
                .body(matchesJsonSchemaInClasspath("schemas/carts/unauthorized-cart-schema.json"))
                .body("message", equalTo("Token de acesso ausente, inválido, expirado ou usuário do token não existe mais"));
    }

}
