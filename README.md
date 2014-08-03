# README #

### POS Tagger / Assignment 1 ###

* POS tagger for CS 626 - Natural Language Processing

**how to use statistics module:**  
***1.*** from statistics import accuracy  
***2.*** acc = accuracy('resultfile.txt').  
The resultfile parameter passed should be the output file after tagging the input text.  
***3.*** acc.precision_recall()  
// to get the precision/recall of the tagged output  
***4.*** acc.confusion_matrix()  
// to generate the confusion matrix on the tagged output
  
**how to use trigram module:**  
***1.*** from trigram import trigramStats  
***2.*** trigramProbab = new trigramStats('brown.txt')  
// pass the corpus. Transition and Emission probabilities will be calculated into tables  
***3.*** trigramProbab.getTransitionProbability(tag1,tag2,tag3)  
// this returns the probability of occurance of tag3 , having already seen tag1 and tag2 consecutively  
***4.*** trigramProbab.getEmissionProbability(tag1,tag2,word)  
// this returns the probability of occurance of a word having already seen tag1 and tag2 consecutively  
  