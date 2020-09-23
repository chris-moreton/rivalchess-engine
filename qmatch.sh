./version.sh 31.0.$1
cd ../rivalchess-uci
./publish.sh 31.0.$1 1
cd ../rivalchess-engine
chessmatch0.25 31.0.4-1 31.0.$1-1 100001

