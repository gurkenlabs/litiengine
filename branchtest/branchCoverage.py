import random
import argparse
import sys
import csv
import numpy as np
from numpy import loadtxt

if __name__ == '__main__':
    n = 10
    outfile_name = 'result.txt'
    filenames = ['test_01.csv', 'test_02.csv', 'test_03.csv', 'test_04.csv', 'test_05.csv',
                 'test_06.csv', 'test_07.csv', 'test_08.csv', 'test_09.csv', 'test_10.csv']

    for i in range(n):
        # Load all data from a specific funciton, each column represents a branch path
        try:
            data = loadtxt(filenames[i], dtype=int, delimiter=',')
        except IOError:
            print(filenames[i] + ' not found')
            continue

        # Sum over columns to see what branches are untested
        data = np.sum(data, axis=0)
        data[data == 0] = -1
        data[data != -1] = 0
        data = data*-1

        nb = len(data)
        coverage = round((nb - np.sum(data))/nb, 2)

        with open(outfile_name, 'w') as outfile:
            outfile.write("----------------------------------------\n")
            outfile.write("ID: " + str(i) + " Coverage: " +
                          str(coverage) + "% Branches: " + str(nb) + "\n")
            # outfile.write("BranchID  -  Covered\n")
            for j in range(nb):
                outfile.write(str(j+1) + " - " + str(data[j]) + '\n')
    sys.stdout.flush()
