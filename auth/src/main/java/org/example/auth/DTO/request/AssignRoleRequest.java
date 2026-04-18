package org.example.auth.DTO.request;

import lombok.Data;
import org.example.auth.entities.RoleName;
import org.springframework.context.support.BeanDefinitionDsl;

@Data
public class AssignRoleRequest {
    private Long userId;
    private RoleName roleName;
}
