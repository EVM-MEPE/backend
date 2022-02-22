package com.propwave.daotool.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

@Service
public class SecurityService {
    // 로그인 서비스할때 같이 사용
    String createToken(String subject, long expTime){
        // 만료시간 예외처리
        if(expTime<=0){
            throw new RuntimeException("expectTime should be longer than zero");
        }

        // 서명 알고리즘 지정
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        //string 형태의 키를 byte로 만들어줌!
        byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(Secret.JWT_SECRET_KEY);
        //key 만들어주기 -> 암호화 키를 만들어주는 과정인 것 같음! 키가 뙇 만들어짐
        Key signingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());

        // builder pattern 공부하면 좋을듯
        // subject는 아마 id가 되곤함. 그리고 비밀번호가 secret key가 되곤함. 참고!
        // sign with + key, algorithm
        // String type으로 리턴이 됨
        return Jwts.builder()
                .setSubject(subject)
                .signWith(signingKey, signatureAlgorithm)
                .setExpiration(new Date(System.currentTimeMillis() + expTime))
                .compact();
    }

    // token 을 꺼내와서 점검해주는 친구.
    //원래는 boolean으로 인증이 잘 되어있는지 여부를 확인해주는 method로 변경하면 됨!
    //토큰 검증해주는 메서드를 불리언으로다가?
    public String getSubject(String token){
        //claim: payloader에 담긴 정보
        //claim에서 서브젝트만 꺼내오면 됨.
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(DatatypeConverter.parseBase64Binary(Secret.JWT_SECRET_KEY))
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
