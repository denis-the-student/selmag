package ag.selm.manager.client.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ClientBadRequestException extends RuntimeException{

    private final List<String> errors;

    public ClientBadRequestException(List<String> errors) {
        this.errors = errors;
    }
}
