package com.terky.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.terky.usercenter.common.BaseResponse;
import com.terky.usercenter.common.ErrorCode;
import com.terky.usercenter.common.ResultUtils;
import com.terky.usercenter.constant.UserConstant;
import com.terky.usercenter.exception.BusinessException;
import com.terky.usercenter.model.User;
import com.terky.usercenter.model.UserLoginRequest;
import com.terky.usercenter.model.UserRegisterRequest;
import com.terky.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){

        if (userRegisterRequest == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");;

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();

        if (StringUtils.isAnyBlank(userAccount, userPassword,checkPassword,planetCode))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");;

        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);

        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        long userid = currentUser.getId();
        User user = userService.getById(userid);
        User safeUser = userService.getSafeUser(user);

        return ResultUtils.success(safeUser);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){

        if (userLoginRequest == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");;

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");;

        User user =  userService.userLogin(userAccount, userPassword,request);

        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){
        if (request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        System.out.println("no error");
        int result = userService.userLogout(request);

        return ResultUtils.success(result);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request){

        if (!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNoneBlank(username)){
            queryWrapper.like("username",username);
        }
        List<User> userList =  userService.list(queryWrapper);

        List<User> list =  userList.stream().map(user -> userService.getSafeUser(user)).collect(Collectors.toList());

        return ResultUtils.success(list);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(long id, HttpServletRequest request){

        if (!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        if (id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        boolean result =  userService.removeById(id);

        return ResultUtils.success(result);
    }

    private boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == UserConstant.ADMIN_ROLE;
    }

}
