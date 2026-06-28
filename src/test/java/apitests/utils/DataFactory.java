package apitests.utils;

import apitests.models.Product;
import apitests.models.User;
import com.github.javafaker.Faker;
import java.util.Locale;

public class DataFactory {

    private static final Faker faker = new Faker(new Locale("pt-BR"));

    public static User generateAdminUser() {
        return new User(
                faker.name().fullName(),
                faker.internet().emailAddress(),
                "Test@12345",
                "true"
        );
    }

    public static User generateRegularUser() {
        return new User(
                faker.name().fullName(),
                faker.internet().emailAddress(),
                "Test@12345",
                "false"
        );
    }

    public static User generateRegularUserWithoutEmail() {
        return new User(
                faker.name().fullName(),
                "",
                "Test@12345",
                "false"
        );
    }

    public static User generateRegularUserWithoutPassword() {
        return new User(
                faker.name().fullName(),
                faker.internet().emailAddress(),
                "",
                "false"
        );
    }

    public static User generateRegularUserWithoutEmailPassword() {
        return new User(
                faker.name().fullName(),
                "",
                "",
                "false"
        );
    }

    public static User generateUserWithEmail(String email) {
        return new User(
                faker.name().fullName(),
                email,
                "Test@12345",
                "false"
        );
    }

    public static Product generateProduct() {
        return new Product(
                faker.commerce().productName() + " " + faker.numerify("###"),
                faker.number().numberBetween(10, 5000),
                faker.lorem().sentence(),
                faker.number().numberBetween(1, 500)
        );
    }

    public static String generateEmail() {
        return faker.internet().emailAddress();
    }

    public static String generateName() {
        return faker.name().fullName();
    }

    public static String generateInvalidId() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder id = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            id.append(characters.charAt(faker.number().numberBetween(0, characters.length())));
        }
        return id.toString();
    }

}
