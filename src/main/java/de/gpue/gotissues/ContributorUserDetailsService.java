package de.gpue.gotissues;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import de.gpue.gotissues.bo.Contributor;
import de.gpue.gotissues.repo.ContributorRepository;

@Configuration("UserDetailsService")
public class ContributorUserDetailsService implements UserDetailsService {
	public static GrantedAuthority ADMIN = new GrantedAuthority() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public String getAuthority() {
			return "ADMIN";
		}
	};
	public static GrantedAuthority USER = new GrantedAuthority() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public String getAuthority() {
			return "USER";
		}
	};
	
	@Autowired
	private ContributorRepository contributors;

	@Override
	public Contributor loadUserByUsername(String name)
			throws UsernameNotFoundException {
		Contributor c = contributors.findOne(name);
		if (c!=null)
			return c;
		else
			throw new UsernameNotFoundException("User " + name + " not found!");
	}
	
	public ContributorRepository getRepo() {
		return contributors;
	}
	
}