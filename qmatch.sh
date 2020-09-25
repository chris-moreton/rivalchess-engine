./version.sh $2
cd ../rivalchess-uci
./publish.sh $2 1
cd ../rivalchess-engine
cutechess-cli -engine cmd="java -jar /home/chrismoreton/Chess/rivalchess-$1-1.jar" -engine cmd="java -jar /home/chrismoreton/Chess/rivalchess-$2-1.jar" -each proto=uci book="/home/chrismoreton/Chess/ProDeo.bin" timemargin=1500 st=0.25 -resign movecount=10 score=600 -rounds $3 -pgnout /home/chrismoreton/Chess/Rival$1-v-Rival$2-$3-rounds.pgn -epdout /home/chrismoreton/Chess/Rival$1-v-Rival$2-$3-rounds.epd $4

