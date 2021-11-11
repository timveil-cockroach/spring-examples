package io.crdb.spring.common;

import java.time.ZonedDateTime;
import java.util.UUID;

public record UserDTO(UUID id, String firstName, String lastName, String email, String address, String city, String stateCode, String zipCode, ZonedDateTime createdTimestamp, ZonedDateTime updatedTimestamp) {

    @Override
    public String toString() {
        return "UserDTO{" +
               "id=" + id +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", email='" + email + '\'' +
               ", address='" + address + '\'' +
               ", city='" + city + '\'' +
               ", stateCode='" + stateCode + '\'' +
               ", zipCode='" + zipCode + '\'' +
               ", createdTimestamp=" + createdTimestamp +
               ", updatedTimestamp=" + updatedTimestamp +
               '}';
    }
}
