from sklearn.metrics import classification_report
from sklearn.metrics import confusion_matrix
from sklearn.metrics import accuracy_score
from sklearn.metrics import precision_score
from sklearn.metrics import recall_score
from sklearn.metrics import precision_recall_fscore_support

import csv
import itertools
import operator
import matplotlib.pyplot as plt
import re

class processTags:
	def __init__(self,basefile):
		# get the word-tag mappings from the basefile. This could be the entire corpus, or just the test set file
		filehandle = open(basefile,'r')
		self.contents = filehandle.read().strip()
		self.actual_words = [mappings.split('_')[0] for mappings in self.contents.split(' ')]
		self.actual_tags = [mappings.split('_')[1] for mappings in self.contents.split(' ')]
		self.actual_mintags = [re.sub("(-HL)|(-TL)|(-NC)|(FW-)", "", x) for x in self.actual_tags]
		self.actual_wordset = list(set(self.actual_words))
		self.actual_tagset = list(set(self.actual_tags))
		self.actual_mintagset = list(set(self.actual_mintags))
		self.actual_wordset.sort()
		self.actual_tagset.sort()
		self.actual_mintagset.sort()
		
	def wordsOverTagsTable(self):
		self.word_over_tags = {w:{t:0 for t in self.actual_tagset} for w in self.actual_wordset}
		for w,t in itertools.izip(self.actual_words,self.actual_tags):
			self.word_over_tags[w][t]+=1
		for w in self.actual_wordset:
			total = self.actual_words.count(w)
			for t in self.actual_tagset:
				c = self.word_over_tags[w][t]
				c=1.0*c/total
				self.word_over_tags[w][t]=c

	def getActualTagsByProbability(self):
		self.wordsOverTagsTable()
		actualtags = []
		for w in self.actual_words:
			actualtags.append(max(self.word_over_tags[w],key=self.word_over_tags[w].get))
		return actualtags

	def getActualTagsFromTestSet(self):
		return self.actual_tags

	def getPredictedTags(self,resultfile):
		# get the word-tag predictions from the result file
		filehandle = open(resultfile,'r')
		self.result_contents = filehandle.read().strip()
		self.result_tags = [mappings.split('_')[1] for mappings in self.result_contents.split(' ')]
		return self.result_tags
	
	def tagsUnion(self):
		u = self.actual_tags + self.result_tags
		u = list(set(u))
		u.sort()
		return u

class accuracy:
	def __init__(self,resultfile,basefile):
		self.output = processTags(basefile)
		self.predicted = self.output.getPredictedTags(resultfile)

	def precision_recall(self):
		return precision_score(self.actual,self.predicted),recall_score(self.actual,self.predicted)

	def precision_recall_fscore_support(self):
		return precision_recall_fscore_support(self.actual,self.predicted)

	def confusion_matrix(self):
		union = self.output.tagsUnion()
		return union,confusion_matrix(self.actual,self.predicted)
	
	def percentage(self):
		return 100.0*accuracy_score(self.actual,self.predicted)

	def setTestSetAsBase(self):
		self.actual = self.output.getActualTagsFromTestSet()
	
	def setMaxProbabAsBase(self):
		self.actual = self.output.getActualTagsByProbability()	
	
	def report(self):
		return classification_report(self.actual,self.predicted)

class csv_reports:
	def confusion_csv(self,labels,confusion_matrix,filename,append=False):
		header = 0 
		headerrow = []
		if append:
			fh = open(filename,'a')
		else:
			fh = open(filename,'wb+')

		with fh as outfile:
			writer = csv.writer(outfile)
			writer.writerow([])
			if header == 0:
				headerrow.insert(0,"TAGS")
				for tag in labels:
					headerrow.append(tag)
	    
				header = 1 
				writer.writerow(headerrow)
	    
			for i in range(len(labels)):
				row = []
				row.insert(0,labels[i])
				for j in range(len(labels)):
					row.append(confusion_matrix[i][j])
				writer.writerow(row)

	def prfs_csv(self,labels,prfs,filename,append=False):
		header = 0 
		headerrow = ['','precision','recall','f1-score','support']
		
		if append:
			fh = open(filename,'a')
		else:
			fh = open(filename,'wb+')

		with fh as outfile:
			writer = csv.writer(outfile)
			writer.writerow([])
			if header == 0:
				writer.writerow(headerrow)
				header=1
			
			for i in range(len(labels)):
				row = []
				row.insert(0,labels[i])
				row.append(prfs[0][i])
				row.append(prfs[1][i])
				row.append(prfs[2][i])
				row.append(prfs[3][i])
				writer.writerow(row)

	def accuracy_csv(self,accuracy,filename,append=False):
		if append:
			fh = open(filename,'a')
		else:
			fh = open(filename,'wb+')
		
		row = ["Accuracy",accuracy]

		with fh as outfile:
			writer = csv.writer(outfile)
			writer.writerow([])
			writer.writerow(row)

	def overall_csv(self,accuracy,precision,recall,append=False):
		if append:
			fh = open(filename,'a')
		else:
			fh = open(filename,'wb+')

		row1 = ["Overall Accuracy",accuracy]
		row2 = ["Overall Precision",precision]
		row3 = ["Overall Recall",recall]

		with fh as outfile:
			writer = csv.writer(outfile)
			writer.writerow(row1)
			writer.writerow(row2)
			writer.writerow(row3)
