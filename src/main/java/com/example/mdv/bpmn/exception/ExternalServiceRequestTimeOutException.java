package com.example.mdv.bpmn.exception;

import com.example.mdv.bpmn.actions.AbstractDelegate;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExternalServiceRequestTimeOutException extends AbstractDelegate {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(DelegateExecution delegateExecution) {
        //todo: implement it
    }
}
