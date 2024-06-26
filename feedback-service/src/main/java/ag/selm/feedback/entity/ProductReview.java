package ag.selm.feedback.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReview {

    @Id
    private UUID id;
    private Integer productId;
    private Integer rating;
    private String review;
    private String userId;
}
