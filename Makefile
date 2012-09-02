compile:
	lein uberjar
	cp target/rewryte.jar .

launch:
	./rewryte.sh
