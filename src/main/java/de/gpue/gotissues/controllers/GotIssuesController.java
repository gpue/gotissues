package de.gpue.gotissues.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.gpue.gotissues.bo.Contributor;

@Controller
@RequestMapping("/gotissues")
public class GotIssuesController implements ErrorController {
	@Autowired
	private GotIssuesRestController service;

	@RequestMapping("/")
	public String index() {
		return "redirect:/issuelist";
	}

	@RequestMapping("/login")
	public String login() {
		return "/login";
	}

	@RequestMapping("/userstats")
	public String userstats(
			@RequestParam(value = "start", required = false) Long start,
			Model model) {
		return skeleton("/userstats", model);
	}

	@RequestMapping("/createaccount")
	public String createaccount(Model model) {
		return skeleton("/account", model);
	}

	@RequestMapping("/account/{u}")
	public String account(@PathVariable("u") String name, Model model) {
		return skeleton("/account", model);
	}

	@RequestMapping("/issue/{parent}/createissue")
	public String createissue(@PathVariable("parent") Long parent, Model model) {
		model.addAttribute("parent", service.getIssue(parent));
		model.addAttribute("contributors", service.getContributors());
		return skeleton("/createissue", model);
	}

	@RequestMapping("/createissue")
	public String createissue(Model model) {
		model.addAttribute("contributors", service.getContributors());
		return skeleton("/createissue", model);
	}

	@RequestMapping("/error")
	public String error(Model model) {
		return "/error";
	}

	@RequestMapping("/issuelist")
	public String issuelist(
			@RequestParam(value = "search", defaultValue="") String search,
			Model model) {
		model.addAttribute("search", search);
		return skeleton("/issuelist", model);
	}

	@RequestMapping("/issue/{id}")
	public String showIssue(@PathVariable("id") Long id, Model model) {
		model.addAttribute("issue", service.getIssue(id));
		//model.addAttribute("contributions", service.getContributionsByIssue(id));
		model.addAttribute("contributors", service.getContributors());
		return skeleton("/showissue", model);
	}

	@RequestMapping("/contributor/{c}")
	public String showContributor(@PathVariable("c") String c, Model model) {
		model.addAttribute("contributor", service.getContributor(c));;
		return skeleton("/showcontributor", model);
	}

	private String skeleton(String target, Model model) {
		Contributor me = null;
		model.addAttribute("me", (me = service.getMe()));
		model.addAttribute("issuecount", service.countIssues());
		model.addAttribute("watchcount", service.getWatchedIssues(me.getName())
				.size());
		model.addAttribute("assigncount",
				service.getAssignedIssues(me.getName()).size());
		return target;
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}
}
