package com.amine.billardbook.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

/**
 * UserDetailsDto .
 * @param login .
 * @param name .
 * @param ownedReservations .
 */
@JacksonXmlRootElement(localName = "user")
public record UserDetailsDto(
        String login,
        String name,
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "ownedReservation")
        @JsonUnwrapped
        List<String> ownedReservations) {
}
