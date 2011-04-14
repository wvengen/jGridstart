#!/bin/sh
#
# update wwwbase in jnlp files
#

wwwbase="$1"; shift
if [ ! "$wwwbase" ]; then
	echo "Usage: $0 <wwwbase>" >/dev/stderr
	exit 1
fi

sed -i "s|codebase=\(['\"]\).*\1|codebase=\1$wwwbase\1|g" `dirname $0`/*.jnlp

