package com.stream.coupon.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stream.coupon.dto.LoginFormDTO;
import com.stream.coupon.dto.Result;
import com.stream.coupon.dto.UserDTO;
import com.stream.coupon.entity.User;
import com.stream.coupon.mapper.UserMapper;
import com.stream.coupon.service.IUserService;
import com.stream.coupon.utils.RegexUtils;
import com.stream.coupon.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.stream.coupon.utils.RedisConstants.*;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1. 校验手机号
        if(RegexUtils.isPhoneInvalid(phone)) {
            // 2. 如果不符合，返回错误信息
            return Result.fail("手机号格式错误！");
        }
        // 3. 符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4. 验证码保存到 Redis 中，并设置过期时间
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code,
                LOGIN_CODE_TTL, TimeUnit.MINUTES);
        log.debug("发送短信验证码成功，验证码：{}", code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号格式错误！");
        }
        // 从 Redis 获取验证码并校验
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        if(!loginForm.getCode().equals(cacheCode)){
            return Result.fail("验证码不正确！");
        }

        // 查询到用户数据
        User user = query().eq("phone", phone).one();
        // 不存在则插入新用户到数据库
        if(user == null){
            user = createUserWithPhone(phone);
        }

        // 部分用户信息（UserDTO，用户 ID，nickname, icon）保存到 Redis
        String token = UUID.randomUUID().toString(); // 生成全局唯一token
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create() // userDTO 中的 id 字段是 long，但userMap要求所有字段都是string，所以这里做一个处理
                        .setIgnoreNullValue(true) // 忽略空值
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString())); // 字段转换为string
        String tokenKey = LOGIN_USER_KEY + token; // 将全局唯一token加上固定前缀作为 key
        // 多个字段，用putAll
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 设置token有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 带着 token 返回
        return Result.ok(token);
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(6));
        // 保存新用户到数据库
        save(user);
        return user;
    }
}
