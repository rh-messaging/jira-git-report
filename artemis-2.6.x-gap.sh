export CHECKOUT=$1
export OUTPUT=$2
export START=5e5ae404174d61735cf5283b59da59460c36d629

java -jar ./target/jira-git-0.1.SNAPSHOT-jar-with-dependencies.jar artemis $CHECKOUT $OUTPUT $START master true 2.6.x 2.6.0
