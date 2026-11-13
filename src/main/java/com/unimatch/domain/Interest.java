package com.unimatch.domain;

public class Interest {
    private Long id;
    private String name;
    private InterestType type;

    protected Interest() {
    }

    public Interest(String name, InterestType type) {
        this.name = name;
        this.type = type;
    }

    public Interest(Long id, String name, InterestType type) {
        this(name, type);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public InterestType getType() {
        return type;
    }
}
