var actionRegistry = new Array();

function updateState() {
	$.ajax({
		type : "GET",
		url : '/api/issue/' + issue + ':updateprocstate',
		data : {
			state : JSON.stringify(Proclib.netstate)
		},
		dataType : "json",
		contentType : "application/json"
	});
}

function confirmAction(msg) {
	return function(transition) {
		return function() {
			var btn = null;
			document.getElementById('procform').appendChild(
					p(btn = button(msg, '')));

			var action = function() {
				transition.finalize();
				updateState();
				contribute("Confirmed process task: " + msg);
			};

			var idx = actionRegistry.push(action) - 1;

			btn.setAttribute('onClick', 'javascript:actionRegistry[' + idx
					+ ']()');
		}
	}
}

function enterAction(msg) {
	return function(transition) {
		return function() {
			var ig = null;
			document.getElementById('procform').appendChild(
					p(ig = inputGroupButton(msg, '')));

			var action = function() {
				transition.finalize();
				updateState();
				contribute("User entered required information ("+msg+"): " + ig.childNodes[0].value);
			};

			var idx = actionRegistry.push(action) - 1;

			ig.childNodes[1].childNodes[0].setAttribute('onClick', 'javascript:actionRegistry[' + idx
					+ ']()');
		}
	}
}
