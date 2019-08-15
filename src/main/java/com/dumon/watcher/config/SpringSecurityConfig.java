package com.dumon.watcher.config;

import com.dumon.watcher.entity.User;
import com.dumon.watcher.helper.LoadHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

import java.util.List;

@EnableWebSecurity
@Configuration
@EnableJdbcHttpSession
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	public void configure(final WebSecurity web) {
		  web.ignoring().antMatchers("/console/**");
	}

	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		http
			.httpBasic() //HTTP Basic authentication
			.and()
			.authorizeRequests()
			.antMatchers(HttpMethod.GET, "/**").hasRole("USER")
			.antMatchers(HttpMethod.POST, "/**").hasRole("ADMIN")
			.antMatchers(HttpMethod.PUT, "/**").hasRole("ADMIN")
			.antMatchers(HttpMethod.PATCH, "/**").hasRole("ADMIN")
			.antMatchers(HttpMethod.DELETE, "/**").hasRole("ADMIN")
			.and()
			.csrf().disable()
			.formLogin().disable();
	}

	@Override
	public void configure(final AuthenticationManagerBuilder auth) throws Exception {
		List<User> users = LoadHelper.importUsers();
		for (User user : users) {
			auth.inMemoryAuthentication()
					.withUser(user.getLogin()).password(passwordEncoder().encode(user.getPass())).roles(user.getRole());
		}
	}
}
