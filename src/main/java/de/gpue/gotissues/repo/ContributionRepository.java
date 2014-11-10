package de.gpue.gotissues.repo;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
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
	
	List<Contribution> findByContentContaining(String search);
	
	@Query("SELECT SUM(cb.points) FROM Contribution cb WHERE cb.contributor=?1 and cb.created > ?2")
	Integer getPoints(Contributor c, Date from);
		
	Integer count();
}
