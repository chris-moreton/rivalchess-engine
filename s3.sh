./version.sh $2
cd ../rivalchess-uci
./publish.sh $2 1
cd ../rivalchess-engine
aws s3 cp ~/Chess/rivalchess-$2-1.jar s3://rivalchess-jars

