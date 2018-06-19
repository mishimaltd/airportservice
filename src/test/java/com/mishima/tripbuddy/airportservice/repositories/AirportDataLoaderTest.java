package com.mishima.tripbuddy.airportservice.repositories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mishima.tripbuddy.airportservice.entity.Airport;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
@Ignore("run manually")
public class AirportDataLoaderTest {

    @Autowired
    private ReactiveAirportRepository repository;

    @Autowired
    private ReactiveMongoOperations operations;

    private final ObjectMapper om = new ObjectMapper();
    private final TypeReference tr = new TypeReference<Map<String,Object>>() {};

    @After
    public void setup() {
        operations.dropCollection(Airport.class).then().block();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void loadFromSkyscanner() throws Exception {
        RestTemplate template = new RestTemplate();
        String response = template.getForObject("http://partners.api.skyscanner.net/apiservices/geo/v1.0?apikey=prtl6749387986743898559646983194", String.class);
        Map<String, Object> json = om.readValue(response, tr);
        List<Map<String, Object>> continents = (List<Map<String, Object>>) json.get("Continents");
        for (Map<String, Object> continent : continents) {
            List<Map<String, Object>> countries = (List<Map<String, Object>>) continent.get("Countries");
            for (Map<String, Object> country : countries) {
                List<Map<String, Object>> cities = (List<Map<String, Object>>) country.get("Cities");
                for (Map<String, Object> city : cities) {
                    List<Map<String, Object>> airports = (List<Map<String, Object>>) city.get("Airports");
                    for (Map<String, Object> airport : airports) {
                        String[] location = ((String) airport.get("Location")).split(", ");
                        Airport newAirport = Airport.builder()
                                .code((String) airport.get("Id"))
                                .name((String) airport.get("Name"))
                                .cityId((String) airport.get("CityId"))
                                .location(new double[]{Double.valueOf(location[0]), Double.valueOf(location[1])})
                                .build();
                        repository.save(newAirport).block();
                        log.info("Saved airport {}", newAirport);
                    }
                }
            }
        }
    }

}
