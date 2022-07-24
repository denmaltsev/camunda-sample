package com.example.mdv.bpmn.actions;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TaskLogDelegate implements JavaDelegate {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Override
    public void execute(DelegateExecution execution) {
        try {
            log.trace("Delegate {} was called for process {}",
                    getClass().getSimpleName(),
                    execution.getProcessInstanceId());
        } catch (Exception exception) {
            log.error("Error occurred", exception);
        }
    }
}
