include general.env
export

ifneq (,$(wildcard local.env))
    include local.env
    export
endif

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
	docker build --secret id=sentry_auth_token,src=sentry_auth_token.secret \
	-t rom-kavyrshin/java-project-99 -f Dockerfile .

image-build-debug:
	docker build --progress=plain --no-cache --build-arg MY_VAR=somevalue213 \
	-t rom-kavyrshin/java-project-99 -f Dockerfile .

image-run:
	docker run -p 7070:7070 --env-file local.env rom-kavyrshin/java-project-99

image-run-it:
	docker run -it -p 7070:7070 --env-file local.env rom-kavyrshin/java-project-99 bash

frontend-install:
	npm i @hexlet/java-task-manager-frontend

frontend-build:
	npx build-frontend

.PHONY: build