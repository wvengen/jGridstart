#!/bin/sh
#
# update java web start files to be runnable directly
#  instead of to be processed by JNLPDownloadServlet
#

codebase="$1"
if [ ! "$codebase" ]; then
	echo "Usage: $0 -l|<codebase>" 1>&2
	exit 1
fi
shift

[ "$codebase" = "-l" ] && codebase=file://`pwd`

# expand JNLPDownloadServlet properties and update codebase
#   (assumes $$codebase is only used in codebase property)
#   (assumes $$context is not used, which wouldn't make sense without a WAR)
for jnlp in *.jnlp; do
        jnlpf=`basename "$jnlp"`
        sed -i "s|codebase=\(['\"]\).*\1|codebase=\1$codebase\1|g;s|\$\$name\b|$jnlpf|g" $jnlp
done

# link to version-numbered JARs directly; versioning with an attribute only
#   works when deployed using JNLPDownloadServlet.
#   Note that versioned dependencies may also be present manifest classpaths;
#   the name difference (with vs. without version number) can cause the error
#   "attempted to open sandboxed jar (...) as a Trusted-Library" in javaws.
sed -i "s|\(<jar\b.*\)\bhref=\(['\"]\)\(.*\)\.jar\2\s\+version=\(['\"]\)\(\S\+\)\4\(.*/>\s*\)$|\1href=\2\3-\5.jar\2\6|" *.jnlp

# version.xml is invalid now we've changed the above hrefs
rm -f version.xml


