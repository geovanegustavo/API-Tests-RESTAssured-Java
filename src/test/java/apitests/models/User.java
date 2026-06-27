package apitests.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Modelo de Usuário — espelha o contrato da API ServeRest.
 */

// se a resposta da API vier com campos desconhecidos, ignore-os silenciosamente.
@JsonIgnoreProperties(ignoreUnknown = true)

// ao converter esse objeto para JSON, omita qualquer campo que esteja null.
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    private String nome;
    private String email;
    private String password;
    private String administrador;
    private String _id;

    // O construtor vazio é exigido pelo Jackson para conseguir desserializar JSON em objeto.
    // ele precisa instanciar a classe antes de popular os campos.
    public User() {}

    // O construtor com parâmetros é o usado no DataFactory para criar usuários nos testes.
    public User(String nome, String email, String password, String administrador) {
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.administrador = administrador;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAdministrador() { return administrador; }
    public void setAdministrador(String administrador) { this.administrador = administrador; }

    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }

    @Override
    public String toString() {
        return "User{nome='" + nome + "', email='" + email + "', administrador='" + administrador + "'}";
    }
}
