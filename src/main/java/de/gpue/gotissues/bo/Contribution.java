package de.gpue.gotissues.bo;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
public class Contribution {
	@GeneratedValue
	@Id
	private Long id;
	private String content;
	@NotNull
	private Date created;
	@NotNull
	@ManyToOne
	private Issue issue;
	@NotNull
	@ManyToOne
	private Contributor contributor;
	@NotNull
	private Boolean revisable;
	
	public Contribution() {
	}

	public Contribution(String content, Date created, Issue issue,
			Contributor contributor, Boolean revisable) {
		this.content = content;
		this.created = created;
		this.issue = issue;
		this.contributor = contributor;
		this.revisable = revisable;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Issue getIssue() {
		return issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	public Contributor getContributor() {
		return contributor;
	}

	public void setContributor(Contributor contributor) {
		this.contributor = contributor;
	}

	public Boolean getRevisable() {
		return revisable;
	}

	public void setRevisable(Boolean revisable) {
		this.revisable = revisable;
	}

	@Override
	public String toString() {
		return content;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Contribution other = (Contribution) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
