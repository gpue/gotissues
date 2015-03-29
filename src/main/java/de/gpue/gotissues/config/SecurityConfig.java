package de.gpue.gotissues.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import de.gpue.gotissues.bo.Contribution;
import de.gpue.gotissues.bo.Contributor;
import de.gpue.gotissues.bo.Issue;

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
			Contributor admin = new Contributor("admin", "", "test@example.com",
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
					.loginPage("/login").defaultSuccessUrl("/issuelist",true).permitAll().and().logout()
					.logoutUrl("/logout").logoutSuccessUrl("/login");
			http.csrf().disable();
			log.debug("configured API authentification");
		}
	}
	
	
	@Aspect
	@Component("APIPasswordFilter")
	public static class PasswordFilterAspect{
		
		private static String SECURE_PKG_PREFIX = "de.gpue";
		
		@AfterReturning(value="execution(* de.gpue.gotissues.controllers.GotIssuesRestController.*(..))",returning="result")
		public void removePasswordWriteObject(JoinPoint p, Object result){
			String caller = Thread.currentThread().getStackTrace()[2].getClassName();
			if(!caller.startsWith(SECURE_PKG_PREFIX))removePassword(result);
		}
		
		public void removePassword(Object o){
			if(o instanceof Iterable)((Iterable<?>)o).forEach(e -> removePassword(e));
			if(o instanceof Contributor)removePasswordsFromContributor((Contributor)o);
			if(o instanceof Contribution)removePasswordsFromContribution((Contribution)o);
			if(o instanceof Issue)removePasswordsFromIssue((Issue)o);
		}
		
		private void removePasswordsFromContributor(Contributor c) {
			c.setPassword(null);
		}

		private void removePasswordsFromIssue(Issue i) {
			i.getCreator().setPassword(null);
			i.getAssignees().forEach(a -> a.setPassword(null));
			i.getWatchers().forEach(a -> a.setPassword(null));
		}

		private void removePasswordsFromContribution(Contribution c) {
			removePasswordsFromIssue(c.getIssue());
			c.getContributor().setPassword(null);
		}
	}

}