package com.pw.edu.pl.master.thesis.ai.configuration;

import com.pw.edu.pl.master.thesis.ai.dto.ai.deepseek.DeepSeekChatRequest;
import com.pw.edu.pl.master.thesis.ai.dto.ai.deepseek.DeepSeekChatResponse;
import com.pw.edu.pl.master.thesis.ai.dto.ai.deepseek.DeepSeekProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor @Slf4j
public class DeepSeekClientConfiguration {
    private final RestTemplate restTemplate;
    private final DeepSeekProperties deepSeekProperties;

    public DeepSeekChatResponse chat(DeepSeekChatRequest req) {
        String url = deepSeekProperties.getBaseUrl() + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(deepSeekProperties.getApiKey());

        HttpEntity<DeepSeekChatRequest> request = new HttpEntity<>(req, headers);

        try {
            ResponseEntity<DeepSeekChatResponse> response = restTemplate.exchange( url, HttpMethod.POST,
                    request, DeepSeekChatResponse.class);

            return response.getBody();

        } catch (HttpClientErrorException clientException) {
            log.error(
                    "DeepSeek API client error {}: {}",
                    clientException.getStatusCode(),
                    clientException.getResponseBodyAsString(),
                    clientException
            );
            throw new RuntimeException(
                    "DeepSeek client error: " + clientException.getStatusCode(),
                    clientException
            );

        } catch (ResourceAccessException ioException) {
            log.error("DeepSeek I/O error", ioException);
            throw new RuntimeException(
                    "Timeout calling DeepSeek: " + ioException.getMessage(),
                    ioException
            );

        } catch (RestClientException restException) {
            log.error("Unexpected error calling DeepSeek", restException);
            throw new RuntimeException(
                    "Error calling DeepSeek: " + restException.getMessage(),
                    restException
            );
        }
    }

}
