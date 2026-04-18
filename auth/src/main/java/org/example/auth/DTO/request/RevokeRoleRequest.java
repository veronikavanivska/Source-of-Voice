package org.example.auth.DTO.request;

import lombok.Data;
import org.example.auth.entities.RoleName;

@Data
public class RevokeRoleRequest {
    private Long userId;
    private RoleName roleName;
}
