#!/bin/bash

# To run
# > chmod u+x testCoverage.sh
# > ./testCoverage.sh

echo "----------------- Running test coverage ------------------"

# Delete all old .csv files
echo " * Deleting old .csv files..."
find *.csv -type f -delete

# Run gradle build followed by gradle test
echo " * Building and testing..."
cd ../ 
gradle build
gradle cleanTest test

cd branchtest/
# Run branch coverage script
echo " * Running the branch coverage python script..."
python3 branchCoverage.py

echo < "result.txt"
echo "✅ Result printed to branchtest/result.txt"