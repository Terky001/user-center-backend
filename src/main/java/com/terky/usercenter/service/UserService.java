package com.terky.usercenter.service;

import com.terky.usercenter.model.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author www
* @description 针对表【user】的数据库操作Service
* @createDate 2023-01-01 22:22:04
*/
public interface UserService extends IService<User> {



    /**
     * 用户注册
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getSafeUser(User user);

    int userLogout(HttpServletRequest request);
}
