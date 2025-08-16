package com.amine.billardbook.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

/**
 * DTO de type UserResponse (voir doc OpenAPI).
 * @param users Liste de liens vers les utilisateurs
 */
@JacksonXmlRootElement(localName = "users")
public record UsersResponseDto(
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "link")
        @JsonUnwrapped
        List<LinkDto> users) {
}
