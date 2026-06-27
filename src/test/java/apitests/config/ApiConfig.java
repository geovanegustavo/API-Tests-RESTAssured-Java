package apitests.config;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Configuração central do RestAssured.
 * Define base URI, headers padrão e filtros de log/Allure.
 */
public class ApiConfig {

    private static final String BASE_URL = System.getProperty(
            "base.url",
            EnvConfig.get("BASE_URL", "https://serverest.dev")
    );

    private static RequestSpecification requestSpec;

    public static RequestSpecification getRequestSpec() {
        if (requestSpec == null) {
            requestSpec = new RequestSpecBuilder()
                    .setBaseUri(BASE_URL)
                    .setContentType(ContentType.JSON)
                    .setAccept(ContentType.JSON)
                    .addFilter(new AllureRestAssured())
                    .addFilter(new RequestLoggingFilter())
                    .addFilter(new ResponseLoggingFilter())
                    .build();
        }
        return requestSpec;
    }

    public static RequestSpecification getAuthRequestSpec(String token) {
        // O token da ServeRest já vem com o prefixo "Bearer "
        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;
        return new RequestSpecBuilder()
                .addRequestSpecification(getRequestSpec())
                .addHeader("Authorization", authHeader)
                .build();
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }
}
