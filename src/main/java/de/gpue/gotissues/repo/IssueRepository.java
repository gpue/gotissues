package de.gpue.gotissues.repo;

import java.util.List;

import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import de.gpue.gotissues.bo.Contributor;
import de.gpue.gotissues.bo.Issue;

@Component("issues")
public interface IssueRepository extends Repository<Issue, Long>{
	public List<Issue> findAll();
	
	public Issue findOne(Long id);
	public List<Issue> findByWatchers(Contributor watcher);
	public List<Issue> findByAssignees(Contributor assignee);
	
	public void save(Issue i);
	
	int count();
}
