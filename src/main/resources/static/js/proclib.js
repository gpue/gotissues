var actionRegistry = new Array();

function updateState(msg) {
	$.ajax({
		type : "GET",
		url : baseURL+'/api/issue/' + issue + ':updateprocstate',
		data : {
			state : JSON.stringify(Proclib.netstate)
		},
		dataType : "json",
		contentType : "application/json",
		error : function() {
			shout("error", "Error!", "Updating process statee failed.");
		},
		success : function() {
			contribute(msg);
		}
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
				updateState("Confirmed process task: " + msg);
			};

			var idx = actionRegistry.push(action) - 1;

			btn.setAttribute('onClick', 'javascript:actionRegistry[' + idx
					+ ']()');
		}
	}
}

function enterAction(msg, interpreter) {
	return function(transition) {
		return function() {
			var ig = null;
			document.getElementById('procform').appendChild(
					p(ig = inputGroupButton(msg, '')));

			var action = function() {
				if (typeof interpreter == 'undefined') {
					interpreter = function(v) {
						return v
					};
				}

				var value = interpreter(ig.childNodes[0].value);

				if (value != null) {
					transition.finalize();
					Proclib.netstate.data[transition.name] = value;
					updateState("User entered required information (" + msg
							+ "): " + value);
				}
			};

			var idx = actionRegistry.push(action) - 1;

			ig.childNodes[1].childNodes[0].setAttribute('onClick',
					'javascript:actionRegistry[' + idx + ']()');
		}
	}
}
