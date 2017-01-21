#!/usr/bin/env bash
echo "Usage: submit-container.sh path/to-jar/file [args....]"

echo "Submitting jar: " $1 " with params:" ${@:2}
echo "${@:2}"
echo "Copy local jar to hdfs...."
PATHVAR=`echo $1 | rev | cut -d"/" -f1 | rev`
hadoop fs -put $1 /$PATHVAR
echo Submit job to YARN...
echo "Submit args " --jar=$PATHVAR ${@:2}
hadoop jar $1 cljyarn.yarn.submit --jar=$PATHVAR ${@:2}
echo "Submitted jar: " $1



