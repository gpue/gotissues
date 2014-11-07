package de.gpue.gotissues.bo.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import de.gpue.gotissues.bo.Contributor;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends GlobalAuthenticationConfigurerAdapter {

	private final Log log = LogFactory.getLog(getClass());

	private PasswordEncoder encoder;
	private ContributorUserDetailsService cuds;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService()).passwordEncoder(
				passwordEncoder());
		log.debug("configured global authentification");

		try {
			cuds.loadUserByUsername("admin");
		} catch (UsernameNotFoundException e) {
			Contributor admin = new Contributor("admin", "test@example.com",
					encoder.encode("vivaris"));
			admin.setAdmin(true);
			cuds.getRepo().save(admin);

			log.info("Standard user admin with password 'vivaris' was added.");
		}
	}

	@Bean
	public ContributorUserDetailsService userDetailsService() {
		return cuds == null ? (cuds = new ContributorUserDetailsService())
				: cuds;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return encoder == null ? (encoder = new BCryptPasswordEncoder())
				: encoder;
	}

	@Configuration
	@Order(1)
	public static class ApiWebSecurityConfigurationAdapter extends
			WebSecurityConfigurerAdapter {

		private final Log log = LogFactory.getLog(getClass());

		protected void configure(HttpSecurity http) throws Exception {
			http.antMatcher("/api/**").authorizeRequests().anyRequest()
					.authenticated().and().httpBasic();
			log.debug("configured API authentification");
		}
	}

	@Configuration
	public static class FormLoginWebSecurityConfigurerAdapter extends
			WebSecurityConfigurerAdapter {

		private final Log log = LogFactory.getLog(getClass());

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.authorizeRequests().anyRequest().authenticated().and()
					.formLogin()
					.loginPage("/login").defaultSuccessUrl("/issuelist").permitAll().and().logout()
					.logoutUrl("/logout").logoutSuccessUrl("/login");
			http.csrf().disable();
			log.debug("configured API authentification");
		}
	}

}