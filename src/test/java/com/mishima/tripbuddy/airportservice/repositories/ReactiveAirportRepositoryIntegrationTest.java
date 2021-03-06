package com.mishima.tripbuddy.airportservice.repositories;

import com.mishima.tripbuddy.airportservice.entity.Airport;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ReactiveAirportRepositoryIntegrationTest {

    @Autowired
    private ReactiveAirportRepository repository;

    @Autowired
    private ReactiveMongoOperations operations;

    @Before
    public void setUp() {
        operations.collectionExists(Airport.class)
                .flatMap(exists -> exists ? operations.dropCollection(Airport.class) : Mono.just(exists))
                .flatMap(o -> operations.createCollection(Airport.class)
                        .then(operations.indexOps(Airport.class).ensureIndex(new GeospatialIndex("location").typed(GeoSpatialIndexType.GEO_2DSPHERE))))
                .then().block();

        repository.saveAll(Flux.just(
                Airport.builder().name("Raleigh").cityId("RAL").code("RDU").location(new double[]{35.8861980, -79.0601160}).build(),
                Airport.builder().name("Washington").cityId("WAS").code("WAS").location(new double[]{38.953116, -77.456539}).build()))
                .then().block();
    }

    @After
    public void tearDown() {
        operations.dropCollection(Airport.class).then().block();
    }

    @Test
    public void findByCode() {
        Airport airport = repository.findByCode("RDU").block();
        assertNotNull(airport);
        assertEquals("Raleigh", airport.getName());
    }

    @Test
    public void findByCityId() {
        List<Airport> airports = repository.findByCityId("RAL").collect(Collectors.toList()).block();
        assertNotNull(airports);
        assertEquals(1, airports.size());
    }

    @Test
    public void deleteByCode() {
        assertNotNull(repository.findByCode("RDU").block());
        repository.deleteByCode("RDU").then().block();
        assertNull(repository.findByCode("RDU").block());
    }

    @Test
    public void findNear() {
        Point point = new Point(35.8861980, -79.0601160);
        Distance distance = new Distance(10, Metrics.MILES);
        List<GeoResult<Airport>> airports = repository.findByLocationNear(point, distance).collect(Collectors.toList()).block();
        assertNotNull(airports);
        assertEquals(1, airports.size());
    }
}
