package br.com.itau.journey.controller;

import br.com.itau.journey.dto.KSQLRequest;
import br.com.itau.journey.dto.RequestStartDTO;
import br.com.itau.journey.service.KSQLInstanceService;
import br.com.itau.journey.service.ProcessInstanceService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import sun.net.www.http.HttpClient;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

@RestController
@RequestMapping("instance")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProcessInstanceController {

    private final ProcessInstanceService processInstanceService;

    @PostMapping("/start")
    @Consumes("application/json")
    @Produces("application/json")
    public ResponseEntity<String> start(@RequestBody RequestStartDTO requestStart) throws InterruptedException, URISyntaxException {
        final String processInstanceId = processInstanceService.startProcessInstance(requestStart.getBpmnInstance());

        waitingProcessEnd(processInstanceId);

        return ResponseEntity.ok(processInstanceId);
    }

    @PostMapping("/complete")
    public ResponseEntity<String> start(@RequestBody String taskId) {
        processInstanceService.completeTask(taskId);
        return ResponseEntity.ok("Complete!");
    }

    private void waitingProcessEnd(String processInstanceId) throws InterruptedException, URISyntaxException {
        boolean x = true;
        while (x) {
            ResponseEntity<String> result = processInstanceService.getProcessInstanceId(processInstanceId);
            x = result.getBody() != null && !result.getBody().isEmpty();
        }
    }

    @PostMapping("/query")
    @Consumes("application/json")
    public String query() throws URISyntaxException {
        URI uri = new URI("http://localhost:8088/query");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", "application/json");
        httpHeaders.add("Accept", MediaType.APPLICATION_JSON.toString());

        System.out.println(uri.getHost());

        HashMap<String, String> prop = new HashMap<>();
        prop.put("ksql.streams.auto.offset.reset", "earliest");

        String ksql = "select processInstanceId from END_PROCESS_STREAM where processInstanceId = 'f00cbf0f-6d11-11ea-bbab-964a154991e9' LIMIT 1;";
        KSQLRequest request = new KSQLRequest(ksql, prop);

        HttpEntity httpEntity = new HttpEntity(request, httpHeaders);

        RestTemplate restTemplate = new RestTemplate();
        System.out.println("json text====" + request.toString());

        ResponseEntity<String> result = restTemplate.postForEntity(uri, httpEntity, String.class);

        return result.getBody();
    }
}

