package com.noirix.security;

import com.noirix.security.filter.AuthenticationTokenFilter;
import com.noirix.security.jwt.JwtTokenHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userProvider;

    private final JwtTokenHelper tokenUtils;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(userProvider)
                .passwordEncoder(passwordEncoder);
    }


    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf()
                .disable()
                .exceptionHandling()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/swagger.json", "/v3/api-docs/**", "/configuration/ui/**", "/swagger-resources/**",
                        "/configuration/security/**", "/swagger-ui/**", "/webjars/**").permitAll()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers(HttpMethod.GET, "/swagger-ui.html#").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers("/registration/**").permitAll()
                .antMatchers("/authentication/**").permitAll()
                .antMatchers("/auth/**").permitAll()
                .antMatchers("/rest/**").permitAll()
                .antMatchers("/admin/**").hasAnyRole("ADMIN", "MODERATOR")
                .anyRequest()
                .authenticated();


        httpSecurity
                .addFilterBefore(authenticationTokenFilterBean(authenticationManagerBean()), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public AuthenticationTokenFilter authenticationTokenFilterBean(AuthenticationManager authenticationManager){
        AuthenticationTokenFilter authenticationTokenFilter = new AuthenticationTokenFilter(tokenUtils, userProvider);
        authenticationTokenFilter.setAuthenticationManager(authenticationManager);
        return authenticationTokenFilter;
    }

    @Override
    public void configure(WebSecurity web){
        web.ignoring()
                .antMatchers(
                        "/v3/api-docs/**",
                        "/configuration/ui/**",
                        "/swagger-resources/**",
                        "/configuration/security/**",
                        "/swagger-ui/**",
                        "/webjars/**",
                        "/swagger.json");
    }
}