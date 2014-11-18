function deleteContribution(c){
	$.ajax({
		type : "GET",
		url : '/api/contributions/' + c + ':delete',
		success : function(data) {
			window.location.reload();
		},
		error: function() {
			shout("error","Error!","Deleting contribution failed.");
		},
		dataType : "json",
		contentType : "application/json"
	});
}

function deleteIssue(i) {
	$.ajax({
		type : "GET",
		url : '/api/issues/' + i + ':delete',
		success : function(data) {
			alert("issue deleted");
			window.location.href = "/issuelist";
		},
		error: function(jqxhr,status,msg) {
			shout("error","Error!","Deleting issue failed.");
		},
		dataType : "json",
		contentType : "application/json"
	});
};

function shout(type,heading,message){
	if(type == 'error'){
		type = 'danger';
	}
	
	var div = document.createElement('DIV');
	div.setAttribute("role","alert");
	div.setAttribute("class",'alert alert-'+type);
	var strong = document.createElement('STRONG');
	strong.appendChild(txt(heading));
	div.appendChild(strong);
	div.appendChild(txt(' '+message));
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
	a.href = '/issue/' + id;
	a.appendChild(txt('#' + id + ' ' + title));
	return a;
}

function userlink(name) {
	var a = document.createElement('A');
	a.href = '/contributor/' + name;
	a.appendChild(txt(name));
	return a;
}

function childlink(parentId) {
	var a = document.createElement('A');
	a.href = '/issue/' + parentId + '/createissue';
	a.appendChild(txt('Create child issue'));
	return a;
}

function timelineEntry(cdata){
	var li = document.createElement('LI');
	var wrap = li;
	var x = document.createElement('I');
	wrap.appendChild(x);
	x.setAttribute("class","fa fa-edit bg-blue");
	x = document.createElement('DIV');
	wrap.appendChild(x);
	x.setAttribute("class","timeline-item");
	wrap = x;
	x = document.createElement('SPAN');
	x.setAttribute("class","time fa fa-clock-o");
	x.appendChild(txt(' '+$.format.date(cdata.created, 'dd.MM.yyyy hh:mm:ss')));
	wrap.appendChild(x);
	x = document.createElement('H4');
	x.appendChild(userlink(cdata.contributor.name));
	x.appendChild(txt(' contributed to '));
	x.appendChild(issuelink(cdata.issue.id,cdata.issue.title));
	wrap.appendChild(x);


	
	x = document.createElement('DIV');
	x.setAttribute("class","timeline-body");
	$(x).html(cdata.content);
	wrap.appendChild(x);
	
	x = document.createElement('DIV');
	x.setAttribute("class","timeline-footer");
	var adel = document.createElement('A');
	adel.setAttribute("href",'javascript:deleteContribution('+cdata.id+')');
	adel.setAttribute("class","delc btn btn-danger fa fa-eraser col-sm-2");
	adel.setAttribute("style","display:none");
	adel.appendChild(txt("Delete"));
	x.appendChild(adel);
	wrap.appendChild(x);
	return li;
}