package ag.selm.catalogue.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    @Sql(scripts = "/sql/products.sql", config = @SqlConfig(encoding = "UTF-8"))
    void findProduct_ReturnsProduct() throws Exception {
        // given
        var requestBuilder = get("/catalogue-api/products/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "view_catalogue")));

        // when
        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                            "id": 1,
                            "title": "Товар №1",
                            "details": "Описание товара №1"
                        }"""));
    }

    @Test
    @Sql(scripts = "/sql/products.sql", config = @SqlConfig(encoding = "UTF-8"))
    void findProduct_ProductDoesNotExist_ReturnsNotFound() throws Exception {
        // given
        var requestBuilder = get("/catalogue-api/products/999")
                .with(jwt().jwt(builder -> builder.claim("scope", "view_catalogue")));

        // when
        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = "/sql/products.sql", config = @SqlConfig(encoding = "UTF-8"))
    void findProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = get("/catalogue-api/products/1")
                .with(jwt());

        // when
        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Sql(scripts = "/sql/products.sql", config = @SqlConfig(encoding = "UTF-8"))
    void updateProduct_ReturnsNoContent() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.patch("/catalogue-api/products/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Updated title",
                            "details": "Updated details"
                        }""");

        // when
        mockMvc.perform(requestBuilder)
        // then
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @Sql(scripts = "/sql/products.sql", config = @SqlConfig(encoding = "UTF-8"))
    void updateProduct_RequestIsInvalid_ReturnsBadRequest() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.patch("/catalogue-api/products/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "  ",
                            "details": "Updated details"
                        }""")
                .locale(Locale.ENGLISH);

        // when
        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(content().json("""
                        {
                            "errors": ["Product title must be between 3 and 50 characters","Product title can not start with space"]
                        }"""));
    }

    @Test
    @Sql(scripts = "/sql/products.sql", config = @SqlConfig(encoding = "UTF-8"))
    void updateProduct_ProductDoesNotExist_ReturnsNotFound() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.patch("/catalogue-api/products/999")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Updated title",
                            "details": "Updated details"
                        }""");

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = "/sql/products.sql", config = @SqlConfig(encoding = "UTF-8"))
    void updateProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.patch("/catalogue-api/products/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "view_catalogue")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Updated title",
                            "details": "Updated details"
                        }""");

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Sql(scripts = "/sql/products.sql", config = @SqlConfig(encoding = "UTF-8"))
    void deleteProduct_ReturnsNoContent() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.delete("/catalogue-api/products/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @Sql(scripts = "/sql/products.sql", config = @SqlConfig(encoding = "UTF-8"))
    void deleteProduct_ProductDoesNotExist_ReturnsNotFound() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.patch("/catalogue-api/products/999")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = "/sql/products.sql", config = @SqlConfig(encoding = "UTF-8"))
    void deleteProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.patch("/catalogue-api/products/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "view_catalogue")));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
