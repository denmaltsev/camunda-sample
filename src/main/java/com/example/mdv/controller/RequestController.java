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
    private final static String SCENARIO_ID="Scenatio name put here"
    
    @PostMapping(value = "/run", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> run(@RequestBody String request) {

        Map<String, Object> scenarioParamsMap = new HashMap<>();
        request = StringUtils.normalizeSpace(request);
        scenarioParamsMap.put(INPUT_DATA, request);
        
        log.info("Received request: {}", request);
        
        Map<String, Object> bpmnProcessExecutoionResultMap = startProcess(SCENARIO_ID, UUID.randomUUID().toString(), scenatioParamsMap);
            
        return new ResponseEntity<>("ok", HttpStatus.OK);
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
}
