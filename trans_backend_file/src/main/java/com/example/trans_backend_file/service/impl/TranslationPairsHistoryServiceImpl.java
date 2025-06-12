package com.example.trans_backend_file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.trans_backend_file.model.entity.TranslationPairsHistory;
import com.example.trans_backend_file.service.TranslationPairsHistoryService;
import com.example.trans_backend_file.mapper.TranslationPairsHistoryMapper;
import org.springframework.stereotype.Service;

/**
* @author 20897
* @description 针对表【translation_pairs_history】的数据库操作Service实现
* @createDate 2025-05-27 19:03:11
*/
@Service
public class TranslationPairsHistoryServiceImpl extends ServiceImpl<TranslationPairsHistoryMapper, TranslationPairsHistory>
    implements TranslationPairsHistoryService{

    @Override
    public TranslationPairsHistory getByIdAndVersion(Long id, Integer version) {
        QueryWrapper<TranslationPairsHistory> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("trans_id", id)
                    .eq("version", version-1);
        return baseMapper.selectOne(queryWrapper);
    }
}




