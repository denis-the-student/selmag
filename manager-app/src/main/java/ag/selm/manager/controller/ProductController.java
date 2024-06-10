package ag.selm.manager.controller;

import ag.selm.manager.client.exception.ClientBadRequestException;
import ag.selm.manager.client.ProductsClient;
import ag.selm.manager.controller.payload.UpdateProductPayload;
import ag.selm.manager.entity.Product;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
@RequestMapping("catalogue/products/{productId:\\d+}")
public class ProductController {

    private final ProductsClient productsClient;
    private final MessageSource messageSource;

    @ModelAttribute(value = "product", binding = false)
    public Product loadProduct(@PathVariable("productId") int productId) {
        return this.productsClient.findProduct(productId)
            .orElseThrow(() -> new NoSuchElementException("catalogue.errors.product.not_found"));
    }

    @GetMapping
    public String getProductPage() {
        return "catalogue/products/product";
    }

    @GetMapping("edit")
    public String getProductEditPage() {
        return "catalogue/products/edit";
    }

    @PostMapping("edit")
    public String updateProduct(@PathVariable("productId") int productId,
                                UpdateProductPayload payload, Model model) {
        try {
            this.productsClient.updateProduct(productId, payload.title(), payload.details());
            return "redirect:/catalogue/products/%d".formatted(productId);
        } catch (ClientBadRequestException exception) {
            model.addAttribute("payload", payload);
            model.addAttribute("errors", exception.getErrors());
            return "catalogue/products/edit";
        }
    }

    @PostMapping("delete")
    public String deleteProduct(@PathVariable("productId") int productId) {
        this.productsClient.deleteProduct(productId);
        return "redirect:/catalogue/products/list";
    }

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNoSuchElementException(NoSuchElementException exception, Model model,
                                               HttpServletResponse response, Locale locale) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        model.addAttribute("error",
            this.messageSource.getMessage(exception.getMessage(), new Object[0],
                exception.getMessage(), locale));
        return "errors/404";
    }
}
