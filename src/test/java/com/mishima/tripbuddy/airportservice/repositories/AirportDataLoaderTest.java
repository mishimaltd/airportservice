package com.mishima.tripbuddy.airportservice.repositories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mishima.tripbuddy.airportservice.entity.Airport;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
@Ignore("run manually")
public class AirportDataLoaderTest {

    private final ObjectMapper om = new ObjectMapper();
    private final TypeReference tr = new TypeReference<Map<String,Object>>() {};

    private final WebClient webClient = WebClient.create("https://airportservice-tripbuddy.herokuapp.com/airports");

    @Test
    @SuppressWarnings("unchecked")
    public void loadFromSkyscanner() throws Exception {
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


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
                        webClient.put().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(om.writeValueAsString(airport))).exchange().block();
                        log.info("Saved airport {}", newAirport);
                    }
                }
            }
        }
    }

}
