package com.his.enums;

public enum RegistrationStatusCode {
    UNFINISHED(0), FINISHED(1), CANCELED(2);
    private final int code;
    RegistrationStatusCode(int code) { this.code = code; }
    public int getCode() { return code; }
}
