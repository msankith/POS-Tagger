# -*- coding: utf-8 -*-
"""
POS Tagger
Assignment 1
CS 626: NLP
Eeshan Malhotra
"""

import itertools
import csv
import re

def bigram_trainer(filename):
	### Load Corpus
	filehandle = open(filename, 'r')

	text = filehandle.read()
	text=text.strip() #there's a trailing space at the end of the file

	words = [x.split('_')[0] for x in text.split(' ')]
	tags  = [x.split('_')[1] for x in text.split(' ')]

	#If we want to remove extra-information tags:
	tags = [re.sub("(-HL)|(-TL)|(-NC)|(FW-)", "", x) for x in tags]



	wordset = list(set(words))
	tagset  = list(set(tags))

	wordset.sort()
	tagset.sort()

	#Basic Statistics
	print "Total length:", len(words)
	print "Unique word count:", len(wordset)
	print "Unique tag count:", len(tagset)
	print 

	## Step 1
	## Create Emission Matrix
	print "Creating Emission matrix...",
	#Initialize everything with a 0
	emissioncounts = {t:{w:0 for w in wordset} for t in tagset}

	for w,t in itertools.izip(words, tags):
	    emissioncounts[t][w]+=1

	#if not initialising with 0s
	#df = DataFrame(emissioncounts).T.fillna(0)
	print "Done"


	header=0
	print "Writing Emission matrix...",
	with open('output/emission2.csv', 'wb') as outfile:
	    writer = csv.writer(outfile)
	    if header==0:
		headerrow=emissioncounts.values()[0].keys()
		headerrow.insert(0,"TAG")
		writer.writerow(headerrow)
		header=1
		
	    for t, w in emissioncounts.iteritems():
		counts = w.values()
		total = sum(counts)
		counts = [1.0*c/total for c in counts] #normalizing
		#maybe add a smoothing factor?

	#        smoothfactor=1
	#        unknownmass = 5 #guess
	#        total2 = sum(counts) + (smoothfactor * len(wordset)) + unknownmass
	#        counts2 = [1.0*(c+smoothfactor)/total2 for c in counts] #normalizing


		counts.insert(0, t)
		
		writer.writerow(counts)
	    
	print "Done"




	## Step 2
	## Transition Matrix

	print
	print "Creating Transition matrix...",
	wordset.sort()
	tagset.sort()
	tagset2=tagset
	#Initialize everything with a 0
	transitioncounts = {t1:{t2:0 for t2 in tagset2} for t1 in tagset}

	for t1, t2 in itertools.izip(tags, tags[1:]):
	    transitioncounts[t1][t2]+=1

	print "Done"

	header=0
	print "Writing Transition matrix...",
	with open('output/transition2.csv', 'wb') as outfile:
	    writer = csv.writer(outfile)
	    if header==0:
		headerrow=transitioncounts.values()[0].keys()
		headerrow.insert(0,"TAG")
		writer.writerow(headerrow)
		header=1
		
	    for t1, t2 in transitioncounts.iteritems():
		counts = t2.values()
		total = sum(counts)
		counts = [1.0*c/total for c in counts] #normalizing
		#maybe add a smoothing factor?
		
	#        smoothfactor=1
	#        unknownmass = 5 #guess
	#        total2 = sum(counts) + (smoothfactor * len(tagset)) + unknownmass
	#        counts2 = [1.0*(c+smoothfactor)/total2 for c in counts] #normalizing
		
		counts.insert(0, t1)
		
		writer.writerow(counts)
	    
	print "Done"
	
	transitionprobab = {t1:{t2:0 for t2 in tagset2} for t1 in tagset}
	for t1 in tagset:
		total = sum(transitioncounts[t1].values())
		for t2 in tagset:
			transitionprobab[t1][t2] = 1.0*transitioncounts[t1][t2]/total

	emissionprobab = {t:{w:0 for w in wordset} for t in tagset}

	for t in tagset:
		total = sum(emissioncounts[t].values())
		for w in wordset:
			emissionprobab[t][w] = 1.0*emissioncounts[t][w]/total

	return transitionprobab,emissionprobab
