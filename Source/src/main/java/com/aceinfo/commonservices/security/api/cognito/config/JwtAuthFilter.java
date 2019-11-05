
package com.aceinfo.commonservices.security.api.cognito.config;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.util.StringUtils;

@ConfigurationProperties("cognito")
public class JwtAuthFilter extends OncePerRequestFilter {

	public static final String AUTH_HEADER_STRING = "Authorization";
	private static final String AUTH_BEARER_STRING = "Bearer";

    @Autowired
	private TokenProvider tokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		String header = null;
		String authorization = req.getHeader("Authorization");
		if (authorization != null) {
			header = resolveToken(req);

			logger.info(header);
			 if (StringUtils.hasText(header) && this.tokenProvider.validateToken(header)) {
				try {
					
					 Authentication authentication = this.tokenProvider.getAuthentication(header);
			         SecurityContextHolder.getContext().setAuthentication(authentication);

					}

				catch (NullPointerException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		chain.doFilter(req, res);

	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTH_HEADER_STRING);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AUTH_BEARER_STRING)) {
			return bearerToken.replace(AUTH_BEARER_STRING, "");
		}
		return null;
	}

}
