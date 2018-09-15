export CHECKOUT=$1
export OUTPUT=$2
#export AMQ_START=`cd $1;git merge-base 2.6.3.jbossorg-x 2.6.x`

# this is the last known commit from 2.6.x before the release
export START="358bf7d4e26be6cdff5186799e22e09a739d36dc"

#java -jar ./target/jira-git-0.1.SNAPSHOT-jar-with-dependencies.jar amq $CHECKOUT $OUTPUT $START 2.6.x false 2.6.3.jbossorg-x $AMQ_START
java -jar ./target/jira-git-0.1.SNAPSHOT-jar-with-dependencies.jar amq $CHECKOUT $OUTPUT $START 2.6.x false 
