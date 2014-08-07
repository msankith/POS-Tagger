from createMatrices import bigram_trainer
from trigram import trigram_trainer
from statistics import accuracy
from statistics import csv_reports
from statistics import processTags
from sys import argv

def getfilecontents(filename):
        fh = open(filename,'r')
        contents = fh.read().strip()
        return contents

def wordsfromtest(inputfile):
        c = getfilecontents(inputfile)
        words = c.split(' ')
        return words

corpusfile = argv[1]
inputfile = argv[2]
outputfile = argv[3]
basefile = argv[4]

# Step 1 - Train using bigrams or trigrams on the corpusfile
transition, emission = bigram_trainer(corpusfile)
#transition, emission = trigram_trainer(corpusfile)

# Step 2 - Get the word list from the input file
words = wordsfromtest(inputfile)

# Step 3 - Pass the word list and matrices to viterbi implementation
#prediction = viterbi(words,transition,emission)

prediction = 'input/base.txt' # just for testing this code without viterbi; to be removed after viterbi completed

''' 
viterbi() should return the PATH of the output file. 
Output file contains the predicted tags for words in the same format as the corpus
word1_tag1 word2_tag2 word3_tag3 and so on 
'''

# Step 4 - Get accuracy related statistics

stats = accuracy(prediction,basefile)
# the prediction would be compared against the basefile
# this could be the corpusfile itself or, we can use some other manually annotated basefile

stats.setTestSetAsBase() 
# Use this if you passed a manually annotated basefile for comparison

#stats.setMaxProbabAsBase()
# Use this if you want to consider maximum probability tag for each word as the correct tag for that word.

accuracy_percent = stats.percentage()
overall_p , overall_r = stats.precision_recall()
tags, cm = stats.confusion_matrix()
prfs = stats.precision_recall_fscore_support()


# Step 5 - Generate Reports
reports = csv_reports()

reports.confusion_csv(tags,cm,outputfile)
reports.prfs_csv(tags,prfs,outputfile,True)
reports.accuracy_csv(accuracy_percent,outputfile,True)
