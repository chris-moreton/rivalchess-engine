Rival Chess Java Engine
=======================

The Rival chess Java engine as used in the Rival Chess Android App.

This is a UCI-compatible engine.  The C++ UCI engine was converted to Java was an almost complete rewrite of the C++ code. 

It plays about 200-300 ECO higher than the old C++ engine when compared on the same hardware.

## Build

    ./gradlew build
    
## Run
    java -jar build/libs/rival-chess-android-engine.jar
    
## Play

Detailed instructions on the Universal Chess Interface (UCI) can be found [here](http://wbec-ridderkerk.nl/html/UCIProtocol.html).

Here is a brief example of how to use the command line interface.

    Hi
    ucinewgame
    position startpos
    go depth 3
    info currmove g1h3 currmovenumber 20 depth 3 score cp 25 pv g1f3 g8f6 e2e3  time 0 nodes 113 nps 0
    bestmove g1f3
    move e2e3
    go depth 3
    info currmove g1h3 currmovenumber 20 depth 3 score cp 25 pv g1f3 g8f6 e2e3  time 1 nodes 108 nps 108000
    bestmove g1f3