export CHECKOUT=$1
export OUTPUT=$2
export AMQ_START=`cd $1;git merge-base 2.6.3.jbossorg-x 2.6.x`

java -jar ./target/jira-git-0.1.SNAPSHOT-jar-with-dependencies.jar amq $CHECKOUT $OUTPUT 2.6.1 2.6.x false 2.6.3.jbossorg-x $AMQ_START
