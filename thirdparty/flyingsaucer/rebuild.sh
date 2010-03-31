#!/bin/sh
#
# Rebuild xhtmlrenderer from release source or CVS with local patches
#

# either CVS or a url
#SOURCE=CVS
SOURCE=http://pigeonholdings.com/projects/flyingsaucer/R8/downloads/flyingsaucer-R8-src.zip

ROOT=`dirname $0`
BUILDDIR=/tmp/xhtmlrenderer-build

# get source
if [ -e "$BUILDDIR" ]; then
	echo "*** Removing existing build directory"
	rm -Rf "$BUILDDIR"
fi
mkdir "$BUILDDIR"
if [ "$SOURCE" != "CVS" ]; then
	#echo "*** Downloading release"
	#rm -f src.zip
	#wget -q -O src.zip "$SOURCE"
	echo "*** Extracting source"
	#unzip -q -d "$BUILDDIR" src.zip && rm -f src.zip
	unzip -q -d "$BUILDDIR" src.zip
else (
	echo "*** Retrieving CVS head"
	cd "$BUILDDIR/.."
	cvs -Q -d :pserver:anoncvs:anoncvs@cvs.dev.java.net:/cvs login
	cvs -Q -d :pserver:anoncvs@cvs.dev.java.net:/cvs checkout -d "`basename "$BUILDDIR"`" xhtmlrenderer
) fi

# apply patches
for i in $ROOT/*.diff; do
	# patches starting with xhtmlrenderer-cvs- only for cvs
	basename "$i" | grep -q '^xhtmlrenderer-cvs-' && [ "$SOURCE" != "CVS" ] && continue
	basename "$i" | grep -q '^xhtmlrenderer-nocvs-' && [ "$SOURCE" = "CVS" ] && continue
	# apply
	patch -l -p0 -d "$BUILDDIR" <"$i"
done

echo "*** Building source"
cd "$BUILDDIR"
ant jar.core-minimal

echo "*** Copying output"
[ -e "$ROOT/core-renderer-minimal.jar" ] && mv "$ROOT/core-renderer-minimal.jar" "$ROOT/core-renderer-minimal.jar.old"
cp "$BUILDDIR/build/core-renderer-minimal.jar" "$ROOT/"
cp "$BUILDDIR/lib/"iText*.jar "$ROOT/"
[ "`ls "$ROOT"/iText*.jar | wc -l`" -gt 1 ] && echo "WARNING: multiple iText verions found, check build setup"

echo "*** Done"

