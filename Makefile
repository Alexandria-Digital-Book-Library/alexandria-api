build:
	docker buildx build --platform linux/amd64,linux/arm64 -t aloussase/alexandria-api:latest .

push: build
	docker push aloussase/alexandria-api:latest


.PHONY: build push
