package ac.unindra.roemah_duren_spring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BeanConfiguration {
    @Bean
    public WebClient.Builder webClient(@Value("${app.roemah-duren.base-url-api}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl);
    }
}
