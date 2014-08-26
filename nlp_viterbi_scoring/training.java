/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nlp_1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author msankith
 */
public class training implements Serializable {
    
    File Corpus;
    public HashMap<String,Integer> TagSet,TagTransitionCount,wordCount, transitionSingletons;
    public HashMap<String,Double> TransistionProbabilty,wordProbability, transitionBackoff, emissionBackoff;
    public static HashMap<String,Double> unknownProb;
    public HashMap<String,Integer> s1,s2,s3,s4;
    public HashMap<String,Double> s1t,s2t,s3t,s4t;
    HashMap<String,Integer> wordSet;
    double total;
    double vocab;
    int oneCount = 1;
    boolean goodTuring = true;
    boolean suffix = false;
    
    training(String Corpus){
        
        this.Corpus=new File(Corpus);
        TagSet=new HashMap<String,Integer>();
        TagTransitionCount=new HashMap<String,Integer>();
        TransistionProbabilty = new HashMap<String,Double>();
        transitionBackoff = new HashMap<String,Double>();
        emissionBackoff = new HashMap<String,Double>();
        wordProbability= new HashMap<String,Double>();
        wordCount = new HashMap<String,Integer>();
        wordSet=new HashMap<String,Integer>();
        transitionSingletons = new HashMap<String,Integer>();
        
        unknownProb = new HashMap<String,Double>();
        
        s1 = new HashMap<String,Integer>();
        s2 = new HashMap<String,Integer>();
        s3 = new HashMap<String,Integer>();
        s4 = new HashMap<String,Integer>();
        
        s1t = new HashMap<String,Double>();
        s2t = new HashMap<String,Double>();
        s3t = new HashMap<String,Double>();
        s4t = new HashMap<String,Double>();
    }
   
