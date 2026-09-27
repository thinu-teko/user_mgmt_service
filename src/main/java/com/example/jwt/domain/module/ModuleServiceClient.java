package com.example.jwt.domain.module;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ModuleServiceClient {

    private final RestClient restClient;

    public ModuleServiceClient(
        @Value("${module-service.base-url:http://module-service:8080}") String baseUrl) {

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(3));

        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .build();
    }

    @Retry(name = "moduleService")
    @CircuitBreaker(name = "moduleService")
    public void checkModule(UUID moduleId) {
        restClient.get()
            .uri("/api/v1/modules/{moduleId}", moduleId)
            .retrieve()
            .toBodilessEntity();
    }

    @Retry(name = "moduleService")
    @CircuitBreaker(name = "moduleService")
    public void assignModule(UUID userId, UUID moduleId) {
        restClient.put()
            .uri("/api/v1/users/{userId}/modules/{moduleId}", userId, moduleId)
            .retrieve()
            .toBodilessEntity();
    }
}
