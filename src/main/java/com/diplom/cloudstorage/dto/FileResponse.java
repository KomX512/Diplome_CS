package com.diplom.cloudstorage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FileResponse(
        @JsonProperty("filename") String filename,
        @JsonProperty("size") long size
) {}
