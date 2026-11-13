package com.unimatch.domain;

public class HiddenContact {
    private Long id;
    private StudentProfile owner;
    private StudentProfile contact;
    protected HiddenContact() { }

    public HiddenContact(StudentProfile owner, StudentProfile contact) { this.owner = owner; this.contact = contact; }

    public HiddenContact(Long id, StudentProfile owner, StudentProfile contact) { this(owner, contact); this.id = id; }

    public StudentProfile getOwner() { return owner; }

    public StudentProfile getContact() { return contact; }
}
