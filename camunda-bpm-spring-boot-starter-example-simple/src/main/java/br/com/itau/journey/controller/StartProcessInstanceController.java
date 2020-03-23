package br.com.itau.journey.controller;

import br.com.itau.journey.dto.KSQLRequest;
import br.com.itau.journey.dto.RequestStartDTO;
import br.com.itau.journey.service.KSQLInstanceService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.Consumes;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

@RestController
@RequestMapping("start")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StartProcessInstanceController {

    private final RuntimeService runtimeService;

    private final KSQLInstanceService ksqlInstanceService;

    @PostMapping
    public String start(@RequestBody RequestStartDTO requestStart) throws InterruptedException, URISyntaxException {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(requestStart.getBpmnInstance());

        waitingProcessEnd(processInstance);

        return processInstance.getProcessInstanceId();
    }

    private void waitingProcessEnd(ProcessInstance processInstance) throws InterruptedException, URISyntaxException {
        boolean x = true;
        while (x) {
            Thread.sleep(5000);
            ResponseEntity<String> result = ksqlInstanceService.getProcessInstanceId(processInstance.getId());
            x = result.getBody() != null && !result.getBody().isEmpty();
        }
    }
}

