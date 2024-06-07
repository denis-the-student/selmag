package ag.selm.manager.client.exception.util;

import ag.selm.manager.client.exception.ClientBadRequestException;
import org.springframework.http.ProblemDetail;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;

public class ErrorHandlingUtils {

    public static ClientBadRequestException mapHttpClientErrorExceptionBadRequestToClientBadRequestException(
            HttpClientErrorException.BadRequest exception) {

        ProblemDetail problemDetail = exception.getResponseBodyAs(ProblemDetail.class);
        if (problemDetail != null && problemDetail.getProperties() != null) {
            Object errors = problemDetail.getProperties().get("errors");
            if (errors instanceof List<?> errorsList) {
                if (errorsList.isEmpty() || errorsList.get(0) instanceof String) {
                    @SuppressWarnings("unchecked")
                    List<String> errorMessages = (List<String>) errorsList;
                    return new ClientBadRequestException(errorMessages);
                }
            }
        }
        return new ClientBadRequestException(Collections.emptyList());
    }
}
