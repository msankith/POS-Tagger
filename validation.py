def getfilecontents(filename):
	fh = open(filename,'r')
	contents = fh.read().strip()
	return contents

def foldfile(k,contents):
	sentences = contents.rstrip("._.").split("._.")
	foldsize = len(sentences)/k
	for i in range(k):
		train_handle = open("kfold/train"+str(i+1)+".txt",'w+')
		test_handle = open("kfold/test"+str(i+1)+".txt",'w+')
		for test in sentences[i*foldsize:i*foldsize+foldsize]:
			test_handle.write(test.strip()+" ._. ")
		for train in  sentences[:i*foldsize] + sentences[i*foldsize+foldsize:]:
			train_handle.write(train.strip()+" ._. ")
	
c = getfilecontents('minbrown.txt')
foldfile(5,c)
	
