package com.example.trans_backend_file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.trans_backend_common.common.BaseResponse;
import com.example.trans_backend_common.context.BaseContext;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.ThrowUtils;
import com.example.trans_backend_file.mapper.TranslationPairsHistoryMapper;
import com.example.trans_backend_file.mapper.TranslationPairsMapper;
import com.example.trans_backend_file.model.entity.File;
import com.example.trans_backend_file.model.entity.TranslationPairs;
import com.example.trans_backend_file.model.entity.TranslationPairsHistory;
import com.example.trans_backend_file.model.vo.SelectTransPairsVo;
import com.example.trans_backend_file.service.FileService;
import com.example.trans_backend_file.service.TranslationPairsHistoryService;
import com.example.trans_backend_file.service.TranslationPairsService;
import org.apache.ibatis.executor.BatchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author 20897
* @description 针对表【translation_pairs】的数据库操作Service实现
* @createDate 2025-05-07 08:21:46
*/
@Service
public class TranslationPairsServiceImpl extends ServiceImpl<TranslationPairsMapper, TranslationPairs>
    implements TranslationPairsService{

    @Resource
    private FileService fileService;

    @Resource
    private TranslationPairsHistoryService translationPairsHistoryService;

    @Resource
    private TranslationPairsMapper translationPairsMapper;
    @Override
    public List<TranslationPairs> selectTransText(Integer position, Integer size, Integer currentPage,Long fileId) {
        if(position!=null){
            return translationPairsMapper.selectTransText(position, size,fileId);
        }
        ThrowUtils.throwIf(currentPage == null , ErrorCode.PARAMS_ERROR);
        QueryWrapper<TranslationPairs> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("file_id", fileId);
        queryWrapper.orderByAsc("position");
        Page<TranslationPairs> translationPairsPage=new Page<>(currentPage,size);
        Page<TranslationPairs> translationPairsPage1 = baseMapper.selectPage(translationPairsPage, queryWrapper);
        return translationPairsPage1.getRecords();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTransText(List<TranslationPairs> list) {
        if(list==null|| list.isEmpty())return;
        List<Long> idList = list.stream().map(TranslationPairs::getId).collect(Collectors.toList());
        List<TranslationPairs> translationPairs = selectAllById(idList);
        List<TranslationPairs> res=new ArrayList<>();
        List<TranslationPairsHistory> translationPairsHistoryList=new ArrayList<>();
        //旧数据
        Map<Long, TranslationPairs> map = translationPairs.stream().collect(Collectors.toMap(TranslationPairs::getId, translationPairs1 -> {return translationPairs1;}));
        //是否发生修改
        for (TranslationPairs val:list){
            TranslationPairs oldVal = map.get(val.getId());
            ThrowUtils.throwIf(oldVal == null, ErrorCode.PARAMS_ERROR, "TranslationPairs with id " + val.getId() + " does not exist");
            //如果发生修改则进行更新
            if(val.getTranslatedText()==null&&oldVal.getTranslatedText()==null){
                //如果都没有翻译文本则不进行更新
                continue;
            }
            if(val.getTranslatedText()==null||!val.getTranslatedText().equals(oldVal.getTranslatedText())){
                val.setVersion(oldVal.getVersion()+1);
                res.add(val);
                TranslationPairsHistory translationPairsHistory=new TranslationPairsHistory();
                translationPairsHistory.setTranslatedText(oldVal.getTranslatedText());
                translationPairsHistory.setTransId(val.getId());
                translationPairsHistory.setVersion(oldVal.getVersion());
                translationPairsHistoryList.add(translationPairsHistory);
            }
        }
        if(res.isEmpty())return;
        boolean res1 = updateBatchById(res, 100);
        boolean res2 = translationPairsHistoryService.saveBatch(translationPairsHistoryList, 500);
        ThrowUtils.throwIf(!res1||!res2, ErrorCode.OPERATION_ERROR,"批量更新失败");
    }

    @Override
    public List<TranslationPairs> selectAllOrderByPosition(Long fileId) {
        QueryWrapper<TranslationPairs> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("file_id",fileId);
        queryWrapper.orderByAsc("position");
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public Long getUserId(Long id) {
        return translationPairsMapper.getUserId(id);
    }

    @Override
    public List<TranslationPairs> selectAllById(List<Long> ids) {
        return translationPairsMapper.selectAllById(ids);
    }

    @Override
    public Integer getTransTextCount(Long fileId) {
        QueryWrapper<TranslationPairs> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("file_id", fileId);
        long count = baseMapper.selectCount(queryWrapper);
        return (int)count;
    }

    @Override
    public List<SelectTransPairsVo> getTransPairsByUserId(Long id) {
        return translationPairsMapper.selectAllByUserId(id);
    }

    @Override
    public void removeTransPairs(Long transId) {
        UpdateWrapper<TranslationPairs> updateWrapper=new UpdateWrapper<>();
        updateWrapper.set("is_new",0);
        updateWrapper.eq("id",transId);
        int update = baseMapper.update(null, updateWrapper);
        ThrowUtils.throwIf(update == 0, ErrorCode.OPERATION_ERROR, "删除翻译对失败");
    }


}




