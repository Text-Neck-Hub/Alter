.PHONY: run sync

run:
	./gradlew bootRun

sync:
	./gradlew --refresh-dependencies