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
	return enterAction(msg, function(v) {
		return v;
	});
}

function enterAction(msg, interpreter) {
	return function(transition) {
		return function() {
			var ig = null;
			document.getElementById('procform').appendChild(
					p(ig = inputGroupButton(msg, '')));

			var action = function() {
				var value = interpreter(ig.childNodes[0].value);

				if (value != null) {
					transition.finalize();
					Proclib.netstate.data[transition.name] = value;
					updateState();
					contribute("User entered required information (" + msg
							+ "): " + value);
				}
			};

			var idx = actionRegistry.push(action) - 1;

			ig.childNodes[1].childNodes[0].setAttribute('onClick',
					'javascript:actionRegistry[' + idx + ']()');
		}
	}
}
