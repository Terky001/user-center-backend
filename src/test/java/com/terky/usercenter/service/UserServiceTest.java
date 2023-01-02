package com.terky.usercenter.service;
import java.util.Date;

import com.terky.usercenter.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    void testAdduser(){
        User user = new User();
        user.setUsername("terky");
        user.setUserAccount("123");
        user.setAvatarUrl("https://image-assets.mihuashi.com/permanent/396993%7C-2022/01/14/22/FhNetbqKN9_nCtB1JN-ZtLkO6Q52.jpg");
        user.setGender(0);
        user.setUserPassword("456");
        user.setPhone("156");
        user.setEmail("489");

        boolean result = userService.save(user);
        System.out.println(user.getId());
        assertTrue(result);
    }
}