sed -i "s/Rival 1.0.3 |.*|/Rival 1.0.3 |$1|/g" src/com/netsensia/rivalchess/uci/UCIController.java
sed -i "s/version.*/version '1.0.3-$1'/g" build.gradle
./gradlew build
cp build/libs/rivalchess-engine-1.0.3-$1.jar ~/ChessEngines


