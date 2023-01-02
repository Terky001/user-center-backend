package com.terky.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.terky.usercenter.model.User;
import com.terky.usercenter.service.UserService;
import com.terky.usercenter.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author www
* @description 针对表【user】的数据库操作Service实现
* @createDate 2023-01-01 22:22:04
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




