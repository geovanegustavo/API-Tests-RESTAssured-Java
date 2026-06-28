package apitests.clients;

import apitests.config.ApiConfig;
import apitests.models.User;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/**
 * Client responsável pelos endpoints de usuários.
 */
public class UserClient {

    private static final String USERS_PATH = "/usuarios";

    public Response createUser(User user) {
        return given()
                .spec(ApiConfig.getRequestSpec())
                .body(user)
                .when()
                .post(USERS_PATH);
    }

    public Response getAllUsers() {
        return given()
                .spec(ApiConfig.getRequestSpec())
                .when()
                .get(USERS_PATH);
    }

    public Response getUserById(String id) {
        return given()
                .spec(ApiConfig.getRequestSpec())
                .when()
                .get(USERS_PATH + "/" + id);
    }

    public Response getUserByName(String nome) {
        return given()
                .spec(ApiConfig.getRequestSpec())
                .queryParam("nome", nome)
                .when()
                .get(USERS_PATH);
    }

    public Response updateUser(String id, User user, String token) {
        return given()
                .spec(ApiConfig.getAuthRequestSpec(token))
                .body(user)
                .when()
                .put(USERS_PATH + "/" + id);
    }

    public Response deleteUser(String id, String token) {
        return given()
                .spec(ApiConfig.getAuthRequestSpec(token))
                .when()
                .delete(USERS_PATH + "/" + id);
    }
}
