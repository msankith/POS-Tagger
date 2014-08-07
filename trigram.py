from statistics import processTags
import itertools
import copy

class AutoVivification(dict):
	def __getitem__(self,item):
		try:
			return dict.__getitem__(self,item)
		except KeyError:
			value = self[item] = type(self)()
			return value

class trigramStats:
	def __init__(self,corpusfile):
		self.parsed = processTags(corpusfile)
		self.tagTransitionProbabilities()
		self.findEmissionProbabilities()

	def tagTransitionProbabilities(self):
		self.transitionProbabilities = AutoVivification()
		for t1 in self.parsed.actual_tagset:
			for t2 in self.parsed.actual_tagset:
				for t3 in self.parsed.actual_tagset:
					self.transitionProbabilities[(t1,t2)][t3]=0					
		for t1,t2,t3 in itertools.izip(self.parsed.actual_tags,self.parsed.actual_tags[1:],self.parsed.actual_tags[2:]):
			self.transitionProbabilities[(t1,t2)][t3]+=1
		self.transitionCounts = copy.deepcopy(self.transitionProbabilities)
		#At this point, we just have the counts, not the probabilities. So this assignment is fine.
		for t1,t2 in self.transitionProbabilities.iteritems():
			total = sum(t2.values())
			for t3,t3count in t2.iteritems():
				if total!=0:
					self.transitionProbabilities[t1][t3] = 1.0*t3count/total

	def findEmissionProbabilities(self):
		self.emissionProbabilities = AutoVivification()
		for t1 in self.parsed.actual_tagset:
			for t2 in self.parsed.actual_tagset:
				for w in self.parsed.actual_wordset:
					self.emissionProbabilities[(t1,t2)][w]=0					
		for t1,t2,w in itertools.izip(self.parsed.actual_tags,self.parsed.actual_tags[1:],self.parsed.actual_words[1:]):
			self.emissionProbabilities[(t1,t2)][w]+=1
		self.emissionCounts = copy.deepcopy(self.emissionProbabilities)
		for t1,w in self.emissionProbabilities.iteritems():
			total = sum(w.values())
			for wkey,wcount in w.iteritems():
				if total!=0:
					self.emissionProbabilities[t1][wkey] = 1.0*wcount/total

	def getTransProbability(self,t1,t2,t3):
		return self.transitionProbabilities[(t1,t2)][t3]	

	def getEmissionProbability(self,t1,t2,w):
		return self.emissionProbabilities[(t1,t2)][w]

def trigram_trainer(corpusfile):
	trigramprobab = trigramStats(corpusfile)
	return trigramprobab.transitionProbabilities, trigramprobab.emissionProbabilities
