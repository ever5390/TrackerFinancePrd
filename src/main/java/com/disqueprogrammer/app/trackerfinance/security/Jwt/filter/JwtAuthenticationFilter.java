package com.disqueprogrammer.app.trackerfinance.security.Jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.disqueprogrammer.app.trackerfinance.security.Jwt.JwtService;
import com.disqueprogrammer.app.trackerfinance.security.dtoAuth.HttpResponse;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	//OncePerRequestFilter :: Procesa una petición por vez

	Logger LOGGER = LoggerFactory.getLogger(getClass());
	private JwtService jwtService;
	private UserDetailsService userDetailsService;

	public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	/*
	 * 	Este filtro es para validar(al momento de acceder a los recursos) que el usuario ha sido authenticado previamente (que está loggeado).
	 * 	Para ello valida a través del token los datos del usuario y este loggin está registrado en el contexto de SpringSecurity
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {

			final String token = getTokenFromRequest(request);
			final String username;

			if (token==null) {
				filterChain.doFilter(request, response);
				return;
			}

			//3. Obtener el subject(username) desde el jwt
			username=jwtService.getUsernameFromToken(token);
			System.out.println(" ::::: username: " + username);
			//4. valida que el context Holder tenga seteada la authenticación
			if (username!=null && SecurityContextHolder.getContext().getAuthentication()==null)
			{
				UserDetails userDetails=userDetailsService.loadUserByUsername(username);

				System.out.println(":::: userDateails: " + userDetails.getAuthorities());
				if (jwtService.isTokenValid(token, userDetails))
				{
					// El null en las credenciales, aquí las credenciales solo son necesarias cuando mandamos a llamar el método
					// authenticate del AuthenticationManager y eso se hizo cuando generamos el token en el login
					UsernamePasswordAuthenticationToken authToken= new UsernamePasswordAuthenticationToken(
							userDetails,
							null,
							userDetails.getAuthorities());

					//5. Setear el objeto Authentication dentro del SecurityContext
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}

			//6. Ejecutar resto de filtros
			filterChain.doFilter(request, response);

		} catch (ExpiredJwtException expiredJwtException) {
			HttpResponse httpResponse = new HttpResponse(UNAUTHORIZED.value(), UNAUTHORIZED, UNAUTHORIZED.getReasonPhrase().toUpperCase(), "Token is expired");
			response.setContentType(APPLICATION_JSON_VALUE);
			response.setStatus(UNAUTHORIZED.value());
			OutputStream outputStream = response.getOutputStream();
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(outputStream, httpResponse);
			outputStream.flush();
		} catch (Exception e) {
			HttpResponse httpResponse = new HttpResponse(UNAUTHORIZED.value(), UNAUTHORIZED, UNAUTHORIZED.getReasonPhrase().toUpperCase(), "Authentication error");
			response.setContentType(APPLICATION_JSON_VALUE);
			response.setStatus(UNAUTHORIZED.value());
			OutputStream outputStream = response.getOutputStream();
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(outputStream, httpResponse);
			outputStream.flush();
		}

	}

	private String getTokenFromRequest(HttpServletRequest request) {

		//1. Obtener el header que contiene el jwt
		final String authHeader=request.getHeader(HttpHeaders.AUTHORIZATION);

		//2. Obtener el jwt desde el header
		if(StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer "))
		{
			return authHeader.substring(7);
		}
		return null;
	}





}