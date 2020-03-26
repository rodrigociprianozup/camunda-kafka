package br.com.itau.journey.controller;

import br.com.itau.journey.dto.RequestStartDTO;
import br.com.itau.journey.rocksdb.RocksDBKeyValueService;
import br.com.itau.journey.rocksdb.kv.exception.FindFailedException;
import br.com.itau.journey.rocksdb.kv.exception.SaveFailedException;
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
import java.net.URISyntaxException;
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
    public ResponseEntity<String> start(@RequestBody RequestStartDTO requestStart) throws InterruptedException, URISyntaxException, IOException {
        final String processInstanceId = processInstanceService.startProcessInstance(requestStart.getBpmnInstance());
        log.info(":: 1 - Created instance with id {}", processInstanceId);

        String result = waitingProcessEnd(processInstanceId);

        log.info(":: 2 - Result of Streams {}", result);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{processInstanceId}/complete/{taskId}")
    public ResponseEntity<String> complete(@PathVariable String processInstanceId, @PathVariable String taskId) throws URISyntaxException, InterruptedException, IOException, SaveFailedException {
        processInstanceService.completeTask(taskId);
        rocksDBKeyValueService.setCompleteTask(taskId, processInstanceId);
        log.info(":: 1 - Complete task id {}", taskId);

        String result = waitingProcessEnd(processInstanceId);

        log.info(":: 2 - Result of Streams {}", result);

        return ResponseEntity.ok(result);
    }

    private String waitingProcessEnd(String processInstanceId) throws InterruptedException, URISyntaxException, IOException {
        boolean x = true;
        Optional<String> result = null;
        while (x) {
            result = rocksDBKeyValueService.getProcessInstance(processInstanceId);
            x = !result.isPresent();
        }
        return result.get();
    }

    @GetMapping("/get/{processInstanceId}")
    @Produces("application/json")
    public ResponseEntity<String> get(@PathVariable String processInstanceId) throws URISyntaxException, FindFailedException, SerDeException {
        Optional<String> byKey = rocksDBKeyValueService.findByKey(processInstanceId);
        return ResponseEntity.ok(byKey.get());
    }

    @GetMapping("/get")
    @Produces("application/json")
    public ResponseEntity<String> get() throws URISyntaxException, FindFailedException, SerDeException {
        Collection<String> values = rocksDBKeyValueService.findAll();
        return ResponseEntity.ok(values.toString());
    }
}

