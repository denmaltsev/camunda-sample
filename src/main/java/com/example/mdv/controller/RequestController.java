package com.example.mdv.controller;

import camundajar.impl.com.google.gson.JsonObject;
import com.example.mdv.scenario.BpmnProcessVariables;
import com.example.mdv.service.BpmnProcessService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static com.example.mdv.scenario.BpmnProcessStatus.SUCCESS;

@RestController
@RequestMapping("/api/v0")
public class RequestController {
    private static final HttpStatus DEFAULT_RESPONSE_HTTP_SUCCESS_STATUS = HttpStatus.OK;
    private static final HttpStatus DEFAULT_RESPONSE_HTTP_ERROR_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;
    public static final String DEFAULT_ERROR_RESPONSE_BODY = "{\"status\": {\"errorCode\": 0}}";
    public final static String BPMN_TASK_ID="SAMPLE_PROCESS_TASK";

    Logger log = LoggerFactory.getLogger(this.getClass());
    private final BpmnProcessService processService;

    @Autowired
    public RequestController(BpmnProcessService processService) {
        this.processService = processService;
    }

    @PostMapping(value = "/run", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> run(@RequestBody String request) {

        Map<String, Object> scenarioParamsMap = new HashMap<>();
        request = StringUtils.normalizeSpace(request);
        scenarioParamsMap.put(BpmnProcessVariables.INPUT_DATA, request);

        log.info("Received request: {}", request);

        Map<String, Object> bpmnProcessExecutionResultMap = processService.startProcess(BPMN_TASK_ID, UUID.randomUUID().toString(), scenarioParamsMap);

        String bpmnProcessWrappedResponse = getBpmnExecutionVariableValue(bpmnProcessExecutionResultMap, BpmnProcessVariables.BPMN_PROCESS_RESPONSE);
        String bpmnProcessStatus = getBpmnExecutionVariableValue(bpmnProcessExecutionResultMap, BpmnProcessVariables.BPMN_PROCESS_STATUS);
        String bpmnResponseHttpCode = getBpmnExecutionVariableValue(bpmnProcessExecutionResultMap, BpmnProcessVariables.HTTP_RESPONSE_STATUS);

        String bpmnProcessResponse = getMessageFromBpmnWrapper(bpmnProcessWrappedResponse);

        final Optional<HttpStatus> status = Optional.of(resolveProcessResponseHttpCode(bpmnResponseHttpCode, bpmnProcessStatus));
        String response = StringUtils.isNotBlank(bpmnProcessResponse) ? bpmnProcessResponse : DEFAULT_ERROR_RESPONSE_BODY;

        log.info("Sent response: {}", response);
        log.debug("Sent response with httpCode='{}'", status.get().value());

        return ResponseEntity
                .status(status.get())
                .contentType(MediaType.APPLICATION_JSON)
                .contentLength(response.getBytes().length)
                .body(response);
        //    return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    private HttpStatus resolveProcessResponseHttpCode(String bpmnResponseHttpCode, String bpmnProcessStatus) {
        HttpStatus status = null;
        if (StringUtils.isNotBlank(bpmnResponseHttpCode)) {
            try {
                status = HttpStatus.resolve(Integer.parseInt(bpmnResponseHttpCode));
            } catch (NumberFormatException ex) {
                log.warn("Переменная bpmnResponseHttpCode содержит значение, не являющееся целым числом: {}", bpmnResponseHttpCode);
            }
        }
        if (status == null && StringUtils.isNotBlank(bpmnProcessStatus)) {
            status = SUCCESS.toString().equals(bpmnProcessStatus) ? DEFAULT_RESPONSE_HTTP_SUCCESS_STATUS :
                    DEFAULT_RESPONSE_HTTP_ERROR_STATUS;
        }
        return (status != null) ? status : DEFAULT_RESPONSE_HTTP_ERROR_STATUS;
    }

    private String getBpmnExecutionVariableValue(Map<String, Object> bpmnProcessExecutionResultMap, String variableName) {
        String bpmnExecutionVariableValue = null;
        final List<Map<String, Object>> resultVariables = Collections.unmodifiableList((List<Map<String, Object>>) bpmnProcessExecutionResultMap.get("variables"));
        final Optional<Map<String, Object>> resultVarMap =
                resultVariables.stream()
                        .filter(varMap -> varMap.get("name").equals(variableName))
                        .findFirst();
        if (resultVarMap.isPresent()) {
            bpmnExecutionVariableValue = resultVarMap.get().get("value").toString();
        }
        return bpmnExecutionVariableValue;
    }

    private String getMessageFromBpmnWrapper(String wrappedMessage) {
        final String MESSAGE_PATH = "message";

        String message = wrappedMessage;
        try {
            ObjectMapper mapper = new ObjectMapper();
            message = mapper.readValue(message, JsonObject.class).get(MESSAGE_PATH).getAsString();
//            message = GSON.fromJson(message, JsonObject.class).get(MESSAGE_PATH).getAsString();
        } catch (Exception ex) {
            log.warn("Cannot get message from a wrapper response!");
        }
        return message;
    }
}
