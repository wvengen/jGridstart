Simple Test Certificate Authority
=================================

This is a PHP web-interface to a simple certificate authority (CA) for testing
purposes. It does not provide authorization or authentication. The aim is to
provide a simple interface for testing interaction of client tools like
[jGridstart] with online CAs.

The CA functionality is based on [Easy-RSA] 2.0 (part of [OpenVPN]), which is
provided in the `scripts/` directory. Do make sure that this directory is _not_
accessible over HTTP, since that would compromise that little part of security
that is left (the CA private key). The supplied .htaccess file does this for
Apache already.

The `scripts/keys` directory must be readable _and_ writable by the web server
process.

When accessing the web-interface for the first time, a new CA key and
certificate are generated. You may want to edit `scripts/vars` before doing so,
to customize cryptography settings, the distinguished name (DN) of the CA, and
other settings.


Linking to jGridstart
---------------------

To use this CA web interface with jGridstart, you can set the two variables
below and havea link to jGridstart shown on the request page.  You need to have
configured a jGridstart instance to work with this CA. This can be done by
adding the following lines to `jgridstart.jnlp` in its `resources` section:

    <property name="jnlp.jgridstart.ca.provider" value="TestCA" />
    <property name="jnlp.jgridstart.ca.base" value="http://example.com/testca/" />

where `jgridstart.ca.base` should point to the URL of this web interface.

If you have changed the CA DN previously by editing `scripts/vars`, it may be
useful to add another property so that jGridstart knows which certificates can
be renewed by this certificate authority:

    <property name="jnlp.jgridstart.ca.dn" value="C=DE, C=Berlin, O=GmbH, CN=Herr, emailAddress=berliner@example.com" />



Other notes
-----------

Please note that the Easy-RSA scripts as shipped have seen minor modifications
as to work with a `scripts/keys` directory that is not owned by the user
running the scripts.


[Easy-RSA]: http://openvpn.net/index.php/open-source/documentation/miscellaneous/rsa-key-management.html
[OpenVPN]: http://openvpn.net/index.php/open-source/
[jGridstart]: http://jgridstart.nikhef.nl/

