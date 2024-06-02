package ag.selm.catalogue.controller.payload;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProductPayload(
    @Size(min = 3, max = 50, message = "{catalogue.products.update.errors.title_size_is_invalid}:zalupa")
    @Pattern(regexp = "^(?!\\s).*", message = "{catalogue.products.update.errors.title_starts_with_space}")

    String title,

    @Size(max = 1000, message = "{catalogue.products.update.errors.details_size_is_invalid}")
    String details) {
}
