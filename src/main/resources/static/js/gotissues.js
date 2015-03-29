function showProcessGraph(container) {
	var nodes = [];
	var nodesRaw = [];
	for (var i = 0; i < Proclib.places.length; i++) {
		nodes.push({
			id : i,
			label : '' + Proclib.netstate.marking[i],
			group : 'places'
		});
		nodesRaw.push(Proclib.places[i]);
	}
	var j = nodesRaw.length;
	for (var i = 0; i < Proclib.transitions.length; i++) {
		j = nodes.push({
			id : j,
			label : Proclib.transitions[i].name,
			group : 'transitions'
		});
		nodesRaw.push(Proclib.transitions[i]);
	}

	// create an array with edges
	var edges = [];
	Proclib.arcs.forEach(function(a) {
		edges.push({
			from : nodesRaw.indexOf(a.from),
			to : nodesRaw.indexOf(a.to)
		});
	});

	// create a network
	var data = {
		nodes : nodes,
		edges : edges
	};
	var options = {
		edges : {
			style : 'arrow'
		},
		groups : {
			transitions : {
				shape : 'box',
				color : {
					border : 'black',
					background : 'lightblue',
					highlight : {
						border : 'orange',
						background : 'green'
					}
				}
			},
			places : {
				shape : 'circle',
				color : {
					border : 'black',
					background : 'white',
					highlight : {
						border : 'black',
						background : 'orange'
					}
				}
			}
		}
	};

	new vis.Network(container, data, options);
}

function deleteContribution(c) {
	$.ajax({
		type : "GET",
		url : baseURL+'/api/contributions/' + c + ':delete',
		success : function(data) {
			window.location.reload();
		},
		error : function() {
			shout("error", "Error!", "Deleting contribution failed.");
		},
		dataType : "json",
		contentType : "application/json"
	});
}

function deleteIssue(i) {
	$.ajax({
		type : "GET",
		url : baseURL+'/api/issues/' + i + ':delete',
		success : function(data) {
			window.location.href = "/issuelist";
		},
		error : function(jqxhr, status, msg) {
			shout("error", "Error!", "Deleting issue failed.");
		},
		dataType : "json",
		contentType : "application/json"
	});
};

function shout(type, heading, message) {
	if (type == 'error') {
		type = 'danger';
	}

	var div = document.createElement('DIV');
	div.setAttribute("role", "alert");
	div.setAttribute("class", 'alert alert-' + type);
	var strong = document.createElement('STRONG');
	strong.appendChild(txt(heading));
	div.appendChild(strong);
	div.appendChild(txt(' ' + message));
	document.getElementById('shout').appendChild(div);
}

function state(isOpen) {
	var span = document.createElement('SPAN');
	if (isOpen) {
		span.setAttribute('class', 'btn btn-success');
		span.appendChild(txt('Open'));
	} else {
		span.setAttribute('class', 'btn btn-danger');
		span.appendChild(txt('Closed'));
	}

	return span;
}

function tr(id, parentId, tds) {
	var tr = document.createElement('TR');
	tr.setAttribute("data-tt-id", id);
	if (parentId != null)
		tr.setAttribute("data-tt-parent-id", parentId);

	for (var i = 0; i != tds.length; i++) {
		tr.appendChild(tds[i]);
	}

	return tr;
}

function button(text, onclick) {
	var btn = document.createElement('BUTTON');
	btn.value = text;
	btn.onclick = onclick;
	btn.appendChild(txt(text));
	btn.setAttribute("class", "btn btn-primary");
	return btn;
}

function inputGroupButton(placeholder, onclick) {
	var ig = document.createElement('DIV');
	ig.setAttribute("class", "input-group");
	var i = document.createElement('INPUT');
	i.setAttribute("type", "text");
	i.setAttribute("class", "form-control");
	i.setAttribute("placeholder", placeholder);
	ig.appendChild(i);
	var s = document.createElement('SPAN');
	s.setAttribute("class", "input-group-btn");
	ig.appendChild(s);
	var b = button('Ok', onclick);
	s.appendChild(b);
	return ig;
}

function p(content) {
	var p = document.createElement('P');
	p.appendChild(content);
	return p;
}

function td(content) {
	var td = document.createElement('TD');
	td.appendChild(content);
	return td;
}

function txt(str) {
	return document.createTextNode(str);
}

function issuelink(id, title) {
	var a = document.createElement('A');
	a.href = baseURL+'/issue/' + id;
	a.appendChild(txt('#' + id + ' ' + title));
	return a;
}

function userlink(name) {
	var a = document.createElement('A');
	a.href = baseURL+'/contributor/' + name;
	a.appendChild(txt(name));
	return a;
}

function childlink(parentId) {
	var a = document.createElement('A');
	a.href = baseURL+'/issue/' + parentId + '/createissue';
	a.appendChild(txt('Create child issue'));
	return a;
}

function timelineEntry(cdata) {
	var li = document.createElement('LI');
	var wrap = li;
	var x = document.createElement('I');
	wrap.appendChild(x);
	x.setAttribute("class", "fa fa-edit bg-blue");
	x = document.createElement('DIV');
	wrap.appendChild(x);
	x.setAttribute("class", "timeline-item");
	wrap = x;
	x = document.createElement('SPAN');
	x.setAttribute("class", "time fa fa-clock-o");
	x
			.appendChild(txt(' '
					+ $.format.date(cdata.created, 'dd.MM.yyyy hh:mm:ss')));
	wrap.appendChild(x);
	x = document.createElement('H4');
	x.appendChild(userlink(cdata.contributor.name));
	x.appendChild(txt(' contributed to '));
	x.appendChild(issuelink(cdata.issue.id, cdata.issue.title));
	wrap.appendChild(x);

	x = document.createElement('DIV');
	x.setAttribute("class", "timeline-body");
	$(x).html(cdata.content);
	wrap.appendChild(x);

	x = document.createElement('DIV');
	x.setAttribute("class", "timeline-footer");
	var adel = document.createElement('A');
	adel
			.setAttribute("href", 'javascript:deleteContribution(' + cdata.id
					+ ')');
	adel.setAttribute("class", "delc btn btn-danger fa fa-eraser col-sm-2");
	adel.setAttribute("style", "display:none");
	adel.appendChild(txt("Delete"));
	x.appendChild(adel);
	wrap.appendChild(x);
	return li;
}

function editprocesslink(name, id) {
	var a = document.createElement('A');
	a.href = baseURL+'/processes/' + id;
	a.setAttribute("class", "fa fa-cogs");
	a.appendChild(txt(' ' + name));
	return a;
}

function deleteProcess(id) {
	if (confirm("Delete this process: Are you sure?")) {

		$.ajax({
			type : "GET",
			url : baseURL+'/api/processes/' + id + ':delete',
			success : function(data) {
				window.location.href = baseURL+'/processlist';
			},
			error : function() {
				shout("danger", "Error!",
						"Process was not deleted due to an error.");
			},
			dataType : "json",
			contentType : "application/json"
		});
	}
}

function deleteprocesslink(id) {
	var a = document.createElement('A');
	a.href = 'javascript:deleteProcess(' + id + ')';
	a.appendChild(txt(' Delete'));
	a.setAttribute("class", "fa fa-eraser");

	return a;
}

function instantiateprocesslink(id) {
	var a = document.createElement('A');
	a.href = baseURL+'/processes/' + id + ':instantiate';
	a.appendChild(txt(' Instantiate'));
	a.setAttribute("class", "fa fa-rocket");

	return a;
}
