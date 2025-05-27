package com.example.trans_backend_file.service.impl;

import com.aspose.pdf.internal.imaging.internal.bouncycastle.jcajce.provider.symmetric.AES;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.ThrowUtils;
import com.example.trans_backend_file.model.entity.SysRecord;
import com.example.trans_backend_file.service.SysRecordService;
import com.example.trans_backend_file.mapper.SysRecordMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 20897
* @description 针对表【sys_record】的数据库操作Service实现
* @createDate 2025-04-29 21:05:14
*/
@Service
public class SysRecordServiceImpl extends ServiceImpl<SysRecordMapper, SysRecord>
    implements SysRecordService{

    @Override
    public int add(SysRecord sysRecord) {
        return baseMapper.insert(sysRecord);
    }

    @Override
    public void error(Long id) {
        UpdateWrapper<SysRecord> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("id",id);
        updateWrapper.setSql("error = error + 1");
        updateWrapper.le("error",3);
        int update = baseMapper.update(new SysRecord(), updateWrapper);
        ThrowUtils.throwIf(update==0, ErrorCode.SYSTEM_ERROR,"sys_record表更新失败");
    }

    @Override
    public void success(Long id) {
        UpdateWrapper<SysRecord> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("id",id);
        updateWrapper.set("status",1);
        int update = baseMapper.update(new SysRecord(),updateWrapper);
        ThrowUtils.throwIf(update==0, ErrorCode.SYSTEM_ERROR,"sys_record表更新失败");
    }

    @Override
    public Integer select(Long id){
        QueryWrapper<SysRecord> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("error");
        queryWrapper.eq("id",id);
        SysRecord sysRecord = baseMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(sysRecord.getError()==null, ErrorCode.SYSTEM_ERROR,"sys_record表查询失败");
        return sysRecord.getError();
    }

    @Override
    public List<SysRecord> getAll() {
        QueryWrapper<SysRecord> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("status",0);
        queryWrapper.lt("error",3);
        return baseMapper.selectList(queryWrapper);
    }
}




