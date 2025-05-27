package com.example.trans_backend_file.controller;

import com.example.trans_backend_common.common.BaseResponse;
import com.example.trans_backend_common.common.ResultUtils;
import com.example.trans_backend_common.enums.ResourceTypeEnum;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.ThrowUtils;
import com.example.trans_backend_file.aop.ResourceCheck;
import com.example.trans_backend_file.manager.TranslationManager;
import com.example.trans_backend_file.model.dto.SaveTransTextRequest;
import com.example.trans_backend_file.model.dto.SelectTransTextRequest;
import com.example.trans_backend_file.model.entity.TranslationPairs;
import com.example.trans_backend_file.service.TranslationPairsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class FileTranslationPairsController {


    @Resource
    private TranslationPairsService translationPairsService;

    @Resource
    private TranslationManager translationManager;

    @PostMapping("/getTransText")
    public BaseResponse<List<TranslationPairs>> getTransText(@RequestBody SelectTransTextRequest selectTransTextRequest){
        ThrowUtils.throwIf(selectTransTextRequest == null, ErrorCode.PARAMS_ERROR);
        Long fileId = selectTransTextRequest.getFileId();
        Integer size = selectTransTextRequest.getSize();
        ThrowUtils.throwIf(fileId == null || size == null, ErrorCode.PARAMS_ERROR);
        List<TranslationPairs> translationPairs = translationPairsService.selectTransText(selectTransTextRequest.getPosition(),
                selectTransTextRequest.getSize(),
                selectTransTextRequest.getCurrentPage(),
                selectTransTextRequest.getFileId());
        return ResultUtils.success(translationPairs);
    }

    @PostMapping("/saveTransText")
    public BaseResponse<String> saveTransText(@RequestBody SaveTransTextRequest saveTransTextRequest){
        translationPairsService.saveTransText(saveTransTextRequest.getList());
        return ResultUtils.success("更新成功");
    }


    @PostMapping("/translateText")
    public BaseResponse<String> translateText(String text){
        String res = translationManager.translate(text);
        ThrowUtils.throwIf(res==null,ErrorCode.SYSTEM_ERROR,"翻译失败");
        return ResultUtils.success(res);
    }

//    @PostMapping("/searchTransPairs")
//    public BaseResponse<TranslationPairs> searchTransPairs(String text){
//
//    }




}
