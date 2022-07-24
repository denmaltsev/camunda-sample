package com.example.mdv.dto.request;

import com.example.mdv.dto.modules.ZonedDateTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.ZonedDateTime;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SampleInputDataWithDateStart {

    @JsonProperty(value = "code", required = true)
    private String code;
    @JsonProperty(value = "name", required = true)
    private String name;

    @JsonProperty("startDate")
    @JsonFormat(pattern = ZonedDateTimeDeserializer.ZONED_DATE_TIME_FORMAT)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime startDate;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @JsonProperty("startDate")
    public Optional<ZonedDateTime> getStartDate() {
        return Optional.ofNullable(startDate);
    }
}
