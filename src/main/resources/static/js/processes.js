Proclib = {
	places : new Array(),
	arcs : new Array(),
	transitions : new Array(),
	netstate : {},
	initmarking : new Array(),
	prepare : function() {
		if(!Proclib.netstate.init){
			Proclib.netstate.marking = Proclib.initmarking;
			Proclib.netstate.data = [];
			Proclib.netstate.init = true;
		}
		
		Proclib.transitions.forEach(function(t) {
			var activated = true;
			Proclib.places.forEach(function(p) {
				var requiredTokens = 0;
				Proclib.arcs.forEach(function(a) {
					if (a.from == p && a.to == t) {
						requiredTokens++;
					}
				});
				if (Proclib.netstate.marking[Proclib.places.indexOf(p)] < requiredTokens) {
					activated = false;
				}
			});
			if (activated) {
				t.activated = true;
				try{
					t.prepare();
				} catch(e) {
					shout("error", "Error!", "Failure occuring when executing transtion "+t.name+".");
				}
			}
		});

	},
	_test : function() {
		var p1 = new Place();
		var p2 = new Place();
		var t1 = new Transition("Hello", function() {
			Console.log('hello')
		});
		new Arc(p1, t1);
		new Arc(t1, p2);
	}
}

function Place(initcount) {
	Proclib.places.push(this)-1;
	Proclib.initmarking.push(initcount);
}

function Transition(name, prepare) {
	this.name = name;
	this.prepare = prepare(this);
	Proclib.transitions.push(this);
	this.activated = false;
}

Transition.prototype = {
	constructor : Transition,
	finalize : function() {
		var t = this;
		Proclib.arcs.forEach(function(a) {
			if (a.to == t) {
				Proclib.netstate.marking[Proclib.places.indexOf(a.from)]--;
			}
			if (a.from == t) {
				Proclib.netstate.marking[Proclib.places.indexOf(a.to)]++;
			}
		})
	}
}

function Arc(from, to) {
	this.from = from;
	this.to = to;
	Proclib.arcs.push(this);
}
