package br.com.itau.journey.configuration;

import br.com.itau.journey.camunda.ProcessEnginePlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationContextConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);

        return new RestTemplate(factory);
    }

}
