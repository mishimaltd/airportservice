package com.mishima.tripbuddy.airportservice.web;

import com.mishima.tripbuddy.airportservice.entity.Airport;
import com.mishima.tripbuddy.airportservice.repositories.ReactiveAirportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;

@Component
@Slf4j
public class AirportServiceHandler {

    @Autowired
    private ReactiveAirportRepository repository;

    @Nonnull
    public Mono<ServerResponse> save(ServerRequest request) {
        return request.bodyToMono(Airport.class).flatMap(airport -> {
                log.info("Saving airport -> {}", airport);
                return ServerResponse.ok().contentType(APPLICATION_JSON).body(fromPublisher(repository.save(airport), Airport.class));
        });
    }

    @Nonnull
    public Mono<ServerResponse> findAll(ServerRequest request) {
        Flux<Airport> airports = repository.findAll();
        return ServerResponse.ok().contentType(APPLICATION_JSON).body(airports, Airport.class);
    }

    @Nonnull
    public Mono<ServerResponse> getByCode(ServerRequest request) {
        return repository.findByCode(request.pathVariable("code")).flatMap(airport ->
                ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(airport)))
                    .switchIfEmpty(ServerResponse.notFound().build());
    }

    @Nonnull
    public Mono<ServerResponse> getByCityId(ServerRequest request) {
        Flux<Airport> airports = repository.findByCityId(request.pathVariable("cityId"));
        return ServerResponse.ok().contentType(APPLICATION_JSON).body(airports, Airport.class);
    }

    @Nonnull
    public Mono<ServerResponse> findNear(ServerRequest request) {
        Point point = new Point(Double.valueOf(request.pathVariable("lat")), Double.valueOf(request.pathVariable("lng")));
        Distance distance = new Distance(Double.valueOf(request.pathVariable("distance")), Metrics.MILES);
        Flux<GeoResult<Airport>> airports = repository.findByLocationNear(point, distance);
        return ServerResponse.ok().contentType(APPLICATION_JSON).body(fromPublisher(airports, new ParameterizedTypeReference<GeoResult<Airport>>(){}));
    }

    @Nonnull
    public Mono<ServerResponse> deleteById(ServerRequest request) {
        return repository.deleteById(request.pathVariable("id")).then(ServerResponse.ok().build());
    }

    @Nonnull
    public Mono<ServerResponse> deleteByCode(ServerRequest request) {
        return repository.deleteByCode(request.pathVariable("code")).then(ServerResponse.ok().build());
    }
}
