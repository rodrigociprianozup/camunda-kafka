package br.com.itau.journey.dto;

import java.util.Map;

public class KSQLRequest {
    private String ksql;
    private Map<String, String> streamsProperties;

    public KSQLRequest() {
    }

    public KSQLRequest(String ksql, Map<String, String> streamsProperties) {
        this.ksql = ksql;
        this.streamsProperties = streamsProperties;
    }

    public String getKsql() {
        return ksql;
    }

    public void setKsql(String ksql) {
        this.ksql = ksql;
    }

    public Map<String, String> getStreamsProperties() {
        return streamsProperties;
    }

    public void setStreamsProperties(Map<String, String> streamsProperties) {
        this.streamsProperties = streamsProperties;
    }

    @Override
    public String toString() {
        return "KSQLRequest{" +
                "ksql='" + ksql + '\'' +
                ", streamsProperties=" + streamsProperties +
                '}';
    }
}