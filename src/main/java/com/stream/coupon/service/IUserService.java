package com.stream.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stream.coupon.dto.LoginFormDTO;
import com.stream.coupon.dto.Result;
import com.stream.coupon.entity.User;

import javax.servlet.http.HttpSession;

public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);
}
