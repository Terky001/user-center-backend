package com.terky.usercenter.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 3211993696550504653L;

    private String userAccount;
    private String userPassword;
}
