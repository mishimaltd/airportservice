package com.mishima.tripbuddy.airportservice.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Document
public class Airport {

    @Id
    private String id;
    @Indexed(unique = true)
    private String code;
    private String name;
    private String cityId;
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private double[] location;

    private Airport() {
    }

    public Airport(String code, String name, String cityId, double[] location) {
        this.code = code;
        this.name = name;
        this.cityId = cityId;
        this.location = location;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String code;
        private String name;
        private String cityId;
        private double[] location;

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder cityId(String cityId) {
            this.cityId = cityId;
            return this;
        }

        public Builder location(double[] location) {
            this.location = location;
            return this;
        }

        public Airport build() {
            return new Airport(code, name, cityId, location);
        }
    }
}
