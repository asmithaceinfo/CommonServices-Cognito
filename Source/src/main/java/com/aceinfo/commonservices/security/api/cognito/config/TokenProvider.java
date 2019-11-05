package com.aceinfo.commonservices.security.api.cognito.config;



import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.text.ParseException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import com.amazonaws.util.StringUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;


@Component
public class TokenProvider implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private Key key;

    private long tokenValidityInMilliseconds;

    private long tokenValidityInMillisecondsForRememberMe;
    long tokenValidityInSeconds = 1800; // 30 minutes
    long tokenValidityInSecondsForRememberMe = 2592000; // 30 days

	private String KEY_STORE_PATH = "/.well-known/jwks.json";
	
	RemoteJWKSet<SecurityContext> remoteJWKSet;

	
    
    @Override
    public void afterPropertiesSet() throws Exception {
      /*  byte[] keyBytes;
       String secret = null;
        if (!StringUtils.isEmpty(secret)) {
           log.warn("Warning: the JWT key used is not Base64-encoded. " );
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
       } else {
           log.debug("Using a Base64-encoded JWT secret key");
           keyBytes = Decoders.BASE64.decode(base64Secret);
       }
        this.key = Keys.hmacShaKeyFor(keyBytes);*/
        this.tokenValidityInMilliseconds =
            1000 * tokenValidityInSeconds;
        this.tokenValidityInMillisecondsForRememberMe =
            1000 * tokenValidityInSecondsForRememberMe;
    }

 /*   public String createToken(Authentication authentication, boolean rememberMe) {
        String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity;
        if (rememberMe) {
            validity = new Date(now + this.tokenValidityInMillisecondsForRememberMe);
        } else {
            validity = new Date(now + this.tokenValidityInMilliseconds);
        }

       
        return Jwts.builder()
            .setSubject(authentication.getName())
            .claim(AUTHORITIES_KEY, authorities)
            .signWith(SignatureAlgorithm.HS512,key)
            .setExpiration(validity)
            .compact();
    }*/

    public boolean validateToken(String authToken)  {
        try {
        
        	JWTParser.parse(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException | ParseException e) {
        	logger.info("Invalid JWT token.");
        	logger.trace("Invalid JWT token trace.", e);
        }
        return false;
    }
    
    public Authentication getAuthentication(String token) throws MalformedURLException {
    	UsernamePasswordAuthenticationToken authenticationToken = null ;
    
    	JWT jwt;
		try {
			jwt = JWTParser.parse(token);
			String iss = jwt.getJWTClaimsSet().getIssuer();
			logger.info(iss);
        	// check if issuer is our user pool
			if (!StringUtils.isNullOrEmpty(jwt.getJWTClaimsSet().getIssuer())) {	
				
			    String Issuer = jwt.getJWTClaimsSet().getIssuer();
	            URL JWKUrl = new URL(Issuer + KEY_STORE_PATH);
				JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, new RemoteJWKSet<>(JWKUrl));

				ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
				jwtProcessor.setJWSKeySelector(keySelector);
			
				// check token
				JWTClaimsSet claimsSet = jwtProcessor.process(jwt, null);

				// process roles (groups in cognito)
				List<String> groups = (List<String>) claimsSet.getClaim("cognito:groups");
				
				List<GrantedAuthority> authorities = new ArrayList<>();
					
				groups.forEach(s -> {
						authorities.add(new SimpleGrantedAuthority(s));
										
				});
				

				authenticationToken = new UsernamePasswordAuthenticationToken(
						claimsSet, null, authorities);

	        
	    }
		} catch (ParseException | BadJOSEException | JOSEException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		
		return authenticationToken;
}
}
