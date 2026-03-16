package com.his.domain;

import lombok.Data;

@Data
public class SmsRolePermissionRelation {

    private Long id;

    private Long roleId;

    private Long permissionId;

}
