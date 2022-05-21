package com.example.mdv.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v0")
public class RequestController {
    Logger log = LoggerFactory.getLogger(this.getClass());


    @PostMapping(value = "/run", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> run(@RequestBody String request) {

        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

}
