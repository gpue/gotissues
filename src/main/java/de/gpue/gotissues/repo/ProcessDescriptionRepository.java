package de.gpue.gotissues.repo;

import java.util.List;

import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import de.gpue.gotissues.bo.process.ProcessDescription;

@Component("processesdescriptions")
public interface ProcessDescriptionRepository extends  Repository<ProcessDescription, Long> {
	ProcessDescription save(ProcessDescription entry);
	void delete(Long id);
	
	ProcessDescription findOne(Long id);
	
	List<ProcessDescription> findAll();

	Integer count();
}
