package br.com.itau.journey.controller;

import br.com.itau.journey.dto.RequestStartDTO;
import br.com.itau.journey.rocksdb.RocksDBKeyValueService;
import br.com.itau.journey.rocksdb.kv.exception.FindFailedException;
import br.com.itau.journey.rocksdb.mapper.exception.SerDeException;
import br.com.itau.journey.service.ProcessInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("instance/rocksDB")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ProcessInstanceDBKeyValueController {


    private final ProcessInstanceService processInstanceService;
    private final RocksDBKeyValueService rocksDBKeyValueService;

    @PostMapping("/start")
    @Consumes("application/json")
    @Produces("application/json")
    public ResponseEntity<String> start(@RequestBody RequestStartDTO requestStart) throws IOException, FindFailedException {
        Optional<String> processInstanceId = processInstanceService.getProcessingInstance(requestStart.getCpf());

        boolean instanceProcessing = false;
        if (processInstanceId.isPresent()) {
            instanceProcessing = !rocksDBKeyValueService.isEndEvent(processInstanceId.get());
        }

        if (instanceProcessing && !requestStart.isNewProposal()) {
            log.info(":: 1 - Created instance with processInstanceId {}", processInstanceId);
            String result = waitingProcessEnd(processInstanceId.get());
            log.info(":: 2 - Result of Streams {}", result);
            return ResponseEntity.ok(result);
        }

        String uuid = processInstanceService.start(requestStart);
        log.info(":: 1 - Created instance with id execution {}", uuid);
        String result = waitingProcessEndByUUID(uuid);
        log.info(":: 2 - Result of Streams {}", result);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<String> complete(@PathVariable String taskId) throws IOException, FindFailedException {
        String processInstanceId = processInstanceService.completeTask(taskId);

        log.info(":: 1 - Complete task id {}", taskId);

        String result = waitingProcessEnd(processInstanceId, taskId);

        log.info(":: 2 - Result of Streams {}", result);

        return ResponseEntity.ok(result);
    }

    private String waitingProcessEndByUUID(String uuid) throws IOException, FindFailedException {
        boolean loop = true;
        Optional<String> result = null;
        while (loop) {
            Optional<String> processInstanceId = rocksDBKeyValueService.findByKey(uuid);
            if (!processInstanceId.isPresent()) {
                continue;
            }
            result = Optional.of(this.waitingProcessEnd(processInstanceId.get()));
            loop = !result.isPresent();
        }
        return result.get();
    }

    private String waitingProcessEnd(String processInstanceId) throws IOException {
        return this.waitingProcessEnd(processInstanceId, null);
    }

    private String waitingProcessEnd(String processInstanceId, String taskId) throws IOException {
        boolean loop = true;
        Optional<String> result = null;
        while (loop) {
            result = rocksDBKeyValueService.getProcessInstance(processInstanceId, taskId);
            loop = !result.isPresent();
        }
        return result.get();
    }

    @GetMapping("/get/{processInstanceId}")
    @Produces("application/json")
    public ResponseEntity<String> get(@PathVariable String processInstanceId) throws FindFailedException, SerDeException {
        Optional<String> byKey = rocksDBKeyValueService.findByKey(processInstanceId);
        return ResponseEntity.ok(byKey.get());
    }

    @GetMapping("/get")
    @Produces("application/json")
    public ResponseEntity<String> get() throws SerDeException {
        Collection<String> values = rocksDBKeyValueService.findAll();
        return ResponseEntity.ok(values.toString());
    }
}

