package ag.selm.manager.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthClientHttpRequestInterceptorTest {

    @Mock
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2AuthorizedClient authorizedClient;

    @Mock
    private OAuth2AccessToken accessToken;

    private OAuthClientHttpRequestInterceptor interceptor;

    private MockClientHttpRequest request;
    private ClientHttpRequestExecution execution;

    @BeforeEach
    void setUp() {
        interceptor = new OAuthClientHttpRequestInterceptor(authorizedClientManager, "test-registration-id");

        request = new MockClientHttpRequest(HttpMethod.GET, "/test");
        execution = mock(ClientHttpRequestExecution.class);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void intercept_whenAuthorizationHeaderNotPresent_shouldAddAuthorizationHeader() throws IOException {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class))).thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("test-token");

        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(execution.execute(eq(request), any(byte[].class))).thenReturn(response);

        // when
        ClientHttpResponse actualResponse = interceptor.intercept(request, new byte[0], execution);

        // then
        verify(authorizedClientManager).authorize(any(OAuth2AuthorizeRequest.class));
        verify(authorizedClient).getAccessToken();
        verify(accessToken).getTokenValue();
        verify(execution).execute(eq(request), any(byte[].class));

        assertEquals("Bearer test-token", request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals(response, actualResponse);
    }

    @Test
    void intercept_whenAuthorizationHeaderPresent_shouldNotAddAuthorizationHeader() throws IOException {
        // given
        request.getHeaders().setBearerAuth("existing-token");

        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(execution.execute(eq(request), any(byte[].class))).thenReturn(response);

        // when
        ClientHttpResponse actualResponse = interceptor.intercept(request, new byte[0], execution);

        // then
        verify(authorizedClientManager, never()).authorize(any(OAuth2AuthorizeRequest.class));
        verify(execution).execute(eq(request), any(byte[].class));

        assertEquals("Bearer existing-token", request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals(response, actualResponse);
    }
}