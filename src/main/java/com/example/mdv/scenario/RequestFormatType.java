package com.example.mdv.scenario;

import java.util.Arrays;
import java.util.Optional;

public enum RequestFormatType {
    JSON ("{")
    , XML ("<");

    private final String token;

    RequestFormatType(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public static Optional<RequestFormatType> getByToken(String token) {
        return Arrays.stream(RequestFormatType.values()).filter(format -> format.getToken().equals(token)).findFirst();
    }
}
