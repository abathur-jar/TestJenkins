package org.example;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.example.dto.UserPogo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SoftAssertionsExtension.class)
@Epic("Управление пользователями")
public class UserApiTest extends BaseTest{

    @InjectSoftAssertions
    private SoftAssertions softly;

    @Step("Подготовка данных пользователя")
    private UserPogo prepareUser() {
        return UserPogo.builder()
                .name(faker.name().fullName())
                .email(faker.internet().emailAddress())
                .username(faker.name().username())
                .build();
    }

    @Test
    @Feature("Создание пользователя")
    @DisplayName("Успешное создание пользователя с валидными данными")
    void shouldCreateUserSuccessfully() {

        final UserPogo expectedUser = prepareUser();

        UserPogo actualUser = RestAssured
                    .given()
                .spec(requestSpec)
                .body(expectedUser)
                    .when()
                .post("/users")
                    .then()
                .statusCode(201)
                .extract().as(UserPogo.class);

        softly.assertThat(actualUser.getId())
                .as("ID пользователя")
                .isNotNull()
                .isGreaterThan(0);

        softly.assertThat(actualUser)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expectedUser);
    }

    @Test
    @Feature("Получение списка пользователей")
    @DisplayName("Проверка списка пользователей через JsonPath + AssertJ")
    void shouldVerifyUserList() {
        final List<Map<String, Object>> users = RestAssured
                .given()
                .spec(requestSpec)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".");

        softly.assertThat(users)
                .as("Количество пользователей в списке")
                .hasSize(10);

        softly.assertThat(users)
                .as("Проверка заполнения email у всех")
                .extracting(user -> user.get("email"))
                .allSatisfy(email -> {
                    assertThat(email).isNotNull();
                    assertThat(email.toString()).contains("@");
                });
    }

    @Test
    @Feature("Поиск пользователей")
    @DisplayName("Поиск пользователя по город и проверка его данных")
    void shouldFindUserByCityAndVerifyData() {
        final List<Map<String, Object>> users = RestAssured
                .given()
                    .spec(requestSpec)
                .when()
                    .get("/users")
                .then()
                    .statusCode(200)
                    .extract()
                    .jsonPath()
                    .getList(".");

        final Map<String, Object> targetUser = users.stream()
                .filter(user -> {
                    Map<String, Object> address = (Map<String, Object>) user.get("address");
                    return address.get("city").equals("Howemouth");
                })
                .findFirst()
                .orElseThrow(() -> new AssertionError("Пользователя из Howemouth нет!"));

        softly.assertThat(targetUser.get("name"))
                .as("Имя пользователя")
                .isEqualTo("Kurtis Weissnat");

        softly.assertThat(targetUser.get("id"))
                .as("ID пользователя")
                .isEqualTo(7);
    }

    @Test
    @Feature("Поиск пользователей")
    @DisplayName("Поиск пользователя с нужным id")
    void shouldFindUserById() {
        JsonPath json = RestAssured
                .given()
                    .spec(requestSpec)
                .when()
                    .get("/users/5")
                .then()
                    .statusCode(200)
                    .log().all()
                    .extract()
                    .jsonPath();

        softly.assertThat(json.getString("username"))
                .as("Username пользователя")
                .hasSizeBetween(3, 20)
                .isEqualTo("Kamren");

        softly.assertThat(json.getString("email"))
                .as("Проверка email")
                .doesNotStartWith("\\d");

        softly.assertThat(json.getString("company.name"))
                .as("Компания пользователя")
                .contains("Keebler");

        softly.assertThat(json.getString("address.zipcode"))
                .as("Zipcode пользователя")
                .hasSize(5);

        softly.assertThat(json.getString("address.street"))
                        .as("Проверка улицы")
                                .containsIgnoringCase("Walks");

        softly.assertThat(json.getString("address.geo.lat"))
                .as("Широта")
                        .startsWith("-");

        softly.assertThat(json.getString("website"))
                .as("Website")
                        .isIn("demarco.info", "ola.org", "steve.biz");
    }
}
