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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author msankith
 */
public class training {
    
    File Corpus;
    public HashMap<String,Integer> TagSet,TagTransitionCount,wordCount, transitionSingletons, emissionSingletons;
    public HashMap<String,Double> TransistionProbabilty,wordProbability, transitionBackoff, emissionBackoff;
    HashMap<String,Integer> wordSet;
    int total;
    int vocab;
    int doGoodTuring = 1;
    
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
        emissionSingletons = new HashMap<String,Integer>();   
    }
   
    void parseFile()
    {
        int singleCount;
        
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
                
                if(!emissionSingletons.containsKey(history)){
                        emissionSingletons.put(history, 0);
                }
                
                for(String str : words_tag)
                {
                    String[] tagSplit = str.split("_");
                    //tagSplit[1]=tagSplit[1].replaceAll("(-HL)|(-TL)|(-NC)|(FW-)", "");
                    tagSplit[1]=tagSplit[1].replaceAll("(-[A-Z]+)|(\\+[A-Z]+)|\\*|\\$", "");
                    
                  //  System.out.print(tagSplit[0]+" ");
                    //System.out.println(str+"    "+tagSplit[0]+" -> "+tagSplit[1]);
                //    bw.write("\""+str+"\",\""+tagSplit[0]+"\",\""+tagSplit[1]+"\"\n");
                    int count=TagSet.containsKey(tagSplit[1])?TagSet.get(tagSplit[1]):0;
                    TagSet.put(tagSplit[1],count+1);
                    
                    if(!transitionSingletons.containsKey(tagSplit[1])){
                        transitionSingletons.put(tagSplit[1], 0);
                    }
                    
                    if(!emissionSingletons.containsKey(tagSplit[1])){
                        emissionSingletons.put(tagSplit[1], 0);
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
                    
                    TagTransitionCount.put(history+"_"+tagSplit[1],count+1);
                    history=tagSplit[1];
                    
                    count=wordCount.containsKey(tagSplit[0]+"_"+tagSplit[1])?wordCount.get(tagSplit[0]+"_"+tagSplit[1]):0;
                    
                    if(count == 0){
                        singleCount = emissionSingletons.get(tagSplit[1]);
                        emissionSingletons.put(tagSplit[1], singleCount+1);
                    }
                    
                    if(count == 1){
                        singleCount = emissionSingletons.get(tagSplit[1]);
                        emissionSingletons.put(tagSplit[1], singleCount-1);
                    }
                    
                    wordCount.put(tagSplit[0]+"_"+tagSplit[1],count+1);
                    
                    count=wordSet.containsKey(tagSplit[0])?wordSet.get(tagSplit[0]):0;
                    wordSet.put(tagSplit[0],count+1);
                }
            }
            vocab = wordSet.size();
            emissionBackOff();
            transitionBackOff();
            getTransisitionMatrix();
            getEmissionProbability();
        } 
        catch (FileNotFoundException ex) {
            Logger.getLogger(training.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (Exception ex) {
            Logger.getLogger(training.class.getName()).log(Level.SEVERE, null, ex);
        }
         
    }
            
    
    void getTransisitionMatrix() throws IOException
    {
        double backoff;
        int singletons;
        File file = new File("Transistion.csv");
            
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
                if(count!=0)
                {
                    backoff = transitionBackoff.get(colnTag);
                    singletons = transitionSingletons.get(rowTag);
                    
                    f=new Double( ((double)count + ( singletons*backoff*doGoodTuring ))/ ( (double)rowValue + (singletons*doGoodTuring ) ) );
                    TransistionProbabilty.put(rowTag+"_"+colnTag,f);
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
        File file = new File("Emission.csv");
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
                backoff = emissionBackoff.get(rowTag); // actually fetching by word. misleading variable name
                singletons = emissionSingletons.get(tagName);
                
                double count=wordCount.containsKey(rowTag+"_"+tagName)?( (double)wordCount.get(rowTag+"_"+tagName) + (singletons*backoff*doGoodTuring) )/ ( (double)TagSet.get(tagName) + (singletons*doGoodTuring) ):(double)0;
                bw.write(","+count);
                
                
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
    
    void emissionBackOff(){
        Iterator wordIterator = wordSet.entrySet().iterator();
        
        while(wordIterator.hasNext()){
            Map.Entry pair = (Map.Entry) wordIterator.next();
            String word = (String)pair.getKey();
            int count = (int)pair.getValue();
            
            emissionBackoff.put(word, ((count*1.0)+1)/(total+vocab));
        }
    }
    
    
}