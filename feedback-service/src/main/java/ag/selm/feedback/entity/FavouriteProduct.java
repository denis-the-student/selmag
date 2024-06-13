package ag.selm.feedback.entity;

import org.springframework.data.annotation.Id;

import java.util.UUID;

public record FavouriteProduct(@Id UUID id, Integer productId, String userId) {
}