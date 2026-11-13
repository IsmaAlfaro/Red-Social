package com.unimatch.web.discovery;

public class LikeResponse {

    private final boolean mutual;

    public LikeResponse(boolean mutual) {
        this.mutual = mutual;
    }

    public boolean isMutual() {
        return mutual;
    }
}
