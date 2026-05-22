package com.interview.ordersystem.user;

import com.interview.ordersystem.auth.Role;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private Instant createdAt;
}
