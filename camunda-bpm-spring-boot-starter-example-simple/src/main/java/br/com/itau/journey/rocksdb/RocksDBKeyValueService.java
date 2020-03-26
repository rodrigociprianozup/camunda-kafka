package br.com.itau.journey.rocksdb;

import br.com.itau.journey.domain.KafkaExternalTask;
import br.com.itau.journey.domain.KafkaExternalTasks;
import br.com.itau.journey.rocksdb.configuration.RocksDBConfiguration;
import br.com.itau.journey.rocksdb.kv.exception.FindFailedException;
import br.com.itau.journey.rocksdb.kv.exception.SaveFailedException;
import br.com.itau.journey.rocksdb.mapper.exception.DeserializationException;
import br.com.itau.journey.rocksdb.mapper.exception.SerDeException;
import br.com.itau.journey.rocksdb.mapper.exception.SerializationException;
import br.com.itau.journey.rocksdb.repository.RocksDBKeyValueRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

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
}
