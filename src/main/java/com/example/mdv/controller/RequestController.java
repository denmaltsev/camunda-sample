package com.example.mdv.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.camunda.bpm.engine.RuntimeService;

@RestController
@RequestMapping("/api/v0")
public class RequestController {
    Logger log = LoggerFactory.getLogger(this.getClass());

    //todo: перенести константы в отдельный класс
    private final static String INPUT_DATA="intutData";
    private final static String SCENARIO_ID="Scenatio name put here";
    
    public static final String DEFAULT_ERROR_RESPONSE_BODY = "{ \"status\": \"error\" }";
    public static final String BPMN_PROCESS_RESPONSE = "bpmnProcessResponse";
    public static final String BPMN_PROCESS_STATUS = "bpmnProcessStatus";
    public static final String HTTP_RESPONSE_STATUS = "httpResponseStatus";
    
    @PostMapping(value = "/run", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> run(@RequestBody String request) {

        Map<String, Object> scenarioParamsMap = new HashMap<>();
        request = StringUtils.normalizeSpace(request);
        scenarioParamsMap.put(INPUT_DATA, request);
        
        log.info("Received request: {}", request);
        
        Map<String, Object> bpmnProcessExecutoionResultMap = startProcess(SCENARIO_ID, UUID.randomUUID().toString(), scenatioParamsMap);

        String bpmnProcessWrappedResponse = getBpmnExecutionVariableValue(bpmnProcessExecutionResultMap, TransactSmScenarioVariables.BPMN_PROCESS_RESPONSE);
        String bpmnProcessStatus = getBpmnExecutionVariableValue(bpmnProcessExecutionResultMap, BPMN_PROCESS_STATUS);
        String bpmnResponseHttpCode = getBpmnExecutionVariableValue(bpmnProcessExecutionResultMap, HTTP_RESPONSE_STATUS);

        String bpmnProcessResponse = getMessageFromBpmnWrapper(bpmnProcessWrappedResponse);

        final Optional<HttpStatus> status = Optional.of(resolveProcessResponseHttpCode(bpmnResponseHttpCode, bpmnProcessStatus));
        String response = StringUtils.isNotBlank(bpmnProcessResponse) ? bpmnProcessResponse : DEFAULT_ERROR_RESPONSE_BODY;
        
        log.info("Sent response: {}", response);
        log.debug("Sent response with httpCode='{}'", status.get().value());

        return ResponseEntity
                .status(status.get())
                .headers(headers)
                .headers(headersOut)
                .contentType(APPLICATION_JSON)
                .contentLength(response.getBytes().length)
                .body(response);        
    //    return new ResponseEntity<>("ok", HttpStatus.OK);
    }

       private Map<String, Object> startProcess(@NotNull String scenarioId, @NotNull String businessKey, Map<String, Object> payload) {
        Map<String, Object> params = prepareStartProcess(scenarioId, businessKey, payload);
        ProcessInstanceWithVariables processInstance = runtimeService.createProcessInstanceByKey(scenarioId)
                .businessKey(businessKey)
                .setVariables(params)
                .executeWithVariablesInReturn();
        if (processInstance != null) {
            List<Map<String, Object>> vars = new ArrayList<>(processInstance.getVariables().size());
            processInstance.getVariables().entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .forEach(entry -> vars.add(new HashMap<String, Object>() {
                        {
                            put("name", entry.getKey());
                            try {
                                put("value", objectMapper.writeValueAsString(entry.getValue()));
                            } catch (JsonProcessingException e) {
                                log.error("Failed convert value [ {} ] for variable {}", entry.getValue(), entry.getKey());
                            }
                            put("typeName", entry.getValue().getClass().getSimpleName());
                        }
                    }));
            return new HashMap<String, Object>() {{
                put("id", processInstance.getId());
                put("definitionId", processInstance.getProcessDefinitionId());
                put("businessKey", processInstance.getBusinessKey());
                put("suspended", processInstance.isSuspended());
                put("ended", processInstance.isEnded());
                put("variables", vars);
            }};
        }
        return null;
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
        final List<Map<String, Object>> resultVariables = (List<Map<String, Object>>) bpmnProcessExecutionResultMap.get("variables");
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
            //todo: change to Jackson
            message = GSON.fromJson(message, JsonObject.class).get(MESSAGE_PATH).getAsString();
        } catch (Exception ex) {
            log.warn("Cannot get message from a wrapper response!");
        }
        return message;
    }    
}
