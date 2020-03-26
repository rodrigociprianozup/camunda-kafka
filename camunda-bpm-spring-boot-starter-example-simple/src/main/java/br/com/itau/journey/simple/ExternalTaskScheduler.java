package br.com.itau.journey.simple;

import br.com.itau.journey.domain.ExternalTaskAccessInfo;
import br.com.itau.journey.domain.KafkaExternalTask;
import br.com.itau.journey.domain.KafkaExternalTasks;
import br.com.itau.journey.rocksdb.RocksDBKeyValueService;
import br.com.itau.journey.rocksdb.kv.exception.SaveFailedException;
import br.com.itau.journey.rocksdb.mapper.exception.SerializationException;
import br.com.itau.journey.service.ProducerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;

@Component
public class ExternalTaskScheduler {

    private ProducerService producerService;
    private RocksDBKeyValueService rocksDBKeyValueService;
    private ObjectMapper objectMapper;

    @Autowired
    public ExternalTaskScheduler(ProducerService producerService, RocksDBKeyValueService rocksDBKeyValueService, ObjectMapper objectMapper) {
        this.producerService = producerService;
        this.rocksDBKeyValueService = rocksDBKeyValueService;
        this.objectMapper = objectMapper;
    }

    public void scheduler(ExternalTaskAccessInfo externalTaskAccessInfo) {
        ofNullable(externalTaskAccessInfo)
                .ifPresent(externalTaskAccessInfo1 -> ofNullable(externalTaskAccessInfo1.getKafkaTopics())
                        .ifPresent(kafkaTopicList -> kafkaTopicList.parallelStream().forEach(sendToKafkaTopic(externalTaskAccessInfo1))));
    }

    private Consumer<String> sendToKafkaTopic(ExternalTaskAccessInfo externalTaskAccessInfo1) {
        return kafkaTopic -> {
            Message<KafkaExternalTask> externalTaskMessage = getExternalTaskMessage(externalTaskAccessInfo1.getKafkaExternalTask(), kafkaTopic);
            producerService.sendToKafka(externalTaskMessage);

            try {
                String value = objectMapper.writeValueAsString(new KafkaExternalTasks(Collections.singletonList(externalTaskAccessInfo1.getKafkaExternalTask())));
                rocksDBKeyValueService.save(externalTaskAccessInfo1.getKafkaExternalTask().getProcessInstanceId(), value);
            } catch (SaveFailedException | IOException e) {
                e.printStackTrace();
            }
        };
    }

    public Message<KafkaExternalTask> getExternalTaskMessage(KafkaExternalTask kafkaExternalTask, String kafkaTopic) {
        return MessageBuilder
                .withPayload(kafkaExternalTask)
                .setHeader(KafkaHeaders.TOPIC, kafkaTopic)
                .build();
    }
}
