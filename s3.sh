./version.sh $1
cd ../rivalchess-uci
./publish.sh $1 1
cd ../rivalchess-engine
aws s3 cp ~/Chess/rivalchess-$1-1.jar s3://rivalchess-jars

