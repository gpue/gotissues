package de.gpue.gotissues.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.gpue.gotissues.bo.Contribution;
import de.gpue.gotissues.bo.Contributor;
import de.gpue.gotissues.bo.Issue;
import de.gpue.gotissues.repo.ContributionRepository;
import de.gpue.gotissues.repo.ContributorRepository;
import de.gpue.gotissues.repo.IssueRepository;
import de.gpue.gotissues.util.MailUtil;

@RestController
@RequestMapping(value = "/api")
public class GotIssuesRestController {
	private static final String WATCHED = "watched@";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"dd.MM.yyyy");
	public static final int PAGE_SIZE = 20;
	private static final String ASSIGNED = "assigned@";
	private static final String NO_PWD = "********";

	@Autowired
	private IssueRepository issues;
	@Autowired
	private ContributorRepository contributors;
	@Autowired
	private ContributionRepository contributions;
	@Autowired
	private PasswordEncoder encoder;
	@Value("${gotissues.notifier.mail}")
	private String notifierMail = "";

	@RequestMapping("/issues")
	public List<Issue> getIssues(
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "search", defaultValue = "") String search) {
		List<Issue> il = getIssues(search != null ? search : "");
		if (page == null || page <= 1)
			page = 1;
		return page == null ? il : il.subList(
				Math.min(il.size(), PAGE_SIZE * (page - 1)),
				Math.min(il.size(), PAGE_SIZE * page));
	}

	@Cacheable("default")
	private List<Issue> getIssues(String search) {

		Set<Issue> found = new HashSet<Issue>();

		String searchArr[] = search.split("\\s");

		for (String s : searchArr) {
			found.addAll(issues.findByTitleContainingOrDescriptionContaining(
					search, search));
			contributions.findByContentContaining(s).forEach(
					c -> found.add(c.getIssue()));

			if (s.startsWith(WATCHED)) {
				String cn = s.substring(WATCHED.length());
				Contributor c = cn.length() == 0 ? getMe() : contributors
						.findOne(cn);
				if (c != null)
					found.addAll(issues.findByWatchers(c));
			} else if (s.startsWith(ASSIGNED)) {
				String cn = s.substring(ASSIGNED.length());
				Contributor c = cn.length() == 0 ? getMe() : contributors
						.findOne(cn);
				if (c != null)
					found.addAll(issues.findByAssignees(c));
			} else {
				Contributor c = contributors.findOne(s);
				contributions.findByContributorOrderByCreatedDesc(c).forEach(
						ct -> found.add(ct.getIssue()));
			}
		}

		List<Issue> result = new ArrayList<Issue>(found);
		result.sort(new Comparator<Issue>() {

			@Override
			public int compare(Issue o1, Issue o2) {
				return o2.getLastChanged().compareTo(o1.getLastChanged());
			}

		});

		return orderChildren(result);
	}

	private List<Issue> orderChildren(List<Issue> todo) {
		List<Issue> result = new LinkedList<>();

		while (!todo.isEmpty()) {
			Issue head = todo.remove(0);
			result.add(head);
			List<Issue> children = issues
					.findByParentOrderByLastChangedDesc(head);
			todo.removeAll(children);
			result.removeAll(children);
			todo.addAll(0, children);
		}

		return result;
	}

	@RequestMapping("/issues/{i}")
	public Issue getIssue(@PathVariable("i") Long id) {
		return issues.findOne(id);
	}

	@RequestMapping(value = "/issues:add", method = { RequestMethod.GET,
			RequestMethod.POST })
	@CacheEvict("default")
	public Issue addIssue(
			@RequestParam("title") String title,
			@RequestParam(value = "description", defaultValue = "") String description,
			@RequestParam(value = "parent", required = false) Long parent,
			@RequestParam(value = "assignees[]", required = false) List<String> assignees,
			@RequestParam(value = "deadline", required = false) String deadline)
			throws ParseException {
		final Issue i = new Issue(title, description, new Date(),
				contributors.findOne(getMe().getUsername()),
				parent != null ? getIssue(parent) : null);

		if (deadline != null && deadline != "")
			i.setDeadline(DATE_FORMAT.parse(deadline));
		if (assignees != null) {
			i.setAssignees(new HashSet<>());
			assignees.forEach(a -> i.getAssignees()
					.add(contributors.findOne(a)));
		}

		Issue ir = issues.save(i);

		contribute(getMe().getName() + " created a new issue: " + ir,
				"<h4>Issue created: " + title + "</h4>" + description
						+ "<h4>Assignees:</h4>" + assignees(assignees),
				ir.getId(), getMe().getName(), false, 3);

		return ir;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/issues/{i}:alter", method = { RequestMethod.GET,
			RequestMethod.POST })
	@CacheEvict("default")
	public Issue alterIssue(
			@PathVariable("i") Long id,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "deadline", required = false) String deadline,
			@RequestParam(value = "assignees[]", required = false) List<String> assignees) {
		Issue i = issues.findOne(id);

		if (title == null && description == null)
			return i;

		Assert.isTrue(i.isOpen(), "Issue is closed!");

		StringBuffer content = new StringBuffer("<ul>");

		if (title != null && !title.equals(i.getTitle())) {
			content.append("<li><strong>New title: </strong>" + title + "</li>");
			i.setTitle(title);
		}
		if (description != null && !description.equals(i.getDescription())) {
			content.append("<li><strong>New description: </strong>"
					+ description + "</li>");
			i.setDescription(description);
		}
		if (deadline != null) {
			Date dlObj = null;
			try {
				dlObj = DATE_FORMAT.parse(deadline);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			if (i.getDeadline() == null) {
				if (dlObj != null) {
					content.append("<li>strong>New deadline: </strong>"
							+ deadline + "</li>");
					i.setDeadline(dlObj);
				}
			} else {
				if (dlObj == null) {
					content.append("<li><strong>Removed deadline </strong>"
							+ deadline + "</li>");
				} else {
					content.append("<li><strong>Shifted deadline from </strong>"
							+ DATE_FORMAT.format(i.getDeadline())
							+ " to "
							+ deadline + "</li>");
				}
			}
		}
		if (assignees != null) {
			Set<String> oldCl = i.getAssignees().stream().map(a -> a.getName())
					.collect(Collectors.toSet());

			List<String> addCl = (List<String>) CollectionUtils
					.subtract(assignees, oldCl).stream().map(e -> "" + e)
					.collect(Collectors.toList());
			List<String> remCl = (List<String>) CollectionUtils
					.subtract(oldCl, assignees).stream().map(e -> "" + e)
					.collect(Collectors.toList());

			if (!addCl.isEmpty())
				content.append("<li><strong>Newly assigned:</strong>"
						+ assignees(addCl) + "</li>");
			if (!remCl.isEmpty())
				content.append("<li><strong>Removed assignee(s):</strong>"
						+ assignees(remCl) + "</li>");

			i.setAssignees(assignees.stream().map(s -> contributors.findOne(s))
					.collect(Collectors.toSet()));
		}

		content.append("</ul>");

		issues.save(i);

		contribute(getMe().getName() + " changed issue " + i,
				"<h4>Issue changed:</h4>" + content, id, getMe().getName(),
				false, 2);

		return i;
	}

	@RequestMapping(value = "/issues/{i}:delete", method = { RequestMethod.GET,
			RequestMethod.POST })
	@CacheEvict("default")
	public boolean deleteIssue(@PathVariable("i") Long id) {
		Assert.isTrue(getMe().isAdmin());
		Assert.notNull(id);

		Issue issue = issues.findOne(id);

		for (Issue c : issues.findByParentOrderByLastChangedDesc(issue)) {
			deleteIssue(c.getId());
		}

		for (Contribution c : contributions
				.findByIssueOrderByCreatedDesc(issue)) {
			contributions.delete(c.getId());
		}

		issues.delete(id);

		return true;
	}

	private String assignees(List<String> assignees) {
		if (assignees == null || assignees.isEmpty())
			return "none";

		StringBuilder b = new StringBuilder();
		b.append("<ul>");

		assignees.forEach(a -> b.append("<li><a href=\"/contributor/" + a
				+ "\">" + a + "</a></li>"));

		b.append("</ul>");

		return b.toString();
	}

	@RequestMapping(value = "/issues/{i}:close", method = { RequestMethod.GET,
			RequestMethod.POST })
	public Issue closeIssue(@PathVariable("i") Long id) {
		Issue i = issues.findOne(id);

		contribute(getMe() + " closed issue " + i, "<h4>Issue closed.</h4>",
				id, getMe().getName(), false, 1);

		i.setOpen(false);

		issues.save(i);

		issues.findByParentOrderByLastChangedDesc(i).forEach(
				ic -> closeIssue(ic.getId()));

		return i;
	}

	@RequestMapping(value = "/issues/{i}:open", method = { RequestMethod.GET,
			RequestMethod.POST })
	public Issue openIssue(@PathVariable("i") Long id) {
		Issue i = issues.findOne(id);

		i.setOpen(true);

		issues.save(i);

		contribute(getMe() + " re-opened issue " + i,
				"<h4>Issue re-opened.</h4>", id, getMe().getName(), false, 1);

		if (i.getParent() != null)
			openIssue(i.getParent().getId());

		return i;
	}

	@RequestMapping("/issues/{i}:watch")
	public Issue watchIssue(@PathVariable("i") Long id) {
		Issue i = issues.findOne(id);

		if (i.getWatchers() == null)
			i.setWatchers(new HashSet<Contributor>());
		i.getWatchers().add(getMe());

		issues.save(i);

		return i;
	}

	@RequestMapping("/issues/{i}:unwatch")
	public Issue unwatchIssue(@PathVariable("i") Long id) {
		Issue i = issues.findOne(id);

		if (i.getWatchers() == null)
			i.setWatchers(new HashSet<Contributor>());
		i.getWatchers().remove(getMe());

		issues.save(i);

		return i;
	}

	@RequestMapping("/issues:count")
	public int countIssues() {
		return issues.count();
	}

	@RequestMapping(value = "/issues/{i}:contribute", method = {
			RequestMethod.GET, RequestMethod.POST })
	public Contribution contribute(
			@RequestParam(value = "content", defaultValue = "") String content,
			@RequestParam("issue") Long issue) {
		return contribute(getMe().getName() + " contributed to " + issue,
				content, issue, getMe().getName(), true, 3);
	}

	@RequestMapping(value = "/contributions/{id}:delete", method = {
			RequestMethod.GET, RequestMethod.POST })
	public boolean deleteContribution(@PathVariable("id") Long id) {
		Assert.isTrue(getMe().isAdmin());
		Assert.notNull(id);
		contributions.delete(id);
		return true;
	}

	@CacheEvict("default")
	private Contribution contribute(String mailSubject, String content,
			Long issue, String contributor, Boolean revisable, int points) {
		Contribution c = new Contribution(content, new Date(), getIssue(issue),
				getContributor(contributor), revisable, points);

		Assert.isTrue(c.getIssue().isOpen(), "Issue is closed!");

		for (Object or : CollectionUtils.union(c.getIssue().getAssignees(), c
				.getIssue().getWatchers())) {
			Contributor r = (Contributor) or;
			try {
				MailUtil.sendHTMLMail(notifierMail, r.getMail(), mailSubject,
						content);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		c = contributions.save(c);

		c.getContributor().setLastContribution(c.getCreated());
		contributors.save(c.getContributor());

		Issue update = c.getIssue();
		while (update != null) {
			update.setLastChanged(c.getCreated());
			issues.save(update);
			update = update.getParent();
		}

		return c;
	}

	@RequestMapping("/issues/{i}/contributions")
	public List<Contribution> getContributionsByIssue(
			@PathVariable("i") Long id,
			@RequestParam(value = "page", required = false) Integer page) {
		if (page != null) {
			return contributions.findByIssueOrderByCreatedDesc(getIssue(id),
					new PageRequest(page - 1, PAGE_SIZE)).getContent();
		} else {
			return contributions.findByIssueOrderByCreatedDesc(getIssue(id));
		}
	}

	@RequestMapping("/contributors")
	public List<Contributor> getContributors() {
		return contributors.findAll();
	}

	public static class ChartDataSet {
		private String label;
		private int value;
		private String color;
		private String highlight;

		public ChartDataSet(String label, int value, String color,
				String highlight) {
			this.label = label;
			this.value = value;
			this.color = color;
			this.highlight = highlight;
		}

		public String getLabel() {
			return label;
		}

		public int getValue() {
			return value;
		}

		public String getColor() {
			return color;
		}

		public String getHighlight() {
			return highlight;
		}
	}

	public static class ContributorStats {
		private String contributor;
		private int points;
		private List<ChartDataSet> data;

		public ContributorStats(String contributor, int points,
				List<ChartDataSet> data) {
			this.contributor = contributor;
			this.points = points;
			this.data = data;
		}

		public String getContributor() {
			return contributor;
		}

		public int getPoints() {
			return points;
		}

		public List<ChartDataSet> getData() {
			return data;
		}
	}

	@RequestMapping("/stats")
	public List<ContributorStats> getStats(
			@RequestParam(value = "contributor", required = false) String cName,
			@RequestParam(value = "start", required = false) Long startLong) {

		List<Contributor> cl = new LinkedList<>();
		List<ContributorStats> result = new LinkedList<>();

		if (cName != null) {
			cl.add(getContributor(cName));
		} else {
			cl.addAll(getContributors());
		}

		for (Contributor c : cl) {

			List<ChartDataSet> cds = new LinkedList<>();

			Date start = startLong == null ? new Date(0L) : new Date(
					System.currentTimeMillis() + startLong);

			Integer points = contributions.getPoints(c, start);

			int intime = issues.countByAssigneesAndDeadlineAfter(c, new Date());
			int overdue = issues.countByAssigneesAndDeadlineBefore(c,
					new Date());
			int assigned = issues.countByAssignees(c);
			cds.add(new ChartDataSet("in time", intime, "Green", "lightgreen"));
			cds.add(new ChartDataSet("overdue", overdue, "Red", "Orange"));
			cds.add(new ChartDataSet("undated", assigned, "DodgerBlue",
					"LightSkyBlue"));

			result.add(new ContributorStats(c.getName(), points == null ? 0
					: points, cds));
		}

		Collections.sort(result, new Comparator<ContributorStats>() {
			@Override
			public int compare(ContributorStats s1, ContributorStats s2) {
				return s2.getPoints() - s1.getPoints();
			}
		});

		return result;
	}

	@RequestMapping(value = "/contributors:add", method = { RequestMethod.GET,
			RequestMethod.POST })
	public Contributor addContributor(
			@RequestParam("username") String name,
			@RequestParam(value = "fullname", required = false) String fullname,
			@RequestParam("password") String password,
			@RequestParam("mail") String mail) {

		Assert.isTrue(getMe().isAdmin(), "Only admin can do this!");

		Contributor c = new Contributor(name, fullname, mail,
				encoder.encode(password));

		contributors.save(c);

		return getContributor(name);
	}

	@RequestMapping(value = "/contributors/{c}:alter", method = {
			RequestMethod.GET, RequestMethod.POST })
	@CacheEvict("default")
	public Contributor alterContributor(
			@PathVariable("c") String c,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "fullname", required = false) String fullname,
			@RequestParam(value = "password", required = false) String password,
			@RequestParam(value = "mail", required = false) String mail) {

		Assert.isTrue(getMe().isAdmin() && getMe().getName().equals(name),
				"Only admin or the contributor himself can do this!");

		Contributor co = contributors.findOne(c);

		if (name != null)
			co.setName(name);
		if (password != null && !password.equals(NO_PWD))
			co.setPassword(encoder.encode(password));
		if (mail != null)
			co.setMail(mail);
		if (fullname != null)
			co.setFullName(fullname);

		co = contributors.save(co);

		return getContributor(co.getName());
	}

	@RequestMapping(value = "/contributors/{c}:enable", method = {
			RequestMethod.GET, RequestMethod.POST })
	public Contributor enableContributor(@PathVariable("c") String c,
			@RequestParam(value = "enabled") boolean enabled) {

		Contributor co = contributors.findOne(c);

		co.setEnabled(enabled);

		contributors.save(co);

		return getContributor(c);
	}

	@RequestMapping(value = "/contributors/{c}:grant", method = {
			RequestMethod.GET, RequestMethod.POST })
	public Contributor grantAdmin(@PathVariable("c") String c,
			@RequestParam(value = "admin") boolean admin) {

		Contributor co = contributors.findOne(c);

		co.setAdmin(admin);

		contributors.save(co);

		return getContributor(c);
	}

	@RequestMapping("/contributors/{c}/watched")
	public List<Issue> getWatchedIssues(@PathVariable("c") String c) {
		return issues.findByWatchers(getContributor(c));
	}

	@RequestMapping("/contributors/{c}/assigned")
	public List<Issue> getAssignedIssues(@PathVariable("c") String c) {
		return issues.findByAssignees(getContributor(c));
	}

	@RequestMapping("/contributors/{c}/contributions")
	public List<Contribution> getContributionsByContributor(
			@PathVariable("c") String c,
			@RequestParam(value = "page", required = false) Integer page) {
		if (page != null) {
			return contributions.findByContributorOrderByCreatedDesc(
					getContributor(c), new PageRequest(page - 1, PAGE_SIZE))
					.getContent();
		} else {
			return contributions
					.findByContributorOrderByCreatedDesc(getContributor(c));
		}
	}

	@RequestMapping("/contributors/{c}")
	public Contributor getContributor(@PathVariable("c") String c) {
		Contributor co = contributors.findOne(c);
		return co;
	}

	@RequestMapping("/contributors:me")
	public Contributor getMe() {
		Object o = SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		return getContributor(o.toString());
	}
}
