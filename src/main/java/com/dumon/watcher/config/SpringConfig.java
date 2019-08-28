package com.dumon.watcher.config;

import static com.dumon.watcher.helper.Constants.JVM.APP_CONFIG;

import com.dumon.watcher.entity.User;
import com.dumon.watcher.helper.Constants;
import com.dumon.watcher.helper.LoadHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;

@EnableWebSecurity
@Configuration
@EnableJdbcHttpSession
public class SpringConfig extends WebSecurityConfigurerAdapter {

	@Resource
	private ResourceLoader resourceLoader;
	@Resource
	private DataSource dataSource;

	/**
	 * Forced action because {@link EnableJdbcHttpSession} disables spring-boot schema auto-init
	 * (all spring-boot's props 'spring.session.*' are omitted)
	 */
	@PostConstruct
	public void dataSourceSessionsInit() throws SQLException {
		Connection connection = dataSource.getConnection();
		org.springframework.core.io.Resource resource =
				resourceLoader.getResource("classpath:org/springframework/session/jdbc/schema-h2.sql");
		ScriptUtils.executeSqlScript(connection, resource);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	public void configure(final WebSecurity webSecurity) {
		  webSecurity.ignoring()
				  .antMatchers("/login")
				  .antMatchers("/console/**");
	}

	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		http
			.httpBasic() //HTTP Basic authentication
			.and()
			.csrf().disable()
			.authorizeRequests()
				.antMatchers(HttpMethod.GET,"/home").hasAnyRole()
				.antMatchers(HttpMethod.POST,"/j_spring_security_check").hasAnyRole()
				.antMatchers("/devices/**").hasRole("ADMIN")
				.antMatchers("/app/**").hasRole("ADMIN")
			.and()
			.formLogin()
				.loginPage("/login")
				.loginProcessingUrl("/j_spring_security_check")
				.failureUrl("/login?error")
				.usernameParameter("j_username")
				.passwordParameter("j_password")
				.permitAll()
			.and()
			.logout()
				.permitAll()
				.logoutUrl("/logout")
				.logoutSuccessUrl("/login?logout")
				.invalidateHttpSession(true);
	}

	@Override
	public void configure(final AuthenticationManagerBuilder auth) throws Exception {
		InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> inMemoryAuth = auth.inMemoryAuthentication();
		Optional<String> usersFilePath = LoadHelper.getJvmArg(Constants.JVM.USERS);
		List<User> users;
		if (usersFilePath.isPresent()) {
			users = LoadHelper.importUsersFromFile(usersFilePath.get());
		} else {
			users = LoadHelper.importDefaultUsers();
		}
		users.forEach(user -> inMemoryAuth
				.withUser(user.getLogin()).password(passwordEncoder().encode(user.getPass())).roles(user.getRole()));
	}

	@Bean
	public PropertySourcesPlaceholderConfigurer configurer(final ResourceLoader resourceLoader) {
		PropertySourcesPlaceholderConfigurer placeholderConfigurer = new PropertySourcesPlaceholderConfigurer();
		placeholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
		placeholderConfigurer.setIgnoreResourceNotFound(true);

		org.springframework.core.io.Resource[] resources = {
				resourceLoader.getResource("classpath:application.properties"),
				resourceLoader.getResource("classpath:./config/app.config"),
		};
		LoadHelper.getJvmArg(APP_CONFIG)
				.map(path -> resourceLoader.getResource("file:" + path))
				.ifPresent(resource -> resources[resources.length - 1] = resource);

		placeholderConfigurer.setLocations(resources);
		return placeholderConfigurer;
	}
}
