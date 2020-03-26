package br.com.itau.journey.service;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.util.List;

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

    public ResponseEntity<String> getProcessInstance(String ksql) throws URISyntaxException {
        return ksqlInstanceService.getProcessInstance(ksql);
    }

    public List<Task> findPendentTasks(String processInstanceId) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    }


}