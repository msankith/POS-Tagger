from statistics import accuracy
from statistics import csv_reports
from createMatrices import bigram_trainer
import csv
from sys import argv

foldaccuracy = []
foldprecision = []
foldrecall = []
tagses = []
foldcms = []
prfs = []
reports = []

def getfilecontents(filename):
	fh = open(filename,'r')
	contents = fh.read().strip()
	return contents

def foldfile(k,contents):
	sentences = contents.rstrip('._.').split(' ._. ')
	foldsize = len(sentences)/k
	for i in range(k):
		train_handle = open("kfold/train"+str(i+1)+".txt",'w+')
		test_handle = open("kfold/test"+str(i+1)+".txt",'w+')
		base_handle = open("kfold/base"+str(i+1)+".txt",'w+')
		for test in sentences[i*foldsize:i*foldsize+foldsize]:
			base_handle.write(test.strip()+" ._. ")
			words = [mapping.split('_')[0] for mapping in test.split(' ')]
			for w in words[:-1]:
				test_handle.write(w+' ')
			test_handle.write(words[len(words)-1]+". ")
		for train in  sentences[:i*foldsize] + sentences[i*foldsize+foldsize:]:
			train_handle.write(train.strip()+" ._. ")

def wordsfromtest(inputfile):
	c = getfilecontents(inputfile)
	words = c.split(' ')
	return words
	
def kfold(k,corpus):	
	c = getfilecontents(corpus)
	foldfile(k,c)

	for i in range(k):
		transition,emission = bigram_trainer("kfold/train"+str(i+1)+".txt")
		
		#transition,emission = trigram_trainer("kfold/train"+str(i+1)+".txt")

		words = wordsfromtest("kfold/test"+str(i+1)+".txt")
		#prediction = viterbi(words,transition,emission) #should return a filename
		prediction = "kfold/base"+str(i+1)+".txt"

		stats = accuracy(prediction,"kfold/base"+str(i+1)+".txt")
		stats.setTestSetAsBase()

		foldaccuracy.append(stats.percentage())
		p,r = stats.precision_recall()
		foldprecision.append(p)
		foldrecall.append(r)

		tags, cm = stats.confusion_matrix()
		tagses.append(tags)		
		foldcms.append(cm)

		prfs.append(stats.precision_recall_fscore_support())
		reports.append(stats.report())

	overalla = 0.0
	overallp = 0.0
	overallr = 0.0	

	for i in range(k):
		overalla += foldaccuracy[i]
		overallp += foldprecision[i]
		overallr += foldrecall[i]

	overalla = overalla/k
	overallp = overallp/k
	overallr = overallr/k
	
	print
	print "Accuracy:", overalla
	print "Precision:", overallp
	print "Recall:", overallr
	

def writereports(k):
	reports = csv_reports()
	for i in range(k):
		cmfname = "kfold/report"+str(i+1)+".csv"
		taglist = tagses[i]
		cm = foldcms[i]
		prf = prfs[i]
		reports.confusion_csv(taglist,cm,cmfname)
		reports.prfs_csv(taglist,prf,cmfname,True)
		reports.accuracy_csv(foldaccuracy[i],cmfname,True)

filename = argv[1]
kfold(5,filename)
writereports(5)
