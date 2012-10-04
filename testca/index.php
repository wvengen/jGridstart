<?php
/*
 * Simple Test Certificate Authority
 *
 *   This PHP script provides a simple web-interface to a certificate
 *   authority (CA). It does not provide authorization or authentication
 *   and is meant for testing software that interfaces with a CA. It was
 *   developed to provide a testbed for jGridstart, a user-interface for
 *   friendly grid certificate management.
 *
 *   Please see README.md for more information.
 */
/*
 *   Copyright 2010-2012 Stichting FOM <jgridstart@biggrid.nl>
 *             2010-2012 W. van Engen  <wvengen@nikhef.nl>
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

/* Uncomment the following two lines to show a link to jGridstart on the
 * request page. $jgridstart must point to the base url of your jGridstart
 * installation. The instance of jGridstart must be configured to use
 * this certificate authority web interface (see README.md).  */
$jgridstart = 'http://example.com/jgridstart';
$jgridstart_jnlp = $jgridstart.'/jgridstart.jnlp';
// enable for debugging
//error_reporting(E_ALL); ini_set('display_errors', 1);

$keysdir="scripts/keys/";

function get_last_index() {
	global $keysdir;
	$f = @fopen("$keysdir/serial", 'r');
	if (!$f) return 1;
	$index = intval(fgets($f), 16);
	fclose($f);
	return $index;
}


// openssl options with type
$qnames = array(
	'subject' => 'string',
	'serial' => 'int',
	'modulus' => 'hex',
	'issuer' => 'string',
	'email' => 'string',
);

function get_certificates($query=null, $match=null, $value=null) {
	global $keysdir, $qnames;

	$certs = array();

	$dh = opendir($keysdir);
	while (($file=readdir($dh)) !== false) {
		$doshow = false;
		if (preg_match('/^(.*)\.pem$/', $file, $matches)) {
			// get certificate properties
			$opts='-'.implode(' -',array_keys($qnames));
			exec("openssl x509 -noout -in '$keysdir/$file' $opts", $_props);
			$props = array('path'=>"$keysdir/$file");
			foreach ($_props as $p) {
				$p = array_map('trim', split('=',$p,2));
				$props[strtolower($p[0])] = $p[1];
			}
			// filter query if requested
			if (!$query || !$value || !array_key_exists($query, $props)) {
				$doshow = true;
			} else {
				$type = $qnames[$query];
				$certvalue = $props[$query];
				// convert $value and $certvalue to string for comparison
				switch($type) {
				case 'string':
					$certvaluex = $certvalue;
					$valuex = $value;
					break;
				case 'int':
					// hexadecimal or decimal numbers
					if (substr($value,0,2) == '0x') {
						$valuex = sprintf('%x', intval(substr($value,2), 16));
						$certvaluex = sprintf('%x', intval($certvalue));
					} else {
						$valuex = sprintf('%d', intval($value));
						$certvaluex = sprintf('%d', intval($certvalue));
					}
					break;
				case 'hex':
					// hex ignores spaces, colons, leading zeroes, and is case insensitive
					$valuex = preg_replace('/^0+/','',preg_replace('/(\:|\s)+/','',$value));
					$certvaluex = preg_replace('/^0+/','',preg_replace('/(\:|\s)+/','',$certvalue));
					break;
				}
				// case-insensitive
				$valuex = strtoupper($valuex);
				$certvaluex = strtoupper($certvaluex);
				// compare!
				if (strcmp($match,'is')==0)
					$doshow = (strcmp($certvaluex, $valuex)==0);
				elseif (strcmp($match,'contains')==0)
					$doshow = (strstr($certvaluex, $valuex)!=false);
				else
					user_error('match must be either "is" or "contains');
			}
		}
		if ($doshow)
			array_push($certs, $props);
	}
	return $certs;
}


