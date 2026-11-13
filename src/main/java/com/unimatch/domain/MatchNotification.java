package com.unimatch.domain;

public class MatchNotification {
    private Long id;
    private StudentProfile owner;
    private StudentProfile match;
    private boolean seen;

    protected MatchNotification() { }

    public MatchNotification(StudentProfile owner, StudentProfile match) { this.owner = owner; this.match = match; }

    public MatchNotification(Long id, StudentProfile owner, StudentProfile match, boolean seen) { this(owner, match); this.id = id; this.seen = seen; }

    public Long getId() { return id; }

    public StudentProfile getOwner() { return owner; }

    public StudentProfile getMatch() { return match; }

    public boolean isSeen() { return seen; }

    /** Marca la notificacion como vista para que no vuelva a entregarse. */
    public void markSeen() { this.seen = true; }
}
