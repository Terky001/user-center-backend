package com.terky.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.terky.usercenter.common.ErrorCode;
import com.terky.usercenter.exception.BusinessException;
import com.terky.usercenter.model.User;
import com.terky.usercenter.service.UserService;
import com.terky.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.terky.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
* 针对表【user】的数据库操作Service实现
* 2023-01-01 22:22:04
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;

    private static final String SALT = "terky";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        //校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        if (userAccount.length() < 4)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度不足");
        if (userPassword.length() < 8 || checkPassword.length() < 8)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不足");
        if (planetCode.length() > 5)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编码不符");


        //账户不能包含特殊字符  正则
        String validPattern = "[\\\\u00A0\\\\s\\\"`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find())
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");;

        //密码和校验密码相同
        if (!userPassword.equals(checkPassword))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");;

        //账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        long count = this.count(queryWrapper);
        if (count > 0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");;

        //星球不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode",planetCode);
        count = this.count(queryWrapper);
        if (count > 0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");;

        //加密密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) //避免拆箱错误
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");;

        return user.getId();
    }

    /**
     * return 脱敏后的用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //校验
        if (StringUtils.isAnyBlank(userAccount, userPassword))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");;
        if (userAccount.length() < 4)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");;
        if (userPassword.length() < 8)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");;

        //账户不能包含特殊字符  正则
        String validPattern = "[\\\\u00A0\\\\s\\\"`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find())
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户不可以包含特殊字符");

        //加密密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if (user == null){
            log.info("user login failed, account cannot match password");
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
        }

        User safeUser = getSafeUser(user);

        //记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safeUser);

        //用户脱敏
        return safeUser;
    }

    @Override
    public User getSafeUser(User originUser) {
        if (originUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "无用户信息");
        }

        User safeUser = new User();
        safeUser.setId(originUser.getId());
        safeUser.setUsername(originUser.getUsername());
        safeUser.setUserAccount(originUser.getUserAccount());
        safeUser.setAvatarUrl(originUser.getAvatarUrl());
        safeUser.setGender(originUser.getGender());
        safeUser.setPhone(originUser.getPhone());
        safeUser.setEmail(originUser.getEmail());
        safeUser.setPlanetCode(originUser.getPlanetCode());
        safeUser.setTags(originUser.getTags());
        safeUser.setUserRole(originUser.getUserRole());
        safeUser.setUserStatus(0);
        safeUser.setCreateTime(originUser.getCreateTime());

        return safeUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        //删除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /*
     * 根据标签搜索用户
     */
    @Override
    public List<User> searchUsersByTag(List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

//        //sql
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        //拼接 and 查询
//        for (String tagName : tagNameList){
//            queryWrapper.like("tags",tagName);
//        }
//        List<User> userList = userMapper.selectList(queryWrapper);
//        return userList.stream().map(this::getSafeUser).collect(Collectors.toList());

        //内存
        //先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        //在内存中判断是否有符合要求的标签
        return userList.stream().filter(user -> {
                String tagStr = user.getTags();
                if (StringUtils.isBlank(tagStr)){
                    return false;
                }
                Set<String> tempTagNameSet = gson.fromJson(tagStr, new TypeToken<Set<String>>(){}.getType());
                tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
                for (String tagName : tagNameList){
                    if (!tempTagNameSet.contains(tagName)){
                        return false;
                    }
                }
                return true;

        }).map(this::getSafeUser).collect(Collectors.toList());

    }
}




