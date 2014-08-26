/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nlp_1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author msankith
 */
public class Viterbi implements Runnable{

    StringBuilder bigOut = new StringBuilder();
    int perThread = 1;
    
    boolean suffix=true, rule=true, goodTuring=false;
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
                   training t = new training("dummycorpus");
                   BufferedWriter out = new BufferedWriter(new FileWriter("/home/sanjeevmk/NetBeansProjects/PosTagger/src/nlp_1/output/output2_stemmed.txt"));
                   FileInputStream fin = new FileInputStream("/home/sanjeevmk/NetBeansProjects/PosTagger/src/nlp_1/input/training.ser");
		   ObjectInputStream ois = new ObjectInputStream(fin);
                   System.out.println("Reading training objects");
		   t = (training) ois.readObject();
                   System.out.println("Read all objects");
		   ois.close();
		   
		   Viterbi algo= new Viterbi("/home/sanjeevmk/NetBeansProjects/PosTagger/src/nlp_1/input/test2.txt", t,out);
                   System.out.println("calling parse file");
		   algo.parseFile();
    }
    
    @Override
    public void run() {
        int index;
        try {
            
            for(index=0; index<threadSet.size(); index++){
                String[] words = threadSet.get(index).split(" ");
                Viterbi_Algorithm2(words);
            }
            
            pos_output.put(id, bigOut.toString());
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        } catch (IOException ex) {
            Logger.getLogger(Viterbi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    
    class tree {
        String tag;
        ArrayList<tree> children;
        tree parent;
        double probability;
        public tree(String tag,tree parent,double prob)
        {
            this.tag=new String(tag);
            this.parent=parent;
            children= new ArrayList<tree>();
            probability=prob;
        }
        public tree(String tag,tree parent)
        {
            this.tag=new String(tag);
            this.parent=parent;
            children= new ArrayList<tree>();
            probability=0.0;
        }
 
    }
    
  String fileName;
  static  HashMap<String,Integer> TagSet,WordSet,wordCount;
  static  HashMap<String,Double> TransistionMatrix,EmissionMatrix, s1m,s2m,s3m,s4m;
  BufferedWriter output;
  static  HashMap<Integer,String> pos_output= new HashMap<Integer,String>();
  
  int id;
  ArrayList<String> threadSet = new ArrayList<String>();
  ArrayList<ArrayList<String>> setOfSets = new ArrayList<ArrayList<String>>();
  
    public Viterbi(String File,training t,BufferedWriter out)
    {
        fileName=File;
        TagSet = t.TagSet;
        WordSet=t.wordSet;
        wordCount = t.wordCount;
        TransistionMatrix = t.TransistionProbabilty;
        EmissionMatrix= t.wordProbability;
        s1m = t.s1t;
        s2m = t.s2t;
        s3m = t.s3t;
        s4m = t.s4t;
        
        output = out;
    }

    public Viterbi(int ID,ArrayList<String> lines) {
        this.id=ID;
        
        this.threadSet = lines;
    }
    
 	   
    public double getEmissionProb(String w, String pt, boolean known){
        
        double initProb = 1;
        
    	if(!known){
            
                if(rule){
                    String letter = w.substring(0,1);
                    boolean isUpperCase = !letter.equals(letter.toLowerCase());
                    
                    if(isUpperCase){
                        if(pt.equalsIgnoreCase("NP") || pt.equalsIgnoreCase("NP$") || pt.equalsIgnoreCase("NPS") || pt.equalsIgnoreCase("NPS$") )
                            initProb *= 0.25;
                    }
                }
                
                if(suffix){
                    String suff1=null, suff2=null, suff3=null, suff4=null;
                    
                    if(w.length()>=1){
                        suff1 = w.substring(w.length()-1);
                    }
                    if(w.length()>=2){
                        suff2 = w.substring(w.length()-2);
                    }
                    if(w.length()>=3){
                        suff3 = w.substring(w.length()-3);
                    }
                    if(w.length()>=4){
                        suff4 = w.substring(w.length()-4);
                    }
                    
                    Iterator tagIt = TagSet.entrySet().iterator();
                    String tag = null, maxtag=null;
                    double max = 0,p;
                    while(tagIt.hasNext()){
                        Map.Entry pair = (Map.Entry)tagIt.next();
                        tag = (String)pair.getKey();
                        
                        p = s4m.containsKey(suff4+"_"+tag)?(double)s4m.get(suff4+"_"+tag):0.0;
                        
                        if(p>max){
                            max = p;
                            maxtag = tag;
                        }
                    }
                    
                    if(max>0){
                        if(maxtag.equalsIgnoreCase(pt))
                            return 1.0*initProb;
                        else
                            return 0.0;
                    }else{
                        tagIt = TagSet.entrySet().iterator();
                        tag = null; maxtag=null;
                        max = 0; p=0;
                        while(tagIt.hasNext()){
                            Map.Entry pair = (Map.Entry)tagIt.next();
                            tag = (String)pair.getKey();
                        
                            p = s3m.containsKey(suff3+"_"+tag)?(double)s3m.get(suff3+"_"+tag):0.0;
                        
                            if(p>max){
                                max = p;
                                maxtag = tag;
                            }
                        }
                    
                        if(max>0){
                           if(maxtag.equalsIgnoreCase(pt))
                                return 1.0*initProb;
                           else
                                return 0.0;
                        }else{
                            tagIt = TagSet.entrySet().iterator();
                            tag = null; maxtag=null;
                            max = 0; p=0;
                            while(tagIt.hasNext()){
                                Map.Entry pair = (Map.Entry)tagIt.next();
                                tag = (String)pair.getKey();
                        
                                p = s2m.containsKey(suff2+"_"+tag)?(double)s2m.get(suff2+"_"+tag):0.0;
                        
                                if(p>max){
                                    max = p;
                                    maxtag = tag;
                                }
                            }
                            
                            if(max>0){
                                if(maxtag.equalsIgnoreCase(pt))
                                    return 1.0*initProb;
                                else
                                    return 0.0;
                            }else{
                                tagIt = TagSet.entrySet().iterator();
                                tag = null; maxtag=null;
                                max = 0; p=0;
                                while(tagIt.hasNext()){
                                    Map.Entry pair = (Map.Entry)tagIt.next();
                                    tag = (String)pair.getKey();
                        
                                    p = s2m.containsKey(suff2+"_"+tag)?(double)s2m.get(suff2+"_"+tag):0.0;
                        
                                    if(p>max){
                                        max = p;
                                        maxtag = tag;
                                    }
                                }
                                
                                if(max>0){
                                    if(maxtag.equalsIgnoreCase(pt))
                                        return 1.0*initProb;
                                    else
                                        return 0.0;
                                }else{
                                    return 1.0*initProb;
                                }
                            }
                        }
                    }
                        
                }else{
                    return (double) 0.00000001;
                }
    	}
        
        
            String key = w+"_"+pt;
            if(EmissionMatrix.containsKey(key)){
    		return (double) EmissionMatrix.get(key);
            }
    	
        
        return (double) 0.00000001;
    }
    
    public double getTransitionProb(String parentTag, String tagName){
    	
    	String key = parentTag+"_"+tagName;
    	if(TransistionMatrix.containsKey(key)){
    		return (double)TransistionMatrix.get(key);
    	}
    	return 0.00000001;
    }

    void parseFile() throws IOException
    {
        try {
            List<String> lines = new ArrayList<String>();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
                String line=null;
                
                System.out.println("Reading test lines");
                while((line=br.readLine())!=null)
                {
                    lines.add(line);
                    //String Words[]= line.split(" ");
                    //Viterbi_Algorithm2(Words);
                    
                }
                System.out.println("Read all lines");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Viterbi.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Viterbi.class.getName()).log(Level.SEVERE, null, ex);
            }
            int i=0;
            int setC = 0;
            System.out.println("total sentences in file "+ lines.size());
            ExecutorService threadPool = Executors.newFixedThreadPool(lines.size()/perThread);
            for(String l : lines){
                
                if(threadSet.size() < perThread){
                    
                    threadSet.add(l);
                    
                }else{
                    
                    ArrayList<String> tmpCopy = new ArrayList<String>();
                    tmpCopy.addAll(threadSet);
                    threadPool.execute(new Thread(new Viterbi(i,tmpCopy)));
                    i++;
                    //System.out.println(threadSet);
                    //setOfSets.add(tmpCopy);
                    
                    threadSet.clear();
                    threadSet.add(l);
                    
                }
            }
            
            ArrayList<String> tmpCopy = new ArrayList<String>();
            tmpCopy.addAll(threadSet);
            threadPool.execute(new Thread(new Viterbi(i,tmpCopy)));
                    
            threadPool.shutdown();
            // then wait for it to complete

            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            System.out.println("completed");
            
            //System.out.println(setOfSets);
            System.out.println(i+"  "+pos_output.size());
            Map<Integer,String> allOutputs = new TreeMap<Integer,String>(pos_output);
            Iterator outIterate = allOutputs.entrySet().iterator();
            while(outIterate.hasNext())
            {
                Map.Entry me = (Map.Entry)outIterate.next();
                //System.out.println(me.getValue());
                output.write((String)me.getValue());
            }
            output.close();
        } catch (InterruptedException ex) {
            Logger.getLogger(Viterbi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    void Viterbi_Algorithm2(String[] Words) throws IOException
    {
        tree root=new tree(".",null,1.0);
        tree present=root;
        String prev= ".";
        HashMap<String,Double> tagProb = new HashMap<String,Double>();
        HashMap<String,tree> levelTree = new HashMap<String,tree>();
        Iterator it = TagSet.entrySet().iterator();
        double prevProb=1;
        
        
      //  System.out.println("word "+".");
        
        while(it.hasNext())
        {
            Map.Entry tagPair = (Map.Entry)it.next();
            String tagName = (String)tagPair.getKey();
            double currentProb=getTransitionProb(prev,tagName);
            double tempProb=tagProb.containsKey(tagName)?(double)tagProb.get(tagName):(double)0;
            
            if(currentProb>tempProb)
            {
                tree tempTree=new tree(tagName,present,currentProb);
                present.children.add(tempTree);
                levelTree.put(tagName, tempTree);
                tagProb.put(tagName, currentProb);
              //  System.out.print("        "+tagName+" -> "+currentProb);
            }
        }
        
        HashMap<String,tree> level2Tree= levelTree;
        for(int i=0;i<Words.length;i++){
            levelTree=level2Tree;
            //level2Tree.clear();
            level2Tree= new HashMap<String,tree>();
            tagProb = new HashMap<String,Double>();
                
            String word = Words[i];
            Iterator levelIterator= levelTree.entrySet().iterator();
            
            boolean knownWord = WordSet.containsKey(Words[i]);
      //      System.out.println("");
         //   System.out.println("Word "+word);
            
            if(knownWord==false)
            {
                //System.out.println(Words[i]+ "    Unknown Word ");
            }
            while(levelIterator.hasNext())
            {
                Map.Entry parentTree = (Map.Entry) levelIterator.next();
                String parentTag= (String)parentTree.getKey();
                tree parentNode = (tree) parentTree.getValue();
                double parentProb = parentNode.probability;
                double emissionProb = getEmissionProb(word,parentTag,knownWord);
                 
                  
                Iterator tagIterator = TagSet.entrySet().iterator();
                //tagProb.clear();//to clear prev prob
                while(tagIterator.hasNext())
                {
                    Map.Entry tagPair = (Map.Entry) tagIterator.next();
                    String tagName = (String) tagPair.getKey();
                    double presentProb = tagProb.containsKey(tagName)?tagProb.get(tagName):(double)0;
                    double transmissionProb = getTransitionProb(parentTag,tagName);
                    
                    double probability = parentProb * emissionProb * transmissionProb * 1000;
            //        System.out.println(word+" --- "+parentTag+"_"+tagName+"  -> "+emissionProb+"  "+transmissionProb);
                    if(probability>presentProb)
                    {
                        level2Tree.put(tagName,new tree(tagName,parentNode,probability));
                        tagProb.put(tagName,probability);
                      //  System.out.print("            "+tagName+"   "+probability);
                    }
                }
            }
        }
        
        Iterator tagItr = tagProb.entrySet().iterator();
        String maxTag = null;
        double maxProb= 0.0;
        while(tagItr.hasNext())
        {
            Map.Entry tagPair = (Map.Entry)tagItr.next();
            double curProb = (double)tagPair.getValue();
         //   System.out.print("          "+(String)tagPair.getKey()+" ->"+curProb);
            if(curProb>maxProb)
            {
                maxProb=(double)tagPair.getValue();
                maxTag = (String)tagPair.getKey();
            }
        }
     
        
        tree node = level2Tree.get(maxTag);
        
        Stack<String> tags = new Stack<String>();
            
        //ArrayList<String> tags = new ArrayList<String>();
        while(node!=null)
        {
         //   System.out.println("tag --- "+ node.tag);
            //tags.add(node.tag);
            tags.push(node.tag);
            node=node.parent;
        }
      //  System.out.println("--------------------");
        if(tags.isEmpty()==true)
        {
            System.out.println("Something went wrong while POS tagging . Probability is tending to 0");
            for(int i=0;i<Words.length;i++)
            {
                System.out.print(Words[i]+" ");
                output.write(Words[i]+" ");
            }
            System.out.println("");
            
        }else {
            tags.pop();
            StringBuilder out= new StringBuilder() ;
            for(int i=0;i<Words.length;i++)
            {
                //out.append(Words[i]+"_"+tags.pop()+" ");
                bigOut.append(Words[i]+"_"+tags.pop()+" ");
               // String out=Words[i]+"_"+tags.pop()+" ";
        //    System.out.print(out);
                //output.write(out);
                
             }
           // System.out.println(id);
            //pos_output.put(this.id,out.toString());
        }
    }
    
    void printTrans()
    {
        
        System.out.println("printing transisiton ");
        Iterator it = TransistionMatrix.entrySet().iterator();
        
        while(it.hasNext())
        {
            Map.Entry rowPair = (Map.Entry)it.next();
            String tagName=(String)rowPair.getKey();
            
           // System.out.println(tagName+"      "+rowPair.getValue());
        }
    }
    
    double foffW(double n, String tag){
        double fof = 0;
        
        if(!goodTuring){
            return 0;
        }
        
        Iterator wordIterator = WordSet.entrySet().iterator();
        
        while(wordIterator.hasNext()){
            Map.Entry pair = (Map.Entry) wordIterator.next();
            String word = (String)pair.getKey();
            
            if(wordCount.get(word+"_"+tag) == n){
               fof++; 
            }
        }
        
        return fof;
    }
}