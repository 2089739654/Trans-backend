package com.example.trans_backend_file.controller;

import com.example.trans_backend_common.aop.ResourceCheck;
import com.example.trans_backend_common.common.BaseResponse;
import com.example.trans_backend_common.common.ResultUtils;
import com.example.trans_backend_common.context.BaseContext;
import com.example.trans_backend_common.enums.ResourceTypeEnum;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.ThrowUtils;
import com.example.trans_backend_file.manager.TranslationManager;
import com.example.trans_backend_file.model.dto.*;
import com.example.trans_backend_file.model.entity.File;
import com.example.trans_backend_file.model.entity.TranslationPairs;
import com.example.trans_backend_file.model.entity.TranslationPairsHistory;
import com.example.trans_backend_file.model.vo.SelectTransPairsVo;
import com.example.trans_backend_file.service.ElasticsearchService;
import com.example.trans_backend_file.service.FileService;
import com.example.trans_backend_file.service.TranslationPairsHistoryService;
import com.example.trans_backend_file.service.TranslationPairsService;
import lombok.Getter;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class FileTranslationPairsController {


    @Resource
    private ElasticsearchService elasticsearchService;

    @Resource
    private TranslationPairsService translationPairsService;

    @Resource
    private TranslationPairsHistoryService translationPairsHistoryService;

    @Resource
    private TranslationManager translationManager;

    @Resource
    @Lazy
    private FileService fileService;


    @PostMapping("/getTransTextCount")
    public BaseResponse<Integer> getTransTextCount(Long fileId){
        ThrowUtils.throwIf(fileId == null, ErrorCode.PARAMS_ERROR, "fileId不能为空");
        Integer count = translationPairsService.getTransTextCount(fileId);
        ThrowUtils.throwIf(count == null, ErrorCode.SYSTEM_ERROR, "获取翻译文本数量失败");
        return ResultUtils.success(count);
    }



    @PostMapping("/getTransText")
    @ResourceCheck(checkResource = "#selectTransTextRequest.fileId",resourceType = ResourceTypeEnum.FILE)
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
        ThrowUtils.throwIf(saveTransTextRequest==null,ErrorCode.PARAMS_ERROR);
        translationPairsService.saveTransText(saveTransTextRequest.getList());
        return ResultUtils.success("更新成功");
    }


    @PostMapping("/translateText")
    @ResourceCheck(checkResource = "#translateRequest.fileId", resourceType = ResourceTypeEnum.FILE)
    public BaseResponse<String> translateText(@RequestBody TranslateRequest translateRequest){
        //check text todo maybe
        String res = translationManager.translate(translateRequest.getSourceText());
        ThrowUtils.throwIf(res==null,ErrorCode.SYSTEM_ERROR,"翻译失败");
        return ResultUtils.success(res);
    }

    @PostMapping("/searchTransPairs")
    public BaseResponse<List<TranslationPairs>> searchTransPairs(@RequestBody SearchTransTextRequest searchTransTextRequest){
        ThrowUtils.throwIf(searchTransTextRequest==null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(searchTransTextRequest.getText() == null, ErrorCode.PARAMS_ERROR, "text不能为空");
        List<File> files = fileService.selectByUserId(BaseContext.getUser().getId());
        List<Long> list = files.stream().map(File::getId).collect(Collectors.toList());
        List<TranslationPairs> search = elasticsearchService.search(searchTransTextRequest.getText(), list);
        Long transId = searchTransTextRequest.getTransId();
        List<TranslationPairs> collect = search.stream().filter(e -> !(e.getId()).equals(transId)).collect(Collectors.toList());
        return ResultUtils.success(collect);
    }

    @PostMapping("/getTranslationHistory")
    public BaseResponse<TranslationPairsHistory> getTranslationHistory(@RequestBody GetTranslationHistoryRequest getTranslationHistoryRequest){
        ThrowUtils.throwIf(getTranslationHistoryRequest==null, ErrorCode.PARAMS_ERROR);
        if(getTranslationHistoryRequest.getVersion()==0)return null;
        TranslationPairsHistory res = translationPairsHistoryService.getByIdAndVersion(getTranslationHistoryRequest.getTransId(), getTranslationHistoryRequest.getVersion());
        return ResultUtils.success(res);
    }

    @PostMapping ("/getTransPairs")
    public BaseResponse<List<SelectTransPairsVo>> getTransPairs(){
        Long id = BaseContext.getUser().getId();
        List<SelectTransPairsVo> list = translationPairsService.getTransPairsByUserId(id);
        ThrowUtils.throwIf(list == null, ErrorCode.SYSTEM_ERROR, "获取翻译对失败");
        return ResultUtils.success(list);
    }


    @PostMapping("/removeTransPairs")
    public void removeTransPairs(Long transId){
        ThrowUtils.throwIf(transId == null, ErrorCode.PARAMS_ERROR, "transId不能为空");
        translationPairsService.removeTransPairs(transId);

    }


}
