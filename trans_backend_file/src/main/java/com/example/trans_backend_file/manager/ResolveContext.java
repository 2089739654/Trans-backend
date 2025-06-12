package com.example.trans_backend_file.manager;

import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_file.enums.FileExtension;
import com.example.trans_backend_file.manager.resolve.impl.ResolveManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ResolveContext implements ApplicationListener<ContextRefreshedEvent> {


    private final ConcurrentHashMap<String, ResolveManager> resolveServiceMap = new ConcurrentHashMap<>(8);



    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        Map<String, ResolveManager> beansOfType = applicationContext.getBeansOfType(ResolveManager.class);
        resolveServiceMap.putAll(beansOfType);
    }

    public ResolveManager getResolveService(String type) {
        FileExtension fileExtension = FileExtension.getByExtension(type);
        if(fileExtension==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"不支持的文件类型");
        }
        ResolveManager resolveService = resolveServiceMap.get(fileExtension.getResolver());
        if (resolveService == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"没有对应的解析服务");
        }
        return resolveService;
    }
}
