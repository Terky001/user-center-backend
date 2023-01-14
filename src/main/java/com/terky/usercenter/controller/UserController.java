package com.terky.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.terky.usercenter.constant.UserConstant;
import com.terky.usercenter.model.User;
import com.terky.usercenter.model.UserLoginRequest;
import com.terky.usercenter.model.UserRegisterRequest;
import com.terky.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public Long userRegister(@RequestBody UserRegisterRequest userRegisterRequest){

        if (userRegisterRequest == null)
            return null;

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword,checkPassword))
            return null;

        return userService.userRegister(userAccount, userPassword, checkPassword);
    }

    @PostMapping("login")
    public User userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){

        if (userLoginRequest == null)
            return null;

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword))
            return null;

        return userService.userLogin(userAccount, userPassword,request);
    }

    @GetMapping("/search")
    public List<User> searchUsers(String username, HttpServletRequest request){

        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        if (user == null || user.getUserRole() != UserConstant.ADMIN_ROLE){
            return new ArrayList<>();
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNoneBlank(username)){
            queryWrapper.like("username",username);
        }
        return userService.list(queryWrapper);
    }

    @PostMapping("/delete")
    public boolean deleteUser(long id, HttpServletRequest request){

        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        if (user == null || user.getUserRole() != UserConstant.ADMIN_ROLE){
            return false;
        }
        if (id <= 0){
            return false;
        }
        return userService.removeById(id);
    }
}
