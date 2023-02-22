package com.terky.usercenter.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -7709685220048121479L;

    private String userAccount;
    private String userPassword;
    private String checkPassword;

    private String planetCode;

}