    void parseUnknownFile(File unk) throws IOException{
        String tag;
        double prob;
        
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(unk)));
            String line = null;
            
            while((line=br.readLine())!=null){
                tag = line.split("\\t")[0];
                prob = Double.parseDouble(line.split("\\t")[1]);
                
                unknownProb.put(tag, prob);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(training.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    void parseFile()
    {
        int singleCount;
        
        String suff1="",suff2="",suff3="",suff4="";
        
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Corpus)));
            
            String line=null;
           
            while((line=br.readLine())!=null)
            {
                String[] words_tag=line.split("\\s+");
                System.out.println("Total data count in corpus  "+ words_tag.length);
                total = words_tag.length;
                String history=new String(".");
          
               
                if(!transitionSingletons.containsKey(history)){
                        transitionSingletons.put(history, 0);
                }
                
                
                for(String str : words_tag)
                {
                    String[] tagSplit = str.split("_");
                    tagSplit[1]=tagSplit[1].replaceAll("(-HL)|(-TL)|(-NC)|(FW-)", "");
               
                    String w = tagSplit[0];
                    String t = tagSplit[1];
                    suff1=""; suff2=""; suff3=""; suff4="";
                    
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
                    
                    double count = s1.containsKey(suff1)?s1.get(suff1):0;
                    s1.put(suff1, (int)count+1);
                    
                    count = s2.containsKey(suff1)?s2.get(suff1):0;
                    s2.put(suff2, (int)count+1);
                    
                    count = s3.containsKey(suff1)?s3.get(suff1):0;
                    s3.put(suff3, (int)count+1);
                    
                    count = s4.containsKey(suff1)?s4.get(suff1):0;
                    s4.put(suff4, (int)count+1);
                    
                    count = s1t.containsKey(suff1+"_"+t)?s1t.get(suff1+"_"+t):0;
                    s1t.put(suff1+"_"+t, count+1);
                    
                    count = s2t.containsKey(suff2+"_"+t)?s2t.get(suff2+"_"+t):0;
                    s2t.put(suff2+"_"+t, count+1);
                    
                    count = s3t.containsKey(suff3+"_"+t)?s3t.get(suff3+"_"+t):0;
                    s3t.put(suff3+"_"+t, count+1);
                    
                    count = s4t.containsKey(suff4+"_"+t)?s4t.get(suff4+"_"+t):0;
                    s4t.put(suff4+"_"+t, count+1);
                    
                    //tagSplit[1]=tagSplit[1].replaceAll("(-[A-Z]+)|(\\+[A-Z]+)|\\*|\\$", "");
                    
                  //  System.out.print(tagSplit[0]+" ");
                    //System.out.println(str+"    "+tagSplit[0]+" -> "+tagSplit[1]);
                //    bw.write("\""+str+"\",\""+tagSplit[0]+"\",\""+tagSplit[1]+"\"\n");
                    count=TagSet.containsKey(tagSplit[1])?TagSet.get(tagSplit[1]):0;
                    TagSet.put(tagSplit[1],(int)count+1);
                    
                    if(!transitionSingletons.containsKey(tagSplit[1])){
                        transitionSingletons.put(tagSplit[1], 0);
                    }
                    
                    count=TagTransitionCount.containsKey(history+"_"+tagSplit[1])?TagTransitionCount.get(history+"_"+tagSplit[1]):0;
                    
                    if(count == 0){
                        singleCount = transitionSingletons.get(history);
                        transitionSingletons.put(history, singleCount+1);
                    }
                    if(count == 1){
                        singleCount = transitionSingletons.get(history);
                        transitionSingletons.put(history, singleCount-1);
                    }
                    
                    TagTransitionCount.put(history+"_"+tagSplit[1],(int)count+1);
                    history=tagSplit[1];
                    
                    count=wordCount.containsKey(tagSplit[0]+"_"+tagSplit[1])?wordCount.get(tagSplit[0]+"_"+tagSplit[1]):0;
                    

                    wordCount.put(tagSplit[0]+"_"+tagSplit[1],(int)count+1);
                    
                    count=wordSet.containsKey(tagSplit[0])?wordSet.get(tagSplit[0]):0;
                    wordSet.put(tagSplit[0],(int)count+1);
                }
            }
            
            
            if(goodTuring){
                Map.Entry pair, pair2;
                String tag1,tag2,word;
            
                Iterator tag1it = TagSet.entrySet().iterator();
                
                while(tag1it.hasNext()){
                    pair = (Map.Entry) tag1it.next();
                    tag1 = (String)pair.getKey();
                    
                    Iterator tag2it = TagSet.entrySet().iterator();
                    
                    while(tag2it.hasNext()){
                        pair2 = (Map.Entry) tag2it.next();
                        tag2 = (String)pair2.getKey();
                        
                        if(!TagTransitionCount.containsKey(tag1+"_"+tag2)){
                            TagTransitionCount.put(tag1+"_"+tag2, 0);
                        }
                    }
                }
                
            }
            
            vocab = wordSet.size();
            
            transitionBackOff();
            getTransisitionMatrix();
            System.out.println("Transition matrix done");
            getEmissionProbability();
            System.out.println("Emission matrix done");
            
            if(suffix){
                getSuffixMatrix(1);
                getSuffixMatrix(2);
                getSuffixMatrix(3);
                getSuffixMatrix(4);
                System.out.println("Suffixes done");
            }
        } 
        catch (FileNotFoundException ex) {
            Logger.getLogger(training.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (Exception ex) {
            Logger.getLogger(training.class.getName()).log(Level.SEVERE, null, ex);
        }
         
    }
            
    void getSuffixMatrix(int l){
        String suff="",tag="";
        double suffcount;
        
        if(l==1){
            Iterator sIt = s1.entrySet().iterator();
            
            while(sIt.hasNext()){
                Map.Entry pair = (Map.Entry)sIt.next();
                suff = (String)pair.getKey();
                suffcount = (int)pair.getValue();
                
                Iterator tagIt = TagSet.entrySet().iterator();
                
                while(tagIt.hasNext()){
                    Map.Entry pairT = (Map.Entry)tagIt.next();
                    tag=(String)pairT.getKey();
                    
                    if(s1t.containsKey(suff+"_"+tag)){
                        double c = s1t.get(suff+"_"+tag);
                        c = c/suffcount;
                        s1t.put(suff+"_"+tag, c);
                    }
                }
            }
        }
        
        if(l==2){
            Iterator sIt = s2.entrySet().iterator();
            
            while(sIt.hasNext()){
                Map.Entry pair = (Map.Entry)sIt.next();
                suff = (String)pair.getKey();
                suffcount = (int)pair.getValue();
                
                Iterator tagIt = TagSet.entrySet().iterator();
                
                while(tagIt.hasNext()){
                    Map.Entry pairT = (Map.Entry)tagIt.next();
                    tag=(String)pairT.getKey();
                    
                    if(s2t.containsKey(suff+"_"+tag)){
                        double c = s2t.get(suff+"_"+tag);
                        c = c/suffcount;
                        s2t.put(suff+"_"+tag, c);
                    }
                }
            }
        }
        
        if(l==3){
            Iterator sIt = s1.entrySet().iterator();
            
            while(sIt.hasNext()){
                Map.Entry pair = (Map.Entry)sIt.next();
                suff = (String)pair.getKey();
                suffcount = (int)pair.getValue();
                
                Iterator tagIt = TagSet.entrySet().iterator();
                
                while(tagIt.hasNext()){
                    Map.Entry pairT = (Map.Entry)tagIt.next();
                    tag=(String)pairT.getKey();
                    
                    if(s3t.containsKey(suff+"_"+tag)){
                        double c = s3t.get(suff+"_"+tag);
                        c = c/suffcount;
                        s3t.put(suff+"_"+tag, c);
                    }
                }
            }
        }
        
        if(l==4){
            Iterator sIt = s1.entrySet().iterator();
            
            while(sIt.hasNext()){
                Map.Entry pair = (Map.Entry)sIt.next();
                suff = (String)pair.getKey();
                suffcount = (int)pair.getValue();
                
                Iterator tagIt = TagSet.entrySet().iterator();
                
                while(tagIt.hasNext()){
                    Map.Entry pairT = (Map.Entry)tagIt.next();
                    tag=(String)pairT.getKey();
                    
                    if(s4t.containsKey(suff+"_"+tag)){
                        double c = s4t.get(suff+"_"+tag);
                        c = c/suffcount;
                        s4t.put(suff+"_"+tag, c);
                    }
                }
            }
        }
    }
    
    void getTransisitionMatrix() throws IOException
    {
        double backoff;
        int singletons;
        File file = new File("/home/sanjeevmk/NetBeansProjects/PosTagger/src/nlp_1/output/Transition.csv");
            
        if (!file.exists()) {
            file.delete();
            file.createNewFile();
        }
            
    FileWriter fw = new FileWriter(file.getAbsoluteFile());
	BufferedWriter bw = new BufferedWriter(fw);
        
        Iterator row = TagSet.entrySet().iterator();
        bw.write("POS,");
        while(row.hasNext())
        {
                 Map.Entry rowPair = (Map.Entry)row.next();
                 String rowTag=(String) rowPair.getKey();
                 int rowValue=(int) rowPair.getValue();
                 bw.write("\""+rowPair.getKey()+"\",");
              //   System.out.println("\""+rowPair.getKey()+"\"");
        }   
                bw.append("\n");
 
        row = TagSet.entrySet().iterator();
        
        while(row.hasNext())
        {
            
            Map.Entry rowPair = (Map.Entry)row.next();
            String rowTag=(String) rowPair.getKey();
            int rowValue=(int) rowPair.getValue();
            Iterator coln= TagSet.entrySet().iterator();
            int total=0;
            
            bw.write(rowTag+",");
			
            while(coln.hasNext())
            {
                Map.Entry colnPair = (Map.Entry)coln.next();
                String colnTag=(String) colnPair.getKey();
                int colnValue=(int) colnPair.getValue();
                int count=TagTransitionCount.containsKey(rowTag+"_"+colnTag)?TagTransitionCount.get(rowTag+"_"+colnTag):0;
                double f=(float)0;
                //if(count!=0)
                {
                    backoff = transitionBackoff.get(colnTag);
                    singletons = transitionSingletons.get(rowTag);
                 
                    double N = foffT((double)count, rowTag);
                    double Nplus = foffT((double)(count+1),rowTag);
                        
                    if(goodTuring && Nplus!=0){
                        f = new Double( ( ( (double)count + 1 )/(double)rowValue ) * (Nplus/N) );
                    }else {
                        if(count!=0){
                            f=new Double( ((double)count + ( singletons*backoff*oneCount ))/ ( (double)rowValue + (singletons*oneCount ) ) );
                        }else{
                            f=new Double( ((double)count + 1 )/ ( (double)rowValue + vocab ) );
                        }
                    }
                    
                    if(f==0){
                        System.out.println("Zero\n");
                    }
                    
                    if(f!=0){
                    	//f=Math.log10(f);
                        TransistionProbabilty.put(rowTag+"_"+colnTag, f);
                    }
                //    System.out.println(rowTag+" _ "+colnTag +"  =  "+count);
                    total+=count;
                }
                
                bw.write(f+",");
            }
            bw.append("\n");
            //System.out.println(rowTag+" === "+rowValue+" "+total+"-------------------");
        }
        bw.close();
    }
    
    
    void getEmissionProbability() throws IOException
    {
        File file = new File("/home/sanjeevmk/NetBeansProjects/PosTagger/src/nlp_1/output/Emission.csv");
        double backoff;
        int singletons;
        
        if (!file.exists()) {
            file.delete();
            file.createNewFile();
        }
        
        System.out.println("Unique word count "+ wordSet.size());
            
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        
        
        Iterator word = wordSet.entrySet().iterator();
        
        
        Iterator tag2 = TagSet.entrySet().iterator();
            bw.write("Emmission");
            while(tag2.hasNext()){
                Map.Entry tagPair = (Map.Entry)tag2.next();
                bw.write(",\""+(String)tagPair.getKey()+"\"");
            }
            bw.write("\n");
        
        while(word.hasNext())
        {
            Map.Entry wordPair = (Map.Entry)word.next();
            String rowTag=(String) wordPair.getKey();
            
          //   String[] tagSplit = rowTag.split("_");
            // System.out.println(tagSplit[0]+" --- "+tagSplit[1]+" --> "+TagSet.get(tagSplit[1]));
             //wordProbability.put(rowTag, (float)rowValue/(float)TagSet.get(tagSplit[1]));
            Iterator tag = TagSet.entrySet().iterator();
            bw.write(rowTag);
            while(tag.hasNext()){
                Map.Entry tagPair = (Map.Entry)tag.next();
                String tagName = (String) tagPair.getKey();


                
                double count = wordCount.containsKey(rowTag+"_"+tagName)? (double)wordCount.get(rowTag+"_"+tagName):0.0;
                //double N = foffW(count,tagName);
                //double Nplus = foffW(count+1,tagName);
                /*
                if(goodTuring && Nplus!=0){
                    count = ( (count+1)/(double)TagSet.get(tagName) ) * (Nplus/N);
                }
                else{*/
                    count=( count  )/ ( (double)TagSet.get(tagName) );
                    //count=Math.log10(count);
                //}
                //bw.write(","+count);
                
                
                if(count!=0)
                {
                    wordProbability.put(rowTag+"_"+tagName, count);
                }
              /*
                if(count!=0) 
                    System.out.println("    "+rowTag+"_"+tagName+"     "+wordCount.get(rowTag+"_"+tagName));
           */
            }
            bw.write("\n");
        //    System.out.println(rowTag);
        }
        
            bw.close();
    }
    
    void getResult()
    {
        System.out.println("Tag count = "+TagSet.size());
        /*
        Iterator it = TagSet.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            System.out.println(pairs.getKey() + " = " + pairs.getValue());
            it.remove(); // avoids a ConcurrentModificationException
         }
       
        System.out.println("Tag Transisition matrix = "+TagTransistionCount.size());
        Iterator it2 = TagTransistionCount.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry pairs = (Map.Entry)it2.next();
            System.out.println(pairs.getKey() + " = " + pairs.getValue());
            it2.remove(); // avoids a ConcurrentModificationException
         }
        
        System.out.println("Tag Transisition matrix = "+TransistionProbabilty.size());
        Iterator it = TransistionProbabilty.entrySet().iterator();
        while (it.hasNext()) 
        {
            Map.Entry pairs = (Map.Entry)it.next();
            System.out.println(pairs.getKey() + " = " + pairs.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
       */ 
        System.out.println("Tag Transisition matrix = "+wordCount.size());
        Iterator it3 = wordCount.entrySet().iterator();
        while (it3.hasNext()) 
        {
            Map.Entry pairs = (Map.Entry)it3.next();
            
           // System.out.println(pairs.getKey() + " = " + pairs.getValue());
            it3.remove(); // avoids a ConcurrentModificationException
        }
       
    }
    
     
    void transitionBackOff(){
        Iterator tagIterator = TagSet.entrySet().iterator();
        
        while(tagIterator.hasNext()){
            Map.Entry pair = (Map.Entry) tagIterator.next();
            String tag = (String)pair.getKey();
            int count = (int)pair.getValue();
            
            transitionBackoff.put(tag, (count*1.0)/total);
        }
    }
    
    double foffT(double n, String tag){
        
        double fof = 0;
        
        if(!goodTuring){
            return 0;
        }
        
        Iterator tagIterator = TagSet.entrySet().iterator();
        
        while(tagIterator.hasNext()){
            Map.Entry pair = (Map.Entry) tagIterator.next();
            String tag2 = (String)pair.getKey();
            
            if(TagTransitionCount.get(tag+"_"+tag2) == n){
               fof++; 
            }
        }
        
        return fof;
    }
    
}