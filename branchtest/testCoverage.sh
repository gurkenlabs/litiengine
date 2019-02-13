# To run
# > chmod u+x testCoverage.sh
# > ./testCoverage.sh

echo "-----------------Running test coverage------------------"

# Delete all old .csv files
find *.csv -type f -delete

# Run gradle build followed by gradle test
cd ../ 
gradle cleanBuild build
gradle cleanTest test

cd branchtest/
# Run branch coverage script
python3 branchCoverage.py

echo < "result.txt"