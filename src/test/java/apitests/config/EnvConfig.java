package apitests.config;

/**
 * Centraliza leitura de variáveis de ambiente e system properties.
 * Permite execução local e em CI/CD sem alteração de código.
 */
public class EnvConfig {

    public static String get(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isEmpty()) {
            value = System.getProperty(key, defaultValue);
        }
        return value;
    }

    public static String getRequired(String key) {
        String value = get(key, null);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("Variável de ambiente obrigatória não encontrada: " + key);
        }
        return value;
    }

    // Banco de dados
    public static String getDbUrl() {
        return get("DB_URL", "jdbc:postgresql://localhost:5432/servetest_db");
    }

    public static String getDbUser() {
        return get("DB_USER", "postgres");
    }

    public static String getDbPassword() {
        return get("DB_PASSWORD", "postgres");
    }
}
