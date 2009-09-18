
var deployJNLP = {
		// location of java web start file
		jnlp: null,

		// minimum java version, as a string
		minjava: '1.5',
		
		launch: function() {
			deployJava.launch(this.jnlp);
		},
		
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
					+ "<div><a href='javascript:deployJava.installLatestJRE()'><img src='javabutton.png' alt='Install Java'/></a></div"
					+ "<div class='runanyway'>If you do have Java installed, you can still "
					+ "<a href='javascript:deployJNLP.launch()'>launch jGridstart</a>.</div>";
				TINY.box.show(msg, false, 0, 0, false);
			}
		},
		
		launchButton: function() {
			document.write('<div class="jwslaunch" style="text-align: center">');
			document.write('<a href="javascript:deployJNLP.installOrLaunch()">');
			document.write('<img src="'+deployJava.launchButtonPNG+'" border="0" alt="Launch"/>');
			document.write('</a>');
			if (!deployJava.isWebStartInstalled(this.minjava)) {
				document.write('<div style="font-size: 70%">(Java will be installed first)</div>');
			}
			document.write('</div>');
		}
};
