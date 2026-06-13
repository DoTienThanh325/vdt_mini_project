package com.vdt.documenttransfer.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAssignmentUpdateRequest {
    private Integer roleId;

    private Integer organizationId;
}
