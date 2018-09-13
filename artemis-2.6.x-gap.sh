export CHECKOUT=$1
export OUTPUT=$2
export START=`cd $1; git merge-base 2.6.x master`
export AMQ_START=`cd $1;git merge-base 2.6.3.jbossorg-x 2.6.x`

java -jar ./target/jira-git-0.1.SNAPSHOT-jar-with-dependencies.jar artemis $CHECKOUT $OUTPUT $START master false 2.6.x $START 2.6.3.jbossorg-x $AMQ_START
