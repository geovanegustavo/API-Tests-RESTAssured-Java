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

}