if (@$_REQUEST['action']=='retrieve_cert') {
	// must be before html output ...
	// TODO rework code, use templates, etc.

	if (@$_REQUEST['download']=='true') {
		header('Content-Type: application/x-pem-file');
		header('Content-Disposition: attachment; filename=usercert.pem');
	} else {
		header('Content-Type: text/plain');
	}

	if (@$_REQUEST['serial'])
		$certs = @get_certificates('serial', 'is', $_REQUEST['serial']);
	else
		$certs = @get_certificates($_REQUEST['query'],$_REQUEST['match'],$_REQUEST['value']);
	if (count($certs)==1) {
		if (fileperms($certs[0]['path']) & 0x0020) {
			readfile($certs[0]['path']);
		} else {
			header('HTTP/1.0 404 Not Found');
			print "certificate not accessible; please enable in web interface\n";
		}
	} elseif (count($certs)<1) {
		header('HTTP/1.0 404 Not Found');
		print "no certificate matches query\n";
	} else {
		header('HTTP/1.0 300 Multiple Choices');
		print "multiple certificates match query\n";
	}
	exit(0);
} elseif (@$_REQUEST['action']=='retrieve_ca_cert') {
	// CA certificate

	if (@$_REQUEST['download']=='true') {
		header('Content-Type: application/x-x509-ca-cert');
		header('Content-Disposition: attachment; filename=cacert.pem');
	} else if (@$_REQUEST['install']=='true') {
		header('Content-Type: application/x-x509-ca-cert');
		header('Content-Disposition: inline; filename=cacert.pem');
	} else {
		header('Content-Type: text/plain');
	}

	readfile("$keysdir/ca.crt");
	exit(0);
}


$action = @$_REQUEST['action'];
if (!$action) $action = 'request';

?>
<html>
<head>
 <title>Simple Test CA - <?php print htmlentities(ucwords($action)); ?></title>
 <link rel="stylesheet" type="text/css" href="base.css" />
<?php if (@$jgridstart) { ?>
  <script src="http://java.com/js/deployJava.js"></script>
  <script src="<?php print $jgridstart; ?>/tinybox.js"></script>
  <script src="<?php print $jgridstart; ?>/deployJNLP.js"></script>
  <script type="text/javascript"><!--
  deployJNLP.jnlp='<?php print $jgridstart_jnlp; ?>';
  deployJNLP.minjava='1.5';
  //--></script>
  <link rel="stylesheet" href="<?php print $jgridstart; ?>/tinybox.css" />
<?php } ?>
</head>
<body>
<h1>Simple Test CA</h1>

<?php

/*
 * navigation bar
 */
print "<ul class='nav'>\n";
$actions = Array('request', 'submit', 'retrieve');
foreach ($actions as $a) {
	$active = ($action==$a) ? " id='navactive'" : "";
	print "  <li${active}><a href='?action=${a}'>${a}</a></li>\n";
}
print "</ul>\n";

