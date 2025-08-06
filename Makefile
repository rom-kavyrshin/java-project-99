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

image-build:
	docker build -t rom-kavyrshin/java-project-99 -f Dockerfile .

image-run:
	docker run -p 8080:7070 --env-file local.env rom-kavyrshin/java-project-99

image-run-it:
	docker run -it -p 8080:7070 --env-file local.env rom-kavyrshin/java-project-99 bash

.PHONY: build