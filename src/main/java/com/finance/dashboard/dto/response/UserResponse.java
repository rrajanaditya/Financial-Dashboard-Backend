package com.finance.dashboard.dto.response;

import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private UserStatus status;
}