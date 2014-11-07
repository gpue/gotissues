package de.gpue.gotissues.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.gpue.gotissues.bo.Contributor;
import de.gpue.gotissues.bo.Issue;
import de.gpue.gotissues.repo.ContributionRepository;
import de.gpue.gotissues.repo.ContributorRepository;
import de.gpue.gotissues.repo.IssueRepository;

@RestController
@RequestMapping(value = "/api")
public class GotIssuesRestController {
	@Autowired
	private IssueRepository issues;
	@Autowired
	private ContributorRepository contributors;
	@Autowired
	private ContributionRepository contributions;
	@Autowired
	private PasswordEncoder encoder;

	@RequestMapping("/issues")
	public List<Issue> getIssues() {
		return issues.findAll();
	}

	@RequestMapping("/issues:count")
	public int countIssues(){
		return issues.count();
	}

	@RequestMapping("/contributors/{c}/watched")
	public List<Issue> getWatchedIssues(@PathVariable("c") String c) {
		return issues.findByWatchers(getContributor(c));
	}

	@RequestMapping("/contributors/{c}/assigned")
	public List<Issue> getAssignedIssues(@PathVariable("c") String c) {
		return issues.findByAssignees(getContributor(c));
	}

	@RequestMapping("/contributor/{c}")
	public Contributor getContributor(String c) {
		Contributor co = contributors.findOne(c);
		if(co!=null)co.setPassword(null);
		return co;
	}

	@RequestMapping("/contributors:me")
	public Contributor getMe() {
		Object o = SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();	
		return getContributor(o.toString());
	}
}
