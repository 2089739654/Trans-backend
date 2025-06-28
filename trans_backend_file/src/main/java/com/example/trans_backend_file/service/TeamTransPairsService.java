package com.example.trans_backend_file.service;

import com.example.trans_backend_file.model.entity.TeamTransPairs;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 20897
* @description 针对表【team_trans_pairs】的数据库操作Service
* @createDate 2025-06-28 09:56:04
*/
public interface TeamTransPairsService extends IService<TeamTransPairs> {

    List<TeamTransPairs> getTransPairs(Integer size, Integer page,Long fileId);

}