switch($action) {
/*
 * ACTION: start
 */
case 'request':
    print "<h2>Start a certificate request</h2>\n";
	if (@$jgridstart) {
?>
		<h3>Using jGridstart</h3>
		<p>A certificate can be obtained by running jGridstart, please press the <em>Launch</em> button.
		Alternatively, it is possible to create a request manually (see below).</p>
		<p>
			<script type="text/javascript">deployJNLP.launchButton();</script>
			<noscript><a href="<?php print htmlentities($jgridstart_jnlp); ?>">Launch</a></noscript>
		</p>
<?php
	}
?>
	<h3>Manual request</h3>
	<p>To request a certificate, a certificate signing request (CSR) needs to be created
	first. This can be done by the <tt>openssl</tt> comand-line utility:</p>

	<code><pre>    openssl req -nodes -newkey rsa:2048 -keyout userkey.pem -out userrequest.pem</pre></code>

	<p>This results in a private key in the file <tt>userkey.pem</tt> and a certificate signing
	request in the file <tt>userrequest.pem</tt>. Copy the contents of the latter file, go to
	the <a href="?action=submit">submit</a> page, and paste it into the <em>Request</em> box.
	The press <em>Submit request</em> to continue.</p>

<?php
	break;

/*
 * ACTION: submit
 */
case 'submit':
	if (!in_array('request', array_keys($_REQUEST))) {
		print "<h2>Submit your certificate for signing by the authority</h2>\n";
		print "<form action='?action=submit' method='post'><table class='form'>\n";
		print " <tr><td><label for='fullname'>Your name</label></td> ".
				"<td><input type='text' value='' size='40' name='fullname' id='fullname'></td></tr>\n";
		print " <tr><td><label for='email'>Your email address</label></td> ".
				"<td><input type='text' value='' size='40' name='email' id='email'></td></tr>\n";
		print " <tr><td><label for='comments'>Comments</label></td> ".
				"<td><input type='comments' value='' size='78' name='comments' id='comments'></td></tr>\n";
		print " <tr><td><label for='request'>Request</label></td> ".
				"<td><textarea cols='78' rows='5' name='request' id='request'></textarea></td></tr>\n";
		print " <tr><td></td> ".
				"<td><input type='submit' value='Submit request' name='submit' id='submit'></td></tr>\n";
		print "</table></form>\n";
		break;
	}

	print "<h2>Submitting request for signing by the authority</h2>\n";
	// check
	if (!@$_REQUEST['request']) {
		print "<p class='error'>Error: empty certificate request</p>\n";
		break;
	}
	$idx = get_last_index();
	if ($idx <= 1) {
		print "<p>Initialising PKI infrastructure (this is the first request)</p>";
		print "<pre>\n";
		system("scripts/run-cmd clean-all 2>&1");
		system("scripts/run-cmd pkitool --batch --initca 2>&1");
		print "</pre>\n";
	}

	// save request
	$certreq = sprintf('%02X', $idx);
	print "<p>Saving request as $certreq.</p>";
	$fcertreq = fopen($keysdir.$certreq.'.csr', 'w+');
	fwrite($fcertreq, $_REQUEST['request']);
	fclose($fcertreq);
	// sign certificate
	print "<p>Signing certificate request</p>";
	print "<pre>\n";
	system("scripts/run-cmd pkitool --batch --sign '$certreq' 2>&1");
	print "</pre>\n";

	print "<p>finished</p>\n";

	print "<p>disabling certificate for demoing. Please press <em>enable</em> "
		."in the certifcate list to continue the process.</p>\n";
	chmod($keysdir.$certreq.'.pem', 0600);
	break;

/*
 * ACTION: retrieve
 */
case 'retrieve':

	print "<h2>Retrieve signed certificates</h2>\n";
	$query = @$_REQUEST['query'];
	$match = @$_REQUEST['match'];
	$value = @trim($_REQUEST['value']);
	if (@$_REQUEST['clear']) {
		$query = $match = $value = null;
	}
	#if (@$qnames[$query]!='string') $match='is';
	print "<form action='' method='pre'><p>\n";
	print "<input type='hidden' name='action' value='retrieve'>\n";
	print "<select name='query' id='query'>\n";
	foreach (array_keys($qnames) as $qname) {
		$issel = strcmp(urldecode($query),$qname)==0 ? " selected='selected'" : "";
		print " <option value='$qname'$issel>$qname</option>\n";
	}
	print "</select>\n";
	print "<select name='match' id='match'>\n";
	$issel1 = strcmp(urldecode($match),'contains')==0 ? " selected='selected'" : "";
	$issel2 = strcmp(urldecode($match),'is')==0 ? " selected='selected'" : "";
	print " <option value='contains'$issel1>contains</option>\n";
	print " <option value='is'$issel2>is</option>\n";
	print "</select>\n";
	print "<input type='text' name='value' id='value' size='40' value='".htmlentities($value)."'>\n";
	print "<input type='submit' name='submit' value='Search'>\n";
	print "<input type='submit' name='clear' value='Clear' id='clear'>\n";
	print "</p></form>\n";

	$certs = get_certificates($query, $match, $value);
	if (count($certs) > 0) {
		print "<table class='certsummary'>\n";
		print " <tr>\n";
		print "  <th class='serial'>serial</th>\n";
		print "  <th class='subject'>subject</th>\n";
		print "  <th class='email'>email</th>\n";
		print "  <th class='status'></th>\n";
		print "  <th class='actions'></th>\n";
		print " </tr>\n";


		foreach ($certs as $props) {
			$serial = @htmlentities($props['serial']);
			print " <tr>\n";
			print "  <td class='serial'>$serial</span></td>\n";
			print "  <td class='subject'>".@htmlentities($props['subject'])."</td>\n";
			print "  <td class='email'>".@htmlentities($props['email'])."</td>\n";
			print "  <td class='status'>\n";
			if (fileperms($props['path']) & 0x0020)
				print
				  " <a href='?action=disable&amp;serial=$serial'>"
				  .  "<img src='icon-open.gif' width='16' height='16' alt='disable' title='enabled, click to disable'/>"
				  ."</a> ";
			else
				print
				  " <a href='?action=enable&amp;serial=$serial'>"
				  .  "<img src='icon-locked.gif' width='16' height='16' alt='enable' title='disabled, click to enable'/>"
				  ."</a> ";
			print "</td>\n";

			print "  <td class='actions'>";
	
			if (fileperms($props['path']) & 0x0020) {
			  print
				 "<a href='?action=retrieve_cert&amp;serial=$serial'>"
				.  "<img src='icon-view.gif' width='19' height='17' alt='view' title='view'/>"
				."</a>";
			  print
				 "<a href='?action=retrieve_cert&amp;serial=$serial&download=true'>"
				.  "<img src='icon-download.gif' width='19' height='17' alt='download' title='download'/>"
				."</a>";
			}
			print
				 " <a href='?action=delete&amp;serial=$serial'>"
				.  "<img src='icon-delete.gif' width='13' height='14' alt='delete' title='delete'/>"
				."</a> ";
			print "</td>\n";
			print " </tr>\n";
		}
		print "</table>\n";
	} else {
		print "<em>No matches</em>\n";
	}

	break;

case 'delete':
	print "<h2>Delete a certificate</h2>\n";
	$certs = get_certificates('serial', 'is', $_REQUEST['serial']);
	unlink($certs[0]['path']);
	unlink(preg_replace('/\.pem$/','.csr',$certs[0]['path']));
	unlink(preg_replace('/\.pem$/','.crt',$certs[0]['path']));
	print "<p><a href='?action=retrieve'>Continue</a> to the certificate list</p>\n";
	break;

case 'disable':
	print "<h2>Disable a certificate</h2>\n";
	$certs = get_certificates('serial', 'is', $_REQUEST['serial']);
	chmod($certs[0]['path'], 0600);
	print "<p><a href='?action=retrieve'>Continue</a> to the certificate list</p>\n";
	break;

case 'enable':
	print "<h2>Enable certificate</h2>\n";
	$certs = get_certificates('serial', 'is', $_REQUEST['serial']);
	chmod($certs[0]['path'], 0644);
	print "<p><a href='?action=retrieve'>Continue</a> to the certificate list</p>\n";
	break;

/*
 * ACTION: retrieve_cert
 */
case 'retrieve_cert':
	/* handled at start of file to avoid header :/ */
	/* fall through */

/*
 * ACTION: <none>
 */

default:
	print "<strong>Invalid action</strong>\n";
	break;

}

?>
<p class="footer"><em><strong>Note</strong>: this CA is not secure and should only be used for software testing purposes!</em>
<a href="?action=retrieve_ca_cert">CA cert</a> (<a href="?action=retrieve_ca_cert&amp;install=true">install</a>)</p>
</body>
</html>
