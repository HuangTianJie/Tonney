package com.gop.util;

import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.Date;

@Data
public class TokenHelper {

	private String webName = "CTEWeb";

	public String secret = "915fc714cf7000744c908d1bc140166f";

	@Value("${expireTimeStampSecond}")
	private int expireTimeStampSecond;

	private SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

	public String generateToken(Object obj) {
		long currentTimeStamp = Instant.now().getEpochSecond();
		String token = Jwts.builder().setIssuer(webName).setSubject(JSONObject.toJSONString(obj)).setAudience("web")
				.setIssuedAt(new Date(currentTimeStamp * 1000))
				.setExpiration(new Date(currentTimeStamp * 1000 + expireTimeStampSecond))
				.signWith(SIGNATURE_ALGORITHM, secret).compact();
		return token;
	}

	public String generateToken(Integer uid) {
		long currentTimeStamp = Instant.now().getEpochSecond();
		String token = Jwts.builder().setIssuer(webName).setSubject(uid.toString()).setAudience("web")
				.setIssuedAt(new Date(currentTimeStamp * 1000))
				.setExpiration(new Date(currentTimeStamp * 1000 + expireTimeStampSecond))
				.signWith(SIGNATURE_ALGORITHM, secret).compact();
		return token;
	}

	/*public static void main (String[] args){
		TokenHelper tokenHelper = new TokenHelper();
		System.out.println("Expiration: " + tokenHelper.getAllClaimsFromToken(
				"eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJvdXJkYXhXZWIiLCJzdWIiOiIxNyIsImF1ZCI6IndlYiIsImlhdCI6MTU0MDUyMDkwNiwiZXhwIjoxNTQwNTI0NTA2fQ.Gj_UbrLmH4bMkDrvFFQfEvXv2kkcWm8O_YN14Lktj9D0iyXzjMPkccXqAxBwg3SarZ_c_M_8PjRg1HJZPteZag"
		).getExpiration());
	}*/

	private Claims getAllClaimsFromToken(String token) {
		Claims claims;
		try {
			claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
		} catch (Exception e) {
			claims = null;
		}
		return claims;
	}

	public String refreshToken(String token) {
		String refreshedToken;
		long currentTimeStamp = Instant.now().getEpochSecond();
		Date a = new Date(currentTimeStamp * 1000);
		try {
			final Claims claims = this.getAllClaimsFromToken(token);
			claims.setIssuedAt(a);
			refreshedToken = Jwts.builder().setClaims(claims)
					.setExpiration(new Date(currentTimeStamp * 1000 + expireTimeStampSecond))
					.signWith(SIGNATURE_ALGORITHM, secret).compact();
		} catch (Exception e) {
			refreshedToken = null;
		}
		return refreshedToken;
	}

	private String getJsonStringFromBody(String token) {

		String josnString = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
		return josnString;
	}

	public <T> T validAndGetDetailFromToken(String token, Class<T> jsonObject) {

		return JSONObject.parseObject(getJsonStringFromBody(token), jsonObject);

	}

}
