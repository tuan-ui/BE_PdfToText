package com.noffice.enumType;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ActionType {
    DELETE("log.delete"),
    LOGOUT("log.logout"),
    LOGIN("log.login"),
    VIEW("log.view"),
    UPDATE("log.update"),
    CREATE("log.create"),
    LOCK("log.lock"),
    UNLOCK("log.unlock"),
    CHANGE_PASSWORD("log.changePassword"),
    DOWNLOAD("log.download"),;

    private final String action;

    ActionType(String action) {
        this.action = action;
    }
    public String getAction() {
        return action;
    }
    public static List<String> getAllActions() {
        return Arrays.stream(values())
                .map(ActionType::getAction)
                .collect(Collectors.toList());
    }

    public static ActionType fromAction(String action) {
        return Arrays.stream(values())
                .filter(type -> type.getAction().equalsIgnoreCase(action))
                .findFirst()
                .orElse(null);
    }
}
