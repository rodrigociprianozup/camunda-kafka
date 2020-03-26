package br.com.itau.journey.service;

import br.com.itau.journey.dto.KSQLRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

@Service
@Slf4j
public class KSQLInstanceService {

    public ResponseEntity<String> getProcessInstance(String ksql) throws URISyntaxException {
        URI uri = new URI("http://localhost:8088/query");

        log.info(":: 3 - URL Request {}", uri);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", "application/json");
        httpHeaders.add("Accept", MediaType.APPLICATION_JSON.toString());

        log.info(":: 4 - Headers {}", httpHeaders);

        HashMap<String, String> props = new HashMap<>();
        props.put("ksql.streams.auto.offset.reset", "earliest");

        log.info(":: 5 - Properties {}", props.toString());

        KSQLRequest request = new KSQLRequest(ksql, props);

        HttpEntity httpEntity = new HttpEntity(request, httpHeaders);

        log.info(":: 6 - HttpEntity {}", httpEntity.toString());

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> result = restTemplate.postForEntity(uri, httpEntity, String.class);

        log.info(":: 7 - Result Query KSQL {}", result);

        return result;
    }



}
