package com.example.trans_backend_admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.trans_backend_common.entity.User;
import com.example.trans_backend_admin.service.UserService;
import com.example.trans_backend_admin.mapper.UserMapper;
import com.example.trans_backend_common.enums.UserRoleEnum;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.ThrowUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
* @author 20897
* @description 针对表【user】的数据库操作Service实现
* @createDate 2025-04-16 22:19:15
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{


    private final String SALT ="salt";

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public User userRegister(String userAccount, String userPassword, String userName) {
        // 1. 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, userName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 ) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        QueryWrapper<User>queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        Long l = baseMapper.selectCount(queryWrapper);
        ThrowUtils.throwIf(l>0,ErrorCode.PARAMS_ERROR, "用户账号已存在");
        String encryptPassword = getEncryptPassword(userPassword);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName(userName);
        user.setUserRole(UserRoleEnum.USER.getValue());
        String token ="login:"+UUID.randomUUID().toString();
        user.setToken(token);
        int insert = baseMapper.insert(user);
        ThrowUtils.throwIf(insert <= 0, ErrorCode.SYSTEM_ERROR, "注册失败");
        //存redis
        redisTemplate.opsForValue().set(token,user, 60 * 60 * 24, TimeUnit.SECONDS);
        //脱敏处理
        user.setUserPassword("");
        return user;
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码错误");
        }
        // 2. 对用户传递的密码进行加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3. 查询数据库中的用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在");
        if(user.getToken()!=null&&redisTemplate.opsForValue().get(user.getToken())!=null){
            //续期token
            redisTemplate.opsForValue().set(user.getToken(),user, 60 * 60 * 24, TimeUnit.SECONDS);
        }else {
            //生成新的token
            String token ="login:"+UUID.randomUUID().toString();
            redisTemplate.opsForValue().set(token,user, 60 * 60 * 24, TimeUnit.SECONDS);
            user.setToken(token);
            baseMapper.update(user,new UpdateWrapper<User>().eq("id",user.getId()));
        }
        //脱敏处理
        user.setUserPassword("");
        return user;
    }


    public String getEncryptPassword(String userPassword) {
        // 加盐，混淆密码
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }


}




