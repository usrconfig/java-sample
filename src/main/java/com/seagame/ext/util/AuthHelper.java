package com.seagame.ext.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.seagame.ext.entities.UserNFT;

import java.io.UnsupportedEncodingException;

/**
 * https://github.com/auth0/java-jwt
 *
 * @author LamHa
 */
public class AuthHelper {
    private static final String ISSUER = "auth0";
    private static final String SIGNING_KEY = "creants@^($%*$%";
    // expire trong 10 ngày
    private static final int TTL_MILI = 864000000;

    public static DecodedJWT verifyToken(String token) throws IllegalArgumentException, UnsupportedEncodingException {
        // cho phép trễ 1mili, giả sử set expire là 10mili thì 11mili mới expire
        return JWT.decode(token);
    }


    public static long getUserId(String token) {
        Claim claim = JWT.decode(token).getClaim("id");
        return Long.parseLong(claim.asString());
    }


    public static UserNFT getUser(String token) {
        UserNFT user = new UserNFT();
        DecodedJWT decode = JWT.decode(token);
        user.setId(Long.parseLong(decode.getClaim("id").asString()));
        return user;
    }

}
