package org.mohrm.example.some_spring_boot_frontend_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Configuration
public class WebClientConfiguration {

    @Bean
    public ReactiveOAuth2AuthorizedClientManager reactiveOAuth2AuthorizedClientManager(ReactiveClientRegistrationRepository clientRegistrationRepository, @Value("app.user.username") String username, @Value("app.user.password") String password) {
        /*
         * Plan: Construct a AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager. This object has the following
         * collaborators:
         * (i) ReactiveClientRegistrationRepository - registry of the available clients
         * (ii) ReactiveOAuth2AuthorizedClientService - obtains the actual token
         * (iii) ReactiveOAuth2AuthorizedClientProvider - strategy which passes the neccessary data to the service
         * Since we need a password authorization grant, we also need
         * (iv) a context attribute mapper which equips the OAuth2AuthorizationContext with username and password
         * (so they can be passed to the authorization request generated by the service)
         */

        /* (i) ReactiveClientRegistrationRepository is provided from outside (application config) */

        /* (ii) From the repository we can obtain the service */
        var authService = new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);

        /* (iii) the provider needs to be configured for password authorization grant -- we also want to support refresh token */
        var provider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .password()
                .refreshToken()
                .build();

        /* We want a manager that is able to obtain a token outside a http request context */
        var manager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, authService);
        /* connect manager to provider */
        manager.setAuthorizedClientProvider(provider);
        /* username and password are passed via context attributes */
        manager.setContextAttributesMapper(request -> Mono.fromSupplier(() -> {
            var m = new HashMap<String, Object>();
            m.put(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, username);
            m.put(OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, password);
            return m;
        }));

        return manager;
    }

    @Bean
    public WebClient webClient(@Value("${backend.url}") String backendUrl, ReactiveOAuth2AuthorizedClientManager manager) {
        var oauth2Filter = new ServerOAuth2AuthorizedClientExchangeFilterFunction(manager);
        return WebClient.builder()
                .baseUrl(backendUrl)
                .filter(oauth2Filter)
                .filter((request, next) -> {
                   System.out.println(request.headers().toSingleValueMap());
                   return next.exchange(request);
                })
                /* we need to specify which entry of the client registration repository shall be used */
                .defaultRequest(requestSpec -> requestSpec.attributes(ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId("service")))
                .build();
    }
}
