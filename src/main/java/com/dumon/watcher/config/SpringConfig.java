package com.dumon.watcher.config;

import com.dumon.watcher.dto.User;
import com.dumon.watcher.helper.LoadHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
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

	private static final String SESSION_TABLE_CREATE_SCRIPT_PATH = "classpath:org/springframework/session/jdbc/schema-%s.sql";
	private static final String SESSION_TABLE_DROP_SCRIPT_PATH = String.format(SESSION_TABLE_CREATE_SCRIPT_PATH, "drop-%s");

	@Resource
	private ResourceLoader resourceLoader;
	@Resource
	private DataSource dataSource;
	@Resource
	private AppProperties appProperties;
	@Resource
	private JdbcTemplate jdbcTemplate;

	/**
	 * Forced action because {@link EnableJdbcHttpSession} disables spring-boot schema auto-init
	 * (all spring-boot's props 'spring.session.*' are omitted)
	 */
	@PostConstruct
	public void dataSourceInit() throws SQLException {
		Connection connection = dataSource.getConnection();
		initSessions(connection);
	}

	private void initSessions(final Connection connection) {
		org.springframework.core.io.Resource dropScript =
				resourceLoader.getResource(getSessionTablesScriptPath( true));
		org.springframework.core.io.Resource createScript =
				resourceLoader.getResource(getSessionTablesScriptPath( false));
		ScriptUtils.executeSqlScript(connection, dropScript);
		ScriptUtils.executeSqlScript(connection, createScript);
	}

	private String getSessionTablesScriptPath(final boolean isDrop) {
		String scriptPath;
		String dbVendorName = getDbVendor().toLowerCase();
		if (isDrop) {
			scriptPath = String.format(SESSION_TABLE_DROP_SCRIPT_PATH, dbVendorName);
		} else {
			scriptPath = String.format(SESSION_TABLE_CREATE_SCRIPT_PATH, dbVendorName);
		}
		return scriptPath;
	}

	private String getDbVendor() {
		return this.jdbcTemplate.execute((ConnectionCallback<String>) connection ->
				connection.getMetaData().getDatabaseProductName());
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
		Optional<String> usersFilePath = LoadHelper.getJvmArg(appProperties.getUsersFile());
		List<User> users;
		if (usersFilePath.isPresent()) {
			users = LoadHelper.importUsersFromFile(usersFilePath.get());
		} else {
			users = LoadHelper.importDefaultUsers();
		}
		users.forEach(user -> inMemoryAuth
				.withUser(user.getLogin()).password(passwordEncoder().encode(user.getPass())).roles(user.getRole()));
	}
}
