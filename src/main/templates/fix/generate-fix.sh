rm -rf ../../java/com/core/fix/msgs
rm -rf ../../../test/java/com/core/fix/msgs
mkdir ../../java/com/core/fix/msgs
mkdir ../../../test/java/com/core/fix/msgs
python generate-fix.py fix.xml
