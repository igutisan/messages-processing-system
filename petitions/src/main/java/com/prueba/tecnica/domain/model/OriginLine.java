package com.prueba.tecnica.domain.model;

import java.util.UUID;

public class OriginLine {

    private UUID id;
    private String phoneNumber;

    public OriginLine() {
    }

    public OriginLine(UUID id, String phoneNumber) {
        this.id = id;
        this.phoneNumber = phoneNumber;
    }

    public static OriginLine create(String phoneNumber) {
        OriginLine originLine = new OriginLine();
        originLine.id = UUID.randomUUID();
        originLine.phoneNumber = phoneNumber;
        return originLine;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
