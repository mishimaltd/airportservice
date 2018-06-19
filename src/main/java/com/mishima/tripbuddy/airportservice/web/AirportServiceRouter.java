package com.mishima.tripbuddy.airportservice.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class AirportServiceRouter {

    @Bean
    public RouterFunction<ServerResponse> route(AirportServiceHandler handler) {
        return RouterFunctions.route(GET("/airports").and(accept(APPLICATION_JSON)), handler::findAll)
                .and(RouterFunctions.route(GET("/airports/code/{code}").and(accept(APPLICATION_JSON)), handler::getByCode))
                .and(RouterFunctions.route(GET("/airports/cityid/{cityId}").and(accept(APPLICATION_JSON)), handler::getByCityId))
                .and(RouterFunctions.route(GET("/airports/near/{lat}/{lng}/{distance}").and(accept(APPLICATION_JSON)), handler::findNear))
                .and(RouterFunctions.route(PUT("/airports").and(contentType(APPLICATION_JSON)), handler::save))
                .and(RouterFunctions.route(DELETE("/airports/id/{id}"), handler::deleteById))
                .and(RouterFunctions.route(DELETE("/airports/code/{code}"), handler::deleteByCode));
    }
}
