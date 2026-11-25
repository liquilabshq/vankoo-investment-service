package com.liquilabs.vankoo.investment.infrastructure.acl.finance;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
@EnableConfigurationProperties(FinanceProperties.class)
public class FinanceClientConfiguration {

    /** Its own builder, so a test can bind a {@code MockRestServiceServer} to it. */
    @Bean
    RestClient.Builder financeRestClientBuilder(FinanceProperties properties) {
        var httpClient = HttpClient.newBuilder().connectTimeout(properties.timeout()).build();
        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.timeout());
        return RestClient.builder().baseUrl(properties.baseUrl()).requestFactory(requestFactory);
    }
}
