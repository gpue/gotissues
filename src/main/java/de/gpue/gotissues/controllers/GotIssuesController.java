package de.gpue.gotissues.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.gpue.gotissues.bo.Contributor;
import de.gpue.gotissues.bo.process.ProcessDescription;

@Controller("WebController")
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
	public String userStats(
			@RequestParam(value = "start", required = false) Long start,
			Model model) {
		return skeleton("/userstats", model);
	}

	@RequestMapping("/createaccount")
	public String createAccount(Model model) {
		return skeleton("/account", model);
	}

	@RequestMapping("/account/{u}")
	public String account(@PathVariable("u") String name, Model model) {
		return skeleton("/account", model);
	}

	@RequestMapping("/issue/{parent}/createissue")
	public String createIssue(@PathVariable("parent") Long parent, Model model) {
		model.addAttribute("parent", service.getIssue(parent));
		model.addAttribute("contributors", service.getContributors());
		model.addAttribute("issues", service.getIssues(""));
		return skeleton("/createissue", model);
	}

	@RequestMapping("/createissue")
	public String createIssue(Model model) {
		model.addAttribute("issues", service.getIssues(""));
		model.addAttribute("contributors", service.getContributors());
		return skeleton("/createissue", model);
	}

	@RequestMapping("/error")
	public String error(Model model) {
		return "/error";
	}

	@RequestMapping("/issuelist")
	public String issueList(
			@RequestParam(value = "search", defaultValue = "") String search,
			@RequestParam(value = "expand", required = false) Long expand,
			Model model) {
		model.addAttribute("search", search);
		if (expand != null)
			model.addAttribute("expand", expand);
		return skeleton("/issuelist", model);
	}

	@RequestMapping("/issue/{id}")
	public String showIssue(@PathVariable("id") Long id, Model model) {
		model.addAttribute("issue", service.getIssue(id));
		model.addAttribute("issues", service.getIssues(""));
		model.addAttribute("contributors", service.getContributors());
		return skeleton("/showissue", model);
	}

	@RequestMapping("/contributor/{c}")
	public String showContributor(@PathVariable("c") String c, Model model) {
		model.addAttribute("contributor", service.getContributor(c));
		return skeleton("/showcontributor", model);
	}

	@RequestMapping("/processlist")
	public String processList(Model model) {
		return skeleton("/processlist", model);
	}

	@RequestMapping("/processes/{p}")
	public String editProcess(@PathVariable("p") Long id, Model model) {
		ProcessDescription pd = service.getProcess(id);
		model.addAttribute("procid", id);
		model.addAttribute("code", pd.getCode());
		model.addAttribute("procname", pd.getName());
		return skeleton("/editprocess", model);
	}

	@RequestMapping("/processes/{p}:instantiate")
	public String instantiateProcess(@PathVariable("p") Long id, Model model) {
		ProcessDescription pd = service.getProcess(id);
		model.addAttribute("process", pd);
		model.addAttribute("issues", service.getIssues(""));
		return skeleton("/instantiate", model);
	}

	@RequestMapping("/createprocess")
	public String createProcess(Model model) {
		model.addAttribute("procid", -1);
		return skeleton("/editprocess", model);
	}

	private String skeleton(String target, Model model) {
		Contributor me = null;
		model.addAttribute("me", me = service.getMe());
		model.addAttribute("issuecount", service.countIssues());
		model.addAttribute("watchcount", service.getWatchedIssues(me.getName())
				.size());
		model.addAttribute("assigncount",
				service.getAssignedIssues(me.getName()).size());
		model.addAttribute("baseurl", service.getBaseURL());
		return target;
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}
}
