package org.mohrm.example.some_spring_boot_frontend_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class SomeFrontendRestController {

    private final WebClient webClient;

    public SomeFrontendRestController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/double")
    public Mono<ResponseEntity<String>> doubleTheNumber(ServerWebExchange exchange, @RequestParam("arg") Integer number) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/double").queryParam("arg", number).build())
                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
                .map(ResponseEntity::ok);
    }
}
