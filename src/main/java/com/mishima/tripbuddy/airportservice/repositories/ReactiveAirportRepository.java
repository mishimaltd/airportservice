package com.mishima.tripbuddy.airportservice.repositories;

import com.mishima.tripbuddy.airportservice.entity.Airport;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Point;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ReactiveAirportRepository extends ReactiveCrudRepository<Airport,String> {

    Mono<Airport> findByCode(String code);

    Flux<Airport> findByCityId(String cityId);

    // No metric: {'geoNear' : 'airport', 'near' : [x, y], maxDistance : distance }
    // Metric: {'geoNear' : 'airport', 'near' : [x, y], 'maxDistance' : distance,
    //          'distanceMultiplier' : metric.multiplier, 'spherical' : true }
    Flux<GeoResult<Airport>> findByLocationNear(Point location, Distance distance);

    Mono<Airport> deleteByCode(String code);

}
