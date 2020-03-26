package br.com.itau.journey.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class KafkaExternalTask {

    private String type;
    private String processInstanceId;
    private String taskId;
    private boolean taskComplete;
    private String activityInstanceId;
    private String currentActivityId;
    private Object infoUserTask;

}
