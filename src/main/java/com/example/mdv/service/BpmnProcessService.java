package com.example.mdv.service;

import com.example.mdv.scenario.BpmnProcessVariables;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BpmnProcessService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final RuntimeService runtimeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BpmnProcessService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    public Map<String, Object> startProcess(@NotNull String scenarioId, @NotNull String businessKey, Map<String, Object> payload) {
        Map<String, Object> params = prepareStartProcess(scenarioId, businessKey, payload);
        ProcessInstanceWithVariables processInstance = runtimeService
                .createProcessInstanceByKey(scenarioId)
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
    

    /**
     * Подготовка параметров для запуска процесса с проверкой на идемпотентность
     *
     * @param scenarioId  Идентификатор сценария
     * @param businessKey бизнес-ключ (уникальный идентификатор) процесса
     * @param payload     Данные, необходимые для запуска сценария
     *                    Пример входных данных:
     *                    Map<String, Object> payload_ = new HashMap<String, Object>() {
     *                    {
     *                    put("param1", new Date());
     *                    put("param2", "test2");
     *                    <p>
     *                    }
     *                    }
     */
    private Map<String, Object> prepareStartProcess(@NotNull String scenarioId, @NotNull String businessKey, Map<String, Object> payload) {
        Map<String, Object> params = new HashMap<String, Object>() {
            {
                put(BpmnProcessVariables.SCENARIO_ID, scenarioId);
            }
        };

        if (payload != null && !payload.isEmpty()) {
            params.put(BpmnProcessVariables.INPUT_DATA, payload);
        }
        return params;
    }    
}
