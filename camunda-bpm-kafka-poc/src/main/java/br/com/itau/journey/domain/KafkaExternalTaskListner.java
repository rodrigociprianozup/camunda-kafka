package br.com.itau.journey.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class KafkaExternalTaskListner {

    private Number rowTime;
    private String rowKey;
    private KafkaExternalTask payload;


}
