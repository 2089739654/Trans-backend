package com.example.trans_backend_file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.trans_backend_common.common.BaseResponse;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.ThrowUtils;
import com.example.trans_backend_file.mapper.TranslationPairsMapper;
import com.example.trans_backend_file.model.entity.TranslationPairs;
import com.example.trans_backend_file.service.TranslationPairsService;
import org.apache.ibatis.executor.BatchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
* @author 20897
* @description 针对表【translation_pairs】的数据库操作Service实现
* @createDate 2025-05-07 08:21:46
*/
@Service
public class TranslationPairsServiceImpl extends ServiceImpl<TranslationPairsMapper, TranslationPairs>
    implements TranslationPairsService{


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
        Page<TranslationPairs> translationPairsPage=new Page<>(currentPage,size);
        Page<TranslationPairs> translationPairsPage1 = baseMapper.selectPage(translationPairsPage, queryWrapper);
        return translationPairsPage1.getRecords();
    }

    @Override
    public void saveTransText(List<TranslationPairs> list) {
        boolean res = updateBatchById(list, 500);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR,"批量更新失败");
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


}




