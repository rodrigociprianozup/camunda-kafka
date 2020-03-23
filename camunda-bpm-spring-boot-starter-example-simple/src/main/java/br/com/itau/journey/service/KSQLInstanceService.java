package br.com.itau.journey.service;

import br.com.itau.journey.dto.KSQLRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

@Service
public class KSQLInstanceService {

    public ResponseEntity<String> getProcessInstanceId(String instanceId) throws URISyntaxException {
        URI uri = new URI("http://localhost:8088/query");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", "application/json");
        httpHeaders.add("Accept", MediaType.APPLICATION_JSON.toString());

        System.out.println(uri.getHost());

        HashMap<String, String> prop = new HashMap<>();
        prop.put("ksql.streams.auto.offset.reset", "earliest");

        String ksql = "select processInstanceId from END_PROCESS " +
                " where processInstanceId = '" + instanceId + "' LIMIT 1;";
        KSQLRequest request = new KSQLRequest(ksql, prop);

        HttpEntity httpEntity = new HttpEntity(request, httpHeaders);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> result = restTemplate.postForEntity(uri, httpEntity, String.class);

        return result;
    }



}
