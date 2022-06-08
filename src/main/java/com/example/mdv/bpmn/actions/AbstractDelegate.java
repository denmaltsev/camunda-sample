package com.example.mdv.bpmn.actions;

import com.example.mdv.scenario.BpmnErrors;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDelegate implements JavaDelegate {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(DelegateExecution execution) {
        try {
            log.trace("Delegate {} was called for process {}",
                    getClass().getSimpleName(),
                    execution.getProcessInstanceId());
            run(execution);
        } catch (Exception exception) {
            log.error("Error occurred", exception);
            executeHandling(execution, exception);
        }
    }

    public void executeHandling(DelegateExecution execution, Exception exception) {
        throw new BpmnError(BpmnErrors.INTERNAL_ERROR.name(), exception);
    }

    public abstract void run(DelegateExecution delegateExecution);
}
