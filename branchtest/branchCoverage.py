import random
import argparse
import sys
import csv
import numpy as np
from numpy import loadtxt

if __name__ == '__main__':
    outfile_name = 'result.txt'
    filenames = ['test_01.csv', 'test_02.csv', 'test_03.csv', 'test_04.csv', 'test_05.csv',
                 'test_06.csv', 'test_07.csv', 'test_08.csv', 'test_09.csv', 'test_10.csv']
    #filenames = ['test_02.csv']

    for i in range(len(filenames)):
        # Load all data from a specific funciton, each column represents a branch path
        try:
            data = loadtxt(filenames[i], dtype=int, delimiter=',')
        except IOError:
            print('Warning: ' + filenames[i] + ' not found.')
            continue

        # Sum over columns to see what branches are untested
        data = np.sum(data, axis=0)
        data[data > 0] = 1

        # If a branch has a sum > 0 it means it's tested somewhere
        branches = len(data)
        branchesCovered = len(data[data == 1])

        # Calculate branch coverage percentage
        coverage = round((branchesCovered/branches) * 100, 2)

        with open(outfile_name, 'a+') as outfile:
            outfile.write("–––––––––––––––––––––––––\n")
            outfile.write("Function ID: #" + str(i+1) + "\nBranches: " + str(branches) +
                          "\nBranches covered: " + str(branchesCovered) + "\nBranch coverage: " + str(coverage) + "%\n\n")
            # outfile.write("BranchID  -  Covered\n")
            for j in range(branches):
                outfile.write("Branch " + str(j+1) +
                              ": " + str('✅' if data[j] == 1 else '❌') + '\n')
            outfile.write("\n")
    sys.stdout.flush()
