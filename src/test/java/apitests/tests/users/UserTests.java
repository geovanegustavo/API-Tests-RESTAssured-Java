package apitests.tests;

import apitests.clients.AuthClient;
import apitests.clients.UserClient;
import apitests.models.User;
import apitests.utils.DataFactory;
import org.junit.jupiter.api.BeforeAll;

public class UserTests {

    private final UserClient userClient = new UserClient();
    private final AuthClient authClient = new AuthClient();

    private static String createdUserId;
    private static String createdUserEmail;
    private static String adminToken;

    @BeforeAll
    static void setup() {
        UserClient client = new UserClient();
        AuthClient auth = new AuthClient();

        User admin = DataFactory.generateAdminUser();
        createdUserEmail = admin.getEmail();

        client.createUser(admin).then().statusCode(201);
        adminToken = auth.getToken(admin.getEmail(), admin.getPassword());
    }

}
