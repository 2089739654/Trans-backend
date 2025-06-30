package com.example.trans_backend_file.mybatisPlusHandler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Date;

@Component  // 注册为Spring Bean
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 插入时填充 createTime 和 updateTime
        this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
        this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时填充 updateTime
        this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
    }
}
