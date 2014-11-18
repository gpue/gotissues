package de.gpue.gotissues.bo;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import de.gpue.gotissues.config.ContributorUserDetailsService;

@Entity
public class Contributor implements UserDetails {

	private static final long serialVersionUID = -8002485583324454606L;

	@Id
	@Size(min = 3, max = 16)
	private String name;
	@Size(max = 64)
	private String mail;
	private Date lastContribution;
	private boolean admin;
	private boolean enabled;
	@NotNull
	private String password;
	private String fullName;
	@Transient
	private int assignedCount;
	@Transient
	private int deadlineRaisedCount;
	@Transient
	private int points;

	public int getAssignedCount() {
		return assignedCount;
	}

	public void setAssignedCount(int assignedCount) {
		this.assignedCount = assignedCount;
	}

	public int getDeadlineRaisedCount() {
		return deadlineRaisedCount;
	}

	public void setDeadlineRaisedCount(int deadlineRaisedCount) {
		this.deadlineRaisedCount = deadlineRaisedCount;
	}

	public Contributor() {
	}

	public Contributor(String name, String fullName, String mail, String password) {
		this.name = name;
		this.mail = mail;
		this.enabled = true;
		this.admin = false;
		this.password = password;
		this.deadlineRaisedCount = 0;
		this.assignedCount = 0;
		this.points = 0;
		this.fullName = fullName;
	}	

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public Date getLastContribution() {
		return lastContribution;
	}

	public void setLastContribution(Date lastContribution) {
		this.lastContribution = lastContribution;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = new LinkedList<GrantedAuthority>();

		if (isAdmin())
			authorities.add(ContributorUserDetailsService.ADMIN);
		if (isEnabled())
			authorities.add(ContributorUserDetailsService.USER);
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String getUsername() {
		return name;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public int hashCode() {
		return this.getClass().hashCode() + name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Contributor
				&& name.equals(((Contributor) obj).getName());
	}

	@Override
	public String toString() {
		return name;
	}

}
