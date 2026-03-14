# TP2 — GLO-3004 Publisher-Subscriber
# Usage:
#   make         → compile + build jar
#   make test    → compile + run BrokerTest
#   make run     → run with default params (n=2 p=2 s=3 t=100)
#   make clean   → delete out/ and tp2.jar

SRC     = $(wildcard src/*.java)
TEST    = test/BrokerTest.java test/StubBroker.java
OUT     = out
JAR     = tp2.jar

.PHONY: all test run clean

all: $(JAR)

$(OUT):
	mkdir -p $(OUT)

compile: $(OUT)
	javac -d $(OUT) $(SRC)

$(JAR): compile
	jar cfm $(JAR) MANIFEST.MF -C $(OUT) .
	@echo "Built $(JAR)"

test: $(OUT)
	javac -d $(OUT) $(SRC) $(TEST)
	java -cp $(OUT) BrokerTest

run: $(JAR)
	java -Dn=2 -Dp=2 -Ds=3 -Dt=100 -jar $(JAR)

clean:
	rm -rf $(OUT) $(JAR)
