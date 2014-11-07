package de.gpue.gotissues.bo;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Issue {
	@GeneratedValue
	@Id
	private Long id;
	@NotNull
	@Size(min = 3, max = 64)
	private String title;
	private String description;
	@NotNull
	private Date created;
	private Date deadline;
	@NotNull
	@ManyToOne
	private Issue issue;
	@NotNull
	@ManyToOne
	private Contributor creator;
	@NotNull
	private Boolean open;
	@NotNull
	private Date lastChanged;
	@ManyToOne
	private Issue parent;
	@ManyToMany
	private List<Contributor> assignees;
	@ManyToMany
	private List<Contributor> watchers;

	public Issue() {
	}

	public Issue(String title, String description, Date created, Issue issue,
			Contributor creator, Issue parent) {
		this.title = title;
		this.description = description;
		this.created = created;
		this.issue = issue;
		this.creator = creator;
		this.parent = parent;
		this.created = new Date();
		this.lastChanged = created;
		this.open = false;
		this.assignees = new LinkedList<>();
		this.watchers = new LinkedList<>();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public Contributor getCreator() {
		return creator;
	}

	public void setCreator(Contributor creator) {
		this.creator = creator;
	}

	public Boolean getOpen() {
		return open;
	}

	public void setOpen(Boolean open) {
		this.open = open;
	}

	public Date getLastChanged() {
		return lastChanged;
	}

	public void setLastChanged(Date lastChanged) {
		this.lastChanged = lastChanged;
	}

	public Issue getParent() {
		return parent;
	}

	public void setParent(Issue parent) {
		this.parent = parent;
	}

	public List<Contributor> getAssignees() {
		return assignees;
	}

	public void setAssignees(List<Contributor> assignees) {
		this.assignees = assignees;
	}

	public List<Contributor> getWatchers() {
		return watchers;
	}

	public void setWatchers(List<Contributor> watchers) {
		this.watchers = watchers;
	}

	@Override
	public String toString() {
		return "#" + id + ": " + title;
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
		Issue other = (Issue) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public Date getDeadline() {
		return deadline;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

}
