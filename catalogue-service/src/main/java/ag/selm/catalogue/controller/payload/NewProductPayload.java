package ag.selm.catalogue.controller.payload;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NewProductPayload(
    @Size(min = 3, max = 50, message = "{catalogue.products.create.errors.title_size_is_invalid}")
    @Pattern(regexp = "^(?!\\s).*", message = "{catalogue.products.create.errors.title_starts_with_space}")
    String title,

    @Size(max = 1000, message = "{catalogue.products.create.errors.details_size_is_invalid}")
    String details) {

}
