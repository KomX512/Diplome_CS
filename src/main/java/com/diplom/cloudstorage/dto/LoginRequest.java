package com.diplom.cloudstorage.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginRequest(
        @JsonProperty("login") @JsonAlias("login") String login,
        @JsonProperty("password") String password
) {}
