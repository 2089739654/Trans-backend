package com.example.trans_backend_file.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.trans_backend_file.model.entity.TeamTransPairs;
import com.example.trans_backend_file.service.TeamTransPairsService;
import com.example.trans_backend_file.mapper.TeamTransPairsMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
* @author 20897
* @description 针对表【team_trans_pairs】的数据库操作Service实现
* @createDate 2025-06-28 09:56:04
*/
@Service
public class TeamTransPairsServiceImpl extends ServiceImpl<TeamTransPairsMapper, TeamTransPairs>
    implements TeamTransPairsService{


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<TeamTransPairs> getTransPairs(Integer size, Integer page, Long fileId) {
        int start = (page - 1) * size;
        int end = start + size;
        List<Object> list=new ArrayList<>();
        // 生成位置列表
        for (int i = start+1; i <= end ; i++) {
            list.add(i);
        }
        String key="file:"+fileId;
        List<Object> objects = stringRedisTemplate.opsForHash().multiGet(key, list);
        if(objects.isEmpty()){
            QueryWrapper<TeamTransPairs> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("file_id", fileId);
            queryWrapper.orderByAsc("position");
            Page<TeamTransPairs> pageResult = new Page<>( page, size);
            Page<TeamTransPairs> teamTransPairsPage = baseMapper.selectPage(pageResult, queryWrapper);
            List<TeamTransPairs> records = teamTransPairsPage.getRecords();
            if (records.isEmpty())return null;
            // 将查询结果存入Redis
            stringRedisTemplate.opsForHash().putAll(key, records.stream().collect(Collectors.toMap(TeamTransPairs::getPosition, JSONUtil::toJsonStr)));
            return records;
        }
        return objects.stream().map(
                obj -> JSONUtil.toBean((String) obj, TeamTransPairs.class)
        ).sorted(Comparator.comparingInt(TeamTransPairs::getPosition)).collect(Collectors.toList());
    }
}





