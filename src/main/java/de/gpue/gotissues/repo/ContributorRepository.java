package de.gpue.gotissues.repo;

import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import de.gpue.gotissues.bo.Contributor;

@Component("contributors")
public interface ContributorRepository extends Repository<Contributor, String> {
	Iterable<Contributor> findAll();

	Contributor findOne(String name);
	Contributor save(Contributor entry);

}
