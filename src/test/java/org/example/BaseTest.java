package org.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.github.javafaker.Faker;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class BaseTest {

    static final Faker faker = new Faker();

    protected static RequestSpecification requestSpec = new RequestSpecBuilder()
            .setBaseUri("https://jsonplaceholder.typicode.com")
            .setContentType(ContentType.JSON)
            .addFilter(new AllureRestAssured())
            .build();

    // mapper
    static {
        // Указываем RestAssured использовать нашу кастомную фабрику для ObjectMapper
         RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((cls, charset) -> {
                    ObjectMapper mapper = new ObjectMapper();

                    // Тут мы включаем все нужные нам "фишки"
                    mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
                    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                    mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
                    return mapper;
                })
        );
    }
}
