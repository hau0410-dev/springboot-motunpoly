package poly.edu.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // FIX: bỏ RestTemplateBuilder (không có sẵn/đã bị tái cấu trúc gói ở Spring Boot 4.0.1),
    // tự tạo RestTemplate bằng class lõi org.springframework.web.client.RestTemplate
    // + SimpleClientHttpRequestFactory (đều nằm trong spring-web, chắc chắn có sẵn).
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(15).toMillis());
        return new RestTemplate(factory);
    }
}