package com.example.mdv.bpmn.actions;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RequestExternalService extends AbstractDelegate {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(DelegateExecution delegateExecution) {
        //todo: implement it
    }
}
