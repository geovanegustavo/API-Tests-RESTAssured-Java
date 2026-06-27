package apitests.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Modelo de Login e resposta de autenticação.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthToken {

    private String message;
    private String authorization;

    public AuthToken() {}

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAuthorization() { return authorization; }
    public void setAuthorization(String authorization) { this.authorization = authorization; }
}
