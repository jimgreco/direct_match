#!/bin/bash

echo "HELLO"
echo $CIRCLE_ARTIFACTS
for f in "$CIRCLE_ARTIFACTS"/*; do
	echo "File -> $f"
done

scp  $CIRCLE_ARTIFACTS/build/libs/main-218.jar dm@core01.directmatchx.com:/home/dm 

