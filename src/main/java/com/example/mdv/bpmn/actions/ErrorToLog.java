package com.example.mdv.bpmn.actions;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ErrorToLog extends AbstractDelegate {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(DelegateExecution execution) {
        log.error("Log process error. Process variables: \n{}"
                , execution.getVariables().entrySet()
                        .stream().map(entity -> "key: " +  entity.getKey() + "; value: " + entity.getValue())
                        .collect(Collectors.joining(System.lineSeparator()))
        );
    }
}
