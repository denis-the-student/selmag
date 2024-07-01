package ag.selm.catalogue.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class ProductsRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    @Sql(scripts = "/sql/products.sql", config = @SqlConfig(encoding = "UTF-8"))
    void findProducts_ReturnsProductsList() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders
                .get("/catalogue-api/products")
                .param("filter", "товар")
                .with(jwt().jwt(builder -> builder.claim("scope", "view_catalogue")));

        // when
        this.mockMvc.perform(requestBuilder)
                //then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        [
                            {"id":  1, "title":  "Товар №1", "details": "Описание товара №1"},
                            {"id":  3, "title":  "Товар №3", "details": "Описание товара №3"}
                        ]
                        """));
    }

    @Test
    @Sql(scripts = "/sql/products.sql", config = @SqlConfig(encoding = "UTF-8"))
    void findProducts_ByIds_ReturnsProductsList() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders
                .get("/catalogue-api/products")
                .param("ids", "1,2")
                .with(jwt().jwt(builder -> builder.claim("scope", "view_catalogue")));

        // when
        this.mockMvc.perform(requestBuilder)
                //then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        [
                            {"id":  1, "title":  "Товар №1", "details": "Описание товара №1"},
                            {"id":  2, "title":  "Шоколадка", "details": "Очень вкусная шоколадка"}
                        ]
                        """));
    }

    @Test
    @Sql(scripts = "/sql/products.sql", config = @SqlConfig(encoding = "UTF-8"))
    void findProducts_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders
                .get("/catalogue-api/products")
                .param("filter", "товар")
                .with(jwt());

        // when
        this.mockMvc.perform(requestBuilder)
                //then
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void createProduct_RequestIsValid_ReturnsNewProduct() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders
                .post("/catalogue-api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title":  "Новый товар",
                            "details": "Описание нового товара"
                        }""")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/catalogue-api/products/1"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                                {
                                    "id":  1,
                                    "title":  "Новый товар",
                                    "details": "Описание нового товара"
                                }"""));
    }

    @Test
    void createProduct_RequestIsInValid_ReturnsBadRequest() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders
                .post("/catalogue-api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title":  "  ",
                            "details": "Описание нового товара"
                        }""")
                .locale(new Locale("ru", "RU"))
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(content().json("""
                        {
                            "errors": [
                                "Название товара должно быть от 3 до 50 символов",
                                "Название товара не может начинаться с пробела"
                            ]
                        }
                        """));
    }

    @Test
    void createProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders
                .post("/catalogue-api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title":  "Новый товар",
                            "details": "Описание нового товара"
                        }""")
                .locale(new Locale("ru", "RU"))
                .with(jwt().jwt(builder -> builder.claim("scope", "view_catalogue")));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}