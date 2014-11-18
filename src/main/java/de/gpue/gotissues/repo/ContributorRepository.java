package de.gpue.gotissues.repo;

import java.util.List;

import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import de.gpue.gotissues.bo.Contributor;

@Component("contributors")
public interface ContributorRepository extends Repository<Contributor, String> {
	List<Contributor> findAll();
	void delete(String name);

	Contributor findOne(String name);
	Contributor save(Contributor entry);

}
