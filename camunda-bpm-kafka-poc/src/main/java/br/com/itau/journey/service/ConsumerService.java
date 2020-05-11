package br.com.itau.journey.service;

import br.com.itau.journey.domain.KafkaExternalTask;
import br.com.itau.journey.domain.KafkaExternalTaskListner;
import br.com.itau.journey.domain.KafkaExternalTasks;
import br.com.itau.journey.rocksdb.RocksDBKeyValueService;
import br.com.itau.journey.rocksdb.kv.exception.SaveFailedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;

@Service
@Slf4j
public class ConsumerService {

    private ObjectMapper objectMapper;
    private RocksDBKeyValueService rocksDBKeyValueService;
    private ProcessInstanceService processInstanceService;
    private TaskService taskService;
    private ProducerService producerService;
    private final String TOPIC_START_EVENT = "start-process";
    private final String TOPIC_STEPS_EVENT = "steps-process";
    private final String TOPIC_COMPLETE_TASK = "complete-process";
    private final String TOPIC_UPDATE_PROPOSAL_EVENT = "update-proposal-process";
    private final String TOPIC_USER_TASK_EVENT = "user-tasks-process";
    private final String TOPIC_FRAUD_EVENT = "fraud-process";

    @Autowired
    public ConsumerService(ObjectMapper objectMapper, RocksDBKeyValueService rocksDBKeyValueService, ProcessInstanceService processInstanceService, TaskService taskService, ProducerService producerService) {
        this.objectMapper = objectMapper;
        this.rocksDBKeyValueService = rocksDBKeyValueService;
        this.processInstanceService = processInstanceService;
        this.taskService = taskService;
        this.producerService = producerService;
    }

    @KafkaListener(
            id = "startEventProcessor",
            topics = TOPIC_START_EVENT,
            containerFactory = "kafkaListenerContainerFactory")
    public void listenStartProcess(String message) throws IOException, SaveFailedException, InterruptedException {
        KafkaExternalTaskListner kafkaExternalTaskListner = this.objectMapper.readValue(message, KafkaExternalTaskListner.class);
        String processInstanceId = processInstanceService.startProcessInstance(kafkaExternalTaskListner.getPayload().getBpmnInstance());
        kafkaExternalTaskListner.getPayload().setProcessInstanceId(processInstanceId);
        log.info(":: Listener Start ProcessInstanceId {} - Process: {}",processInstanceId,  message);
        rocksDBKeyValueService.save(kafkaExternalTaskListner.getPayload().getUuid(), kafkaExternalTaskListner.getPayload().getProcessInstanceId());
        rocksDBKeyValueService.save(kafkaExternalTaskListner.getPayload().getCpf(), kafkaExternalTaskListner.getPayload().getProcessInstanceId());
    }

    @KafkaListener(
            id = "stepsEventProcessor",
            topics = TOPIC_STEPS_EVENT,
            containerFactory = "kafkaListenerContainerFactory")
    public void listenStepProcess(String message) throws IOException, SaveFailedException {
        KafkaExternalTaskListner kafkaExternalTaskListner = this.objectMapper.readValue(message, KafkaExternalTaskListner.class);
        log.info(":: Listener Step Process: {}",  message);
        KafkaExternalTasks kafkaExternalTasks =
                KafkaExternalTasks.builder().kafkaExternalTasks(Collections.singletonList(kafkaExternalTaskListner.getPayload())).build();
        rocksDBKeyValueService.save(kafkaExternalTaskListner.getPayload().getProcessInstanceId(), this.objectMapper.writeValueAsString(kafkaExternalTasks));
    }

    @KafkaListener(
            id = "updateProposalEventProcessor",
            topics = TOPIC_UPDATE_PROPOSAL_EVENT,
            containerFactory = "kafkaListenerContainerFactory")
    public void listenUpdateProposal(String message) throws IOException, InterruptedException {
        KafkaExternalTaskListner kafkaExternalTaskListner = this.objectMapper.readValue(message, KafkaExternalTaskListner.class);
        log.info(":: Listener Update Proposal Process: {}",  message);
        // update proposal process
        this.sendKafka(kafkaExternalTaskListner.getPayload(), TOPIC_COMPLETE_TASK);
    }

    @KafkaListener(
            id = "userTaskEventProcessor",
            topics = TOPIC_USER_TASK_EVENT,
            containerFactory = "kafkaListenerContainerFactory")
    public void listenUserTask(String message) throws IOException, SaveFailedException {
        KafkaExternalTaskListner kafkaExternalTaskListner = this.objectMapper.readValue(message, KafkaExternalTaskListner.class);
        kafkaExternalTaskListner.getPayload().setInternalUserTask(Boolean.FALSE);
        log.info(":: Listener User Task Process: {}",  message);
        // atualizacao dados cadastrais
        this.sendKafka(kafkaExternalTaskListner.getPayload(), TOPIC_COMPLETE_TASK);
        rocksDBKeyValueService.setCompleteTask(kafkaExternalTaskListner.getPayload().getTaskId(), kafkaExternalTaskListner.getPayload().getProcessInstanceId());
    }

    @KafkaListener(
            id = "fraudEventProcessor",
            topics = TOPIC_FRAUD_EVENT,
            containerFactory = "kafkaListenerContainerFactory")
    public void listenFraud(String message) throws IOException, InterruptedException {
        KafkaExternalTaskListner kafkaExternalTaskListner = this.objectMapper.readValue(message, KafkaExternalTaskListner.class);
        log.info(":: Listener Fraud Process: {}",  message);
        this.sendKafka(kafkaExternalTaskListner.getPayload(), TOPIC_COMPLETE_TASK);
    }

    @KafkaListener(
            id = "completeTaskProcessor",
            topics = TOPIC_COMPLETE_TASK,
            containerFactory = "kafkaListenerContainerFactory")
    public void listenComplete(String message) throws IOException, InterruptedException {
        Thread.sleep(3000);
        KafkaExternalTaskListner kafkaExternalTaskListner = this.objectMapper.readValue(message, KafkaExternalTaskListner.class);
        log.info(":: Listener Complete Process: {}",  message);
        this.completeTask(kafkaExternalTaskListner.getPayload().getTaskId());
    }

    private void completeTask(String taskId) {
        log.info(":: Completing task: {}", taskId);
        taskService.complete(taskId);
        log.info(":: Completed task: {}", taskId);
    }

    private void sendKafka(KafkaExternalTask kafkaExternalTask, String topic) {
        Message<KafkaExternalTask> message = MessageBuilder
                .withPayload(kafkaExternalTask)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build();
        producerService.sendToKafka(message);
    }


}
