from nltk.stem.wordnet import WordNetLemmatizer
from nltk.corpus import wordnet
from sys import argv
import re
import itertools

def getfilecontents(filename):
        fh = open(filename,'r')
        contents = fh.read().strip()
        return contents

def getwordsandtags(text):
	words = [x.split('_')[0] for x in text.split(' ')]
	tags  = [x.split('_')[1] for x in text.split(' ')]
	#If we want to remove extra-information tags:
	tags = [re.sub("(-HL)|(-TL)|(-NC)|(FW-)", "", x) for x in tags]
	return words,tags

def wordstostem(words):
	lmtzr = WordNetLemmatizer()
	wordstems = []
	for w in words:
		wstem = lmtzr.lemmatize(w)
		wordstems.append(wstem)
	return wordstems

def newcorpus(words,tags,filename):
	corpus_h = open(filename,'w+')
	for w,t in zip(words,tags):
		corpus_h.write(w+'_'+t+' ')

filename = argv[1]
c = getfilecontents(filename)
w,t = getwordsandtags(c)
ws = wordstostem(w)
newcorpus(ws,t,'input/stembrown.txt')
		
