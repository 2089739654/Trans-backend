package com.example.trans_backend_common.aop;

import com.example.trans_backend_common.enums.ResourceTypeEnum;

public interface ResourceCheckService {

    boolean doCheck(Long userId, ResourceTypeEnum resourceType,Long resourceId);

}
