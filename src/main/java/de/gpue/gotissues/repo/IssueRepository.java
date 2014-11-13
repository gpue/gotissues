package de.gpue.gotissues.repo;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import de.gpue.gotissues.bo.Contributor;
import de.gpue.gotissues.bo.Issue;

@Component("issues")
public interface IssueRepository extends Repository<Issue, Long>{
	
	public List<Issue> findByTitleContainingOrDescriptionContaining(String st,String sc);
	
	public Issue findOne(Long id);
	public List<Issue> findByWatchers(Contributor watcher);
	public List<Issue> findByAssignees(Contributor assignee);
	public List<Issue> findByParentOrderByLastChangedDesc(Issue parent);

	
	public Issue save(Issue i);

	int countByAssignees(Contributor assigneee);
	int countByAssigneesAndDeadlineBefore(Contributor assignee,Date deadline);
	int countByAssigneesAndDeadlineAfter(Contributor assignee,Date deadline);
	int count();
}
