package de.gpue.gotissues.repo;

import java.util.Date;
import java.util.List;

import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import de.gpue.gotissues.bo.Contribution;
import de.gpue.gotissues.bo.Contributor;
import de.gpue.gotissues.bo.Issue;

@Component("contributions")
public interface ContributionRepository extends  Repository<Contribution, Long> {
	Contribution save(Contribution entry);

	Contribution findOne(Long id);
	
	List<Contribution> findAll();

	List<Contribution> findByIssueOrderByCreatedDesc(Issue issue);
	
	List<Contribution> findByContributorOrderByCreatedDesc(Contributor contributor);
	
	List<Contribution> findByContentLike(String needle);
	
	int countByContributorAndCreatedAfter(Contributor c, Date from);
		
	int count();
}
