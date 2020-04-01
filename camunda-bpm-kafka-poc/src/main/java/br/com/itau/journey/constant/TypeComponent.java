package br.com.itau.journey.constant;

public enum TypeComponent {

    SERVICE_TASK_EVENT("ServiceTask"),
    USER_TASK("UserTask"),
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
