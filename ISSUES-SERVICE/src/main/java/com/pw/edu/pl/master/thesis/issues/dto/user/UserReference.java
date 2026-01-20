package com.pw.edu.pl.master.thesis.issues.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public  class UserReference {
    private String id;
    private String username;

    public static UserReference of(String id, String username) {
        return new UserReference(id, username);
    }

}