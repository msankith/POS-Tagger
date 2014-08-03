from sklearn.metrics import classification_report
from sklearn.metrics import confusion_matrix
import itertools
import operator
import matplotlib.pyplot as plt
import re

class processTags:
	def __init__(self,resultfile):
		filehandle = open(resultfile,'r')
		self.contents = filehandle.read().strip()
		self.words = [mappings.split('_')[0] for mappings in self.contents.split(' ')]
		self.tags = [mappings.split('_')[1] for mappings in self.contents.split(' ')]
		self.mintags = [re.sub("(-HL)|(-TL)|(-NC)|(FW-)", "", x) for x in self.tags]
		self.wordset = list(set(self.words))
		self.tagset = list(set(self.tags))
		self.mintagset = list(set(self.mintags))
		self.wordset.sort()
		self.tagset.sort()
		self.mintagset.sort()

	def getPredictedTags(self):
		return self.tags

	def getActualTags(self):
		self.wordsOverTagsTable()
		actualtags = []
		for w in self.words:
			actualtags.append(max(self.word_over_tags[w],key=self.word_over_tags[w].get))
		return actualtags

				
	def wordsOverTagsTable(self):
		self.word_over_tags = {w:{t:0 for t in self.tagset} for w in self.wordset}
		for w,t in itertools.izip(self.words,self.tags):
			self.word_over_tags[w][t]+=1
		for w in self.wordset:
			total = self.words.count(w)
			for t in self.tagset:
				c = self.word_over_tags[w][t]
				c=1.0*c/total
				self.word_over_tags[w][t]=c

class accuracy:
	def __init__(self,resultfile):
		output = processTags(resultfile)
		self.predicted = output.getPredictedTags()
		self.actual = output.getActualTags()

	def precision_recall(self):
		print classification_report(self.actual,self.predicted)	

	def confusion_matrix(self):
		cm = confusion_matrix(self.actual,self.predicted)
		plt.matshow(cm)
		plt.title('Confusion Matrix')
		plt.colorbar()
		plt.ylabel('True Label')
		plt.xlabel('Predicted Label')
		plt.show()

