Usage

statistics module:
1. from statistics import accuracy
2. acc = accuracy('resultfile.txt') . 
The resultfile parameter passed should be the output file after tagging the input text.
3. acc.precision_recall() 
// to get the precision/recall of the tagged output
4. acc.confusion_matrix()
// to generate the confusion matrix on the tagged output
