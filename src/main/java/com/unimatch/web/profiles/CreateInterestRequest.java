package com.unimatch.web.profiles;

import com.unimatch.domain.InterestType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreateInterestRequest {

    @NotBlank @Size(max = 80) private String name;
    @NotNull private InterestType type;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public InterestType getType() { return type; }
    public void setType(InterestType type) { this.type = type; }
}
