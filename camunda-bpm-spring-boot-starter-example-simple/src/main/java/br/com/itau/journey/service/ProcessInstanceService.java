package br.com.itau.journey.service;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;

@Slf4j
@Component
public class ProcessInstanceService {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private KSQLInstanceService ksqlInstanceService;
    private String processInstanceId;

    public void completeTask(String taskId) {
        taskService.complete(taskId);
        log.info("completed task: {}", taskId);
    }

    public String startProcessInstance(String sample) {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(sample);
        processInstanceId = processInstance.getProcessInstanceId();
        log.info("started instance: {}", processInstanceId);
        return processInstanceId;
    }

    public ResponseEntity<String> getProcessInstanceId(String processInstanceId) throws URISyntaxException {
        return ksqlInstanceService.getProcessInstanceId(processInstanceId);
    }
}