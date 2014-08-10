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
    public HashMap<String,Integer> TagSet,TagTransistionCount,wordCount;
    public HashMap<String,Double> TransistionProbabilty,wordProbability;
    HashMap<String,Integer> wordSet;
    
    
    training(String Corpus){
        
        this.Corpus=new File(Corpus);
        TagSet=new HashMap<String,Integer>();
        TagTransistionCount=new HashMap<String,Integer>();
        TransistionProbabilty = new HashMap<String,Double>();
        wordProbability= new HashMap<String,Double>();
        wordCount = new HashMap<String,Integer>();
        wordSet=new HashMap<String,Integer>();
    }
    
    void parseFile()
    {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Corpus)));
            
            String line=null;
           
            while((line=br.readLine())!=null)
            {
                String[] words_tag=line.split("\\s+");
                System.out.println("Total data count in corpus  "+ words_tag.length);
                String history=new String(".");
                for(String str : words_tag)
                {
                    String[] tagSplit = str.split("_");
                    tagSplit[1]=tagSplit[1].replaceAll("(-HL)|(-TL)|(-NC)|(FW-)", "");
  //                  tagSplit[1]=tagSplit[1].replaceAll("(-[A-Z]+)|(\\+[A-Z]+)|\\*|\\$", "");
                    
                  //  System.out.print(tagSplit[0]+" ");
                    //System.out.println(str+"    "+tagSplit[0]+" -> "+tagSplit[1]);
                //    bw.write("\""+str+"\",\""+tagSplit[0]+"\",\""+tagSplit[1]+"\"\n");
                    int count=TagSet.containsKey(tagSplit[1])?TagSet.get(tagSplit[1]):0;
                    TagSet.put(tagSplit[1],count+1);
                    
                    count=TagTransistionCount.containsKey(history+"_"+tagSplit[1])?TagTransistionCount.get(history+"_"+tagSplit[1]):0;
                    TagTransistionCount.put(history+"_"+tagSplit[1],count+1);
                    history=tagSplit[1];
                    
                    count=wordCount.containsKey(tagSplit[0]+"_"+tagSplit[1])?wordCount.get(tagSplit[0]+"_"+tagSplit[1]):0;
                    wordCount.put(tagSplit[0]+"_"+tagSplit[1],count+1);
                    
                    wordSet.put(tagSplit[0],0);
                }
            }
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
                int count=TagTransistionCount.containsKey(rowTag+"_"+colnTag)?TagTransistionCount.get(rowTag+"_"+colnTag):0;
                double f=(float)0;
                if(count!=0)
                {
                    f=new Double((double)count/(double)rowValue);
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
                double count=wordCount.containsKey(rowTag+"_"+tagName)?(double)wordCount.get(rowTag+"_"+tagName)/(double)TagSet.get(tagName):(double)0;
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
}