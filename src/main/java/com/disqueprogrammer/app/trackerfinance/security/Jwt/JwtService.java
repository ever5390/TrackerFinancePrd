package com.disqueprogrammer.app.trackerfinance.security.Jwt;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.disqueprogrammer.app.trackerfinance.security.persistence.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    @Value("${jwt.time-expiration}")
    private long TIME_EXPIRATION;

    //Generando el token
    public String generateToken(User user) {

        return Jwts
            .builder()
            .setClaims(generateExtraClaims(user))
            .setSubject(user.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis()+TIME_EXPIRATION * 1000 * 60))
            .signWith(generateKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    private Key generateKey() {
       byte[] keyBytes=Decoders.BASE64.decode(SECRET_KEY);
       return Keys.hmacShaKeyFor(keyBytes);
    }

    private Map<String, Object> generateExtraClaims(User user) {

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("authorities",user.getAuthorities());
        extraClaims.put("role",user.getRole());
        extraClaims.put("userId",user.getId());
        extraClaims.put("name",user.getFirstname());
        extraClaims.put("lastName",user.getLastname());
        extraClaims.put("email",user.getEmail());
        return extraClaims;
    }

    //Obteniendo datos desde el token

    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username=getUsernameFromToken(token);
        return username.equals(userDetails.getUsername())&& !isTokenExpired(token);
    }

    private Claims getAllClaims(String token)
    {
        return Jwts
            .parserBuilder() // Obtiene cada sección del JWT
            .setSigningKey(generateKey()) // Valida que JWT Tenga formato correcto / Que el token no haya expirado (comparando fechas creación y expiracióm) / Que la firma sea válida
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public <T> T getClaim(String token, Function<Claims,T> claimsResolver)
    {
        final Claims claims=getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Date getExpiration(String token)
    {
        return getClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token)
    {
        return getExpiration(token).before(new Date());
    }

}
