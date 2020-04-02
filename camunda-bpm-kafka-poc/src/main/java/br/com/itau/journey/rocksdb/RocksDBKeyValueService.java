package br.com.itau.journey.rocksdb;

import br.com.itau.journey.domain.KafkaExternalTask;
import br.com.itau.journey.domain.KafkaExternalTasks;
import br.com.itau.journey.rocksdb.configuration.RocksDBConfiguration;
import br.com.itau.journey.rocksdb.kv.exception.FindFailedException;
import br.com.itau.journey.rocksdb.kv.exception.SaveFailedException;
import br.com.itau.journey.rocksdb.mapper.exception.DeserializationException;
import br.com.itau.journey.rocksdb.mapper.exception.SerDeException;
import br.com.itau.journey.rocksdb.repository.RocksDBKeyValueRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.itau.journey.constant.TypeComponent.END_EVENT;
import static br.com.itau.journey.constant.TypeComponent.USER_TASK;

@Slf4j
@Component
public class RocksDBKeyValueService extends RocksDBKeyValueRepository<String, String> {

    private ObjectMapper objectMapper;

    public RocksDBKeyValueService(ObjectMapper objectMapper) {
       super(new RocksDBConfiguration("/src/main/resources/data/repositories", "db"));
       this.objectMapper = objectMapper;
    }

    @Override
    public void save(String key, String value) throws IOException, SaveFailedException {
        Optional<String> valuesCurrent = null;
        try {
            valuesCurrent = this.findByKey(key);
        } catch (SerDeException | FindFailedException e) {
            e.printStackTrace();
        }

        KafkaExternalTasks tasksCurrents = null;
        if (valuesCurrent.isPresent()) {
            tasksCurrents = this.objectMapper.readValue(valuesCurrent.get(), KafkaExternalTasks.class);
            KafkaExternalTasks taskNow = this.objectMapper.readValue(value, KafkaExternalTasks.class);
            tasksCurrents.getKafkaExternalTasks().addAll(taskNow.getKafkaExternalTasks());
            value = this.objectMapper.writeValueAsString(tasksCurrents);
        }

        super.save(key, value);
    }

    @Override
    public Collection<String> findAll() throws DeserializationException {
        return super.findAll();
    }

    @Override
    public Optional<String> findByKey(String key) throws SerDeException, FindFailedException {
        return super.findByKey(key);
    }

    public void setCompleteTask(String taskId, String processInstanceId) throws IOException, SaveFailedException {
        Optional<String> valuesCurrent = null;
        try {
            valuesCurrent = this.findByKey(processInstanceId);
        } catch (SerDeException | FindFailedException e) {
            e.printStackTrace();
        }

        KafkaExternalTasks tasksCurrents = null;
        if (valuesCurrent.isPresent()) {
            tasksCurrents = this.objectMapper.readValue(valuesCurrent.get(), KafkaExternalTasks.class);
            tasksCurrents.getKafkaExternalTasks().stream().forEach(item -> completed(taskId, item));
            valuesCurrent = Optional.ofNullable(this.objectMapper.writeValueAsString(tasksCurrents));
        }

        super.save(processInstanceId, valuesCurrent.get());
    }

    private void completed(String taskId, KafkaExternalTask item) {
        if (taskId.equals(item.getTaskId())) {
            item.setTaskComplete(Boolean.TRUE);
        }
    }

    public Optional<String> getProcessInstance(String processInstanceId, String taskId) throws IOException {
        try {
            Optional<String> values = super.findByKey(processInstanceId);
            if (values.isPresent()) {
                KafkaExternalTasks kafkaExternalTasks = this.objectMapper.readValue(values.get(), KafkaExternalTasks.class);
                Optional<KafkaExternalTask> userTask = kafkaExternalTasks.getKafkaExternalTasks().stream().filter(item -> isExternalUserTask(item, taskId)).findAny();
                if (userTask.isPresent()) {
                    return Optional.ofNullable(this.objectMapper.writeValueAsString(userTask.get()));
                }
                Optional<KafkaExternalTask> endEvent = kafkaExternalTasks.getKafkaExternalTasks().stream().filter(this::isEndEvent).findAny();
                if (endEvent.isPresent()) {
                    return Optional.ofNullable(this.objectMapper.writeValueAsString(endEvent.get()));
                }
            }
        } catch (SerDeException | FindFailedException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean isEndEvent(String processInstanceId) throws FindFailedException, IOException {
        Optional<String> values = super.findByKey(processInstanceId);
        if (values.isPresent()) {
            KafkaExternalTasks kafkaExternalTasks = this.objectMapper.readValue(values.get(), KafkaExternalTasks.class);
            Optional<KafkaExternalTask> endEvent = kafkaExternalTasks.getKafkaExternalTasks().stream().filter(this::isEndEvent).findAny();
            if (endEvent.isPresent()) {
                return true;
            }
        }
        return false;
    }

    private boolean isEndEvent(KafkaExternalTask item) {
        return END_EVENT.getEvent().equals(item.getType());
    }

    private boolean isExternalUserTask(KafkaExternalTask item, String taskId) {
        return isNotEqualsTaskId(item, taskId) && USER_TASK.getEvent().equals(item.getType()) && !item.isTaskComplete();
    }

    private boolean isNotEqualsTaskId(KafkaExternalTask item, String taskId) {
        return taskId == null || !taskId.equals(item.getTaskId());
    }
}
