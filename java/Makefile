build-open: clean FORCE
	mvn -q -f pom-open.xml install deploy -DskipTests
	
build: clean FORCE
	mvn -q clean install package -DskipTests

test: FORCE
	mvn -q clean install test

format: FORCE
	mvn -q -DskipTests net.revelc.code.formatter:formatter-maven-plugin:format

check-for-updates: FORCE
	mvn -U versions:display-dependency-updates|grep ']   '|sort -u

validate: FORCE
	mvn -q -DskipTests net.revelc.code.formatter:formatter-maven-plugin:validate
	mvn -q clean install test

clean: FORCE
	mvn -q clean

FORCE:
