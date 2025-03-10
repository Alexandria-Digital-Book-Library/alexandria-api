REMOTE_USER := aloussase
REMOTE_HOST := jupiter
JAR_FILE := alexandria-0.0.1-SNAPSHOT.jar

build:
	@./gradlew build --no-daemon -x test --continue

deploy: build
	@ssh ${REMOTE_USER}@${REMOTE_HOST} "mkdir -p /home/${REMOTE_USER}/.jars /home/${REMOTE_USER}/.services"
	@scp ./build/libs/${JAR_FILE} ${REMOTE_USER}@${REMOTE_HOST}:~/.jars/alexandria.jar
	@scp ./alexandria.service ${REMOTE_USER}@${REMOTE_HOST}:~/.services/alexandria.service

.PHONY: build
