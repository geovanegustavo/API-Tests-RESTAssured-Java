package apitests.clients;

import apitests.config.ApiConfig;
import apitests.models.Product;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/**
 * Client responsável pelos endpoints de produtos.
 */
public class ProductClient {

    private static final String PRODUCTS_PATH = "/produtos";

    public Response createProduct(Product product, String token) {
        return given()
                .spec(ApiConfig.getAuthRequestSpec(token))
                .body(product)
                .when()
                .post(PRODUCTS_PATH);
    }

    public Response getAllProducts() {
        return given()
                .spec(ApiConfig.getRequestSpec())
                .when()
                .get(PRODUCTS_PATH);
    }

    public Response getProductById(String id) {
        return given()
                .spec(ApiConfig.getRequestSpec())
                .when()
                .get(PRODUCTS_PATH + "/" + id);
    }

    public Response updateProduct(String id, Product product, String token) {
        return given()
                .spec(ApiConfig.getAuthRequestSpec(token))
                .body(product)
                .when()
                .put(PRODUCTS_PATH + "/" + id);
    }

    public Response deleteProduct(String id, String token) {
        return given()
                .spec(ApiConfig.getAuthRequestSpec(token))
                .when()
                .delete(PRODUCTS_PATH + "/" + id);
    }
}
