package com.stream.coupon.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.stream.coupon.dto.UserDTO;
import com.stream.coupon.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
@Component
@Slf4j
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 生成 JWT
     * @param user
     * @return
     */
    public String generateJwtToken(User user) {
        log.info("secret: {}", secret);
        // UserDTO 转换为 Map
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create() // userDTO 中的 id 字段是 long，但userMap要求所有字段都是string，所以这里做一个处理
                        .setIgnoreNullValue(true) // 忽略空值
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        return Jwts.builder()
                .setClaims(userMap)
                .setSubject(user.getNickName())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * 验证 JWT
     * @param token
     * @return
     */
    public Boolean validateJwtToken(String token) {
        log.info("secret: {}", secret);
        // 1. 校验 JWT 是否被篡改
        // 2. 检查令牌是否过期
        try {
            Date expiredDate = Jwts.parserBuilder()
                            .setSigningKey(secret)
                            .build()
                            .parseClaimsJws(token)
                            .getBody()
                            .getExpiration();
            log.info("JWT expiredDate: {}", expiredDate);
            return expiredDate.after(new Date()); // 解析成功，检查时间
        } catch (Exception e) {
            log.error("JWT token is invalid: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取 JWT 中的 UserDTO
     * @param token
     * @return
     */
    public UserDTO getUserFromJwtToken(String token) {
        Map<String, Object> claims = Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return BeanUtil.fillBeanWithMap(claims, new UserDTO(), true);
    }

}
