package br.com.itau.journey.constant;

public enum TypeComponent {

    SERVICE_TASK("ServiceTask"),
    USER_TASK("UserTask"),
    SIMPLE_TASK("SimpleTask"),
    START_EVENT("StartEvent"),
    END_EVENT("EndEvent");

    private String event;

    TypeComponent(String event) {

        this.event = event;
    }

    public String getEvent() {
        return event;
    }
}
