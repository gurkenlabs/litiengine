# To run
# > chmod u+x testCoverage.sh
# > ./testCoverage.sh

echo "-----------------Running test coverage------------------"

# Delete all old csv files
find *.csv -type f -delete

# Run gradle build
# TODO: only runs if code updates! This is a sligt problem
cd ../ 
gradle build

cd branchtest/
# Run branch coverage script
python3 branchCoverage.py

echo < "result.txt"