package de.gpue.gotissues.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import de.gpue.gotissues.bo.Contributor;

@Controller
public class GotIssuesController implements ErrorController{
	@Autowired private GotIssuesRestController service;
	
	@RequestMapping("/")
	public String index(){
		return "redirect:/issuelist";
	}

	@RequestMapping("/login")
	public String login(){
		return "/login";
	}
	
	@RequestMapping("/userstats")
	public String userstats(Model model){
		return skeleton("/userstats",model);
	}
	
	@RequestMapping("/createaccount")
	public String createaccount(Model model){
		return account(null,model);
	}
	
	@RequestMapping("/account/{u}")
	public String account(@PathVariable("u") String name, Model model){
		model.addAttribute("user",service.getContributor(name));
		if(name != null && service.getContributor(name) != null){
			model.addAttribute("case","edit");
		} else {
			model.addAttribute("case","new");
		}
		return skeleton("/account",model);
	}
	
	@RequestMapping("/createissue")
	public String createissue(Model model){
		return skeleton("/createissue",model);
	}
	
	@RequestMapping("/error")
	public String error(Model model){
		return "/error";
	}
	
	@RequestMapping("/issuelist")
	public String issuelist(Model model){
		model.addAttribute("issues",service.getIssues());
		return skeleton("/issuelist",model);
	}
	
	
	@RequestMapping("/contributor")
	public String showContributor(Model model){
		return skeleton("/showcontributor",model);
	}
	
	private String skeleton(String target, Model model){
		Contributor me = null;
		model.addAttribute("me", (me = service.getMe()));
		model.addAttribute("issuecount",service.countIssues());
		model.addAttribute("watchcount",service.getWatchedIssues(me.getName()).size());
		model.addAttribute("assigncount", service.getAssignedIssues(me.getName()).size());
		return target;
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}
}
