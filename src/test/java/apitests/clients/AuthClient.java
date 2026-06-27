package apitests.clients;

import apitests.config.ApiConfig;
import apitests.models.AuthToken;
import apitests.models.LoginRequest;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/**
 * Client responsável pelos endpoints de autenticação.
 * Abstrai chamadas HTTP da lógica dos testes.
 */
public class AuthClient {

    private static final String LOGIN_PATH = "/login";

    public Response login(String email, String password) {
        LoginRequest body = new LoginRequest(email, password);

        return given()
                .spec(ApiConfig.getRequestSpec())
                .body(body)
                .when()
                .post(LOGIN_PATH);
    }

    /**
     * Realiza login e retorna o token diretamente.
     * Usado como pré-condição em outros testes.
     */
    public String getToken(String email, String password) {
        return login(email, password)
                .then()
                .statusCode(200)
                .extract()
                .as(AuthToken.class)
                .getAuthorization();
    }
}