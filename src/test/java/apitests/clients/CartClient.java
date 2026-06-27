package apitests.clients;

import apitests.config.ApiConfig;
import apitests.models.Cart;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class CartClient {

    private static final String CARTS_PATH = "/carrinhos";

    public Response createCart(Cart cart, String token) {
        return given()
                .spec(ApiConfig.getAuthRequestSpec(token))
                .body(cart)
                .when()
                .post(CARTS_PATH);
    }

    public Response getAllCarts() {
        return given()
                .spec(ApiConfig.getRequestSpec())
                .when()
                .get(CARTS_PATH);
    }

    public Response getCartById(String id) {
        return given()
                .spec(ApiConfig.getRequestSpec())
                .when()
                .get(CARTS_PATH + "/" + id);
    }

    public Response concludePurchase(String token) {
        return given()
                .spec(ApiConfig.getAuthRequestSpec(token))
                .when()
                .delete(CARTS_PATH + "/concluir-compra");
    }

    public Response cancelPurchase(String token) {
        return given()
                .spec(ApiConfig.getAuthRequestSpec(token))
                .when()
                .delete(CARTS_PATH + "/cancelar-compra");
    }
}