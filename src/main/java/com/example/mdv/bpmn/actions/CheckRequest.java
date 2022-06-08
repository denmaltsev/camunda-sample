package com.example.mdv.bpmn.actions;

import com.example.mdv.scenario.BpmnErrors;
import com.example.mdv.scenario.BpmnProcessVariables;
import com.example.mdv.scenario.RequestFormatType;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class CheckRequest extends AbstractDelegate {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(DelegateExecution execution) {
        //TODO: implement it
        String inputRequest = (String) execution.getVariable(BpmnProcessVariables.INPUT_DATA);
        try {
            if (!Objects.isNull(inputRequest) && !inputRequest.isEmpty()) {
                RequestFormatType requestFormat = checkRequestFormatType(inputRequest).orElseThrow(() -> new IllegalArgumentException("Не верный формат входящего сообщения"));
            } else {
                throw new IllegalArgumentException("Входящее сообщение пустое или не найдено в переменной процесса " + BpmnProcessVariables.INPUT_DATA);
            }
        } catch (IllegalArgumentException e) {
            throw new BpmnError(BpmnErrors.REQUEST_VALIDATION.name(), e.getMessage());
        }
    }

    private Optional<RequestFormatType> checkRequestFormatType(String inputRequest) {
        return RequestFormatType.getByToken(inputRequest.trim().substring(0,1));
    }
}
