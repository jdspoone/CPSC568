#DEBUG = -DEBUG_PARSER=true
#DEBUG=-DEBUG_TOKEN_MANAGER=true
JAVACC_BIN = $(HOME)/JavaCC-3.2/bin

all:SLParser.java 

SLParser.java:SLParser.jj
	$(JAVACC_BIN)/javacc -NOSTATIC $(DEBUG) SLParser.jj
	$(JAVACC_BIN)/jjdoc SLParser.jj



