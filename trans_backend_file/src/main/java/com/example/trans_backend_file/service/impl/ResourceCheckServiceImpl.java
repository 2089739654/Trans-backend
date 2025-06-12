package com.example.trans_backend_file.service.impl;

import com.example.trans_backend_common.aop.ResourceCheckService;
import com.example.trans_backend_common.context.BaseContext;
import com.example.trans_backend_common.entity.User;
import com.example.trans_backend_common.enums.ResourceTypeEnum;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_file.model.entity.File;
import com.example.trans_backend_file.model.entity.Project;
import com.example.trans_backend_file.model.entity.TranslationPairs;
import com.example.trans_backend_file.service.FileService;
import com.example.trans_backend_file.service.ProjectService;
import com.example.trans_backend_file.service.TranslationPairsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class ResourceCheckServiceImpl implements ResourceCheckService {

    @Resource
    private FileService fileService;

    @Resource
    private ProjectService projectService;

    @Resource
    private TranslationPairsService translationPairsService;


    @Override
    public boolean doCheck(Long userId, ResourceTypeEnum resourceTypeEnum, Long resourceId) {
        User user= BaseContext.getUser();
        switch (resourceTypeEnum) {
            case FILE:
                File file = fileService.getById(resourceId);
                if (file == null) {
                    log.error("文件不存在" + resourceId);
                    throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
                }
                if (!(file.getUserId()).equals(user.getId())) {
                    log.error("没有权限" + resourceId);
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限");
                }
                break;
            case TRANSLATION_PAIRS:
                TranslationPairs translationPairs = translationPairsService.getById(resourceId);
                if (translationPairs == null) {
                    log.error("翻译对不存在" + resourceId);
                    throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "翻译对不存在");
                }
                if (!translationPairsService.getUserId(resourceId).equals(user.getId())) {
                    log.error("没有权限" + resourceId);
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限");
                }
                break;
            case PROJECT:
                Project project = projectService.getById(resourceId);
                if (project == null) {
                    log.error("项目不存在" + resourceId);
                    throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "项目不存在");
                }
                if (!(project.getUserId()).equals(user.getId())) {
                    log.error("没有权限" + resourceId);
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限");
                }
                break;
            default:
                log.error("资源类型错误" + resourceTypeEnum);
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "资源类型错误");
        }
        return true;
    }
}
