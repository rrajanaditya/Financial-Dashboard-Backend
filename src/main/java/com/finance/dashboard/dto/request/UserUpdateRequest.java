package com.finance.dashboard.dto.request;

import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.UserStatus;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private Role role;
    private UserStatus status;
}