clean:
	./gradlew clean

install:
	./gradlew installDist

build:
	./gradlew clean build

dev:
	./gradlew run

checkstyleMain:
	./gradlew checkstyleMain
checkstyleTest:
	./gradlew checkstyleTest
checkstyle: checkstyleMain checkstyleTest

test:
	./gradlew cleanTest test

report:
	./gradlew jacocoTestReport

.PHONY: build