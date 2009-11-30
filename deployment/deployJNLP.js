
var deployJNLP = {
		// location of java web start file
		jnlp: null,

		// minimum java version, as a string
		minjava: '1.5',
		
		// do launch it, even if java isn't installed
		launch: function() {
			// make sure dialog is gone, if any
			try { TINY.box.hide(); } catch(err) { /* ignore */ }
			// and really launch
			deployJava.launch(this.getJNLP());
		},
		
		// launch jnlp, but show an install window when no jvm was detected
		installOrLaunch: function() {
			if (deployJava.isWebStartInstalled(this.minjava)) {
				// launch directly if required jws version present
				this.launch();

			} else {
				// otherwise show message on installation
				deployJava.returnPage = document.location;
				var msg = 
					"<h2>Java required</h2>"
					+ "<p>You need to install Java before you can run jGridstart.</p>"
					+ "<div><a href='javascript:deployJava.installLatestJRE()'><img src='"+this.resolveUrl('javabutton.png')+"' alt='Install Java'/></a></div>"
					+ "<div class='runanyway'>If you do have Java installed, you can still "
					+ "<a href='javascript:deployJNLP.launch()'>launch jGridstart</a>.</div>";
				TINY.box.show(msg, false, 0, 0, false);
			}
		},
		
		// output a launchbutton
		launchButton: function() {
			document.write('<div class="jwslaunch" style="text-align: center">');
			document.write('<a href="javascript:deployJNLP.installOrLaunch()">');
			document.write('<img src="'+deployJava.launchButtonPNG+'" border="0" alt="Launch"/>');
			document.write('</a>');
			if (!deployJava.isWebStartInstalled(this.minjava)) {
				document.write('<div style="font-size: 70%">(Java will be installed first)</div>');
			}
			document.write('</div>');
		},
		
		// returns an absolute JNLP url from the jnlp property
		getJNLP: function() {
			return this.toAbsURL(this.jnlp);
		},

		// returns the url to a file in the same directory as the jnlp
		resolveUrl: function(e) {
			// if no slashes in jnlp url, it's just this directory
			if (this.jnlp.indexOf('/') == 0)
				return e;
			// otherwise strip off filename and return file with path
			return this.jnlp.substring(0, this.jnlp.lastIndexOf('/')+1) + e;
		},
		
		// absolutize a url (required for getJNLP() and IE)
		// http://groups.google.com/group/comp.lang.javascript/browse_thread/thread/6937160715587627 
		toAbsURL: function(s) {
			var l = location, h, p, f, i;
			if (/^\w+:/.test(s)) {
				return s;
			}

			h = l.protocol + '//' + l.host;
			if (s.indexOf('/') == 0) {
				return h + s;
			}

			p = l.pathname.replace(/\/[^\/]*$/, '');
			f = s.match(/\.\.\//g);
			if (f) {
				s = s.substring(f.length * 3);
				for (i = f.length; i--;) {
					p = p.substring(0, p.lastIndexOf('/'));
				}
			}

			return h + p + '/' + s;
		}
};
