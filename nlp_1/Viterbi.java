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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author msankith
 */
public class Viterbi {

    StringBuilder bigOut = new StringBuilder();
    
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
    HashMap<String,Integer> TagSet,WordSet;
    HashMap<String,Double> TransistionMatrix,EmissionMatrix;
    BufferedWriter output;
    
    Viterbi(String File,training t,BufferedWriter out)
    {
        fileName=File;
        TagSet = t.TagSet;
        WordSet=t.wordSet;
        TransistionMatrix = t.TransistionProbabilty;
        EmissionMatrix= t.wordProbability;
        output=out;
    }
    
    void parseFile() throws IOException
    {
        List<String> lines = new ArrayList<String>();
        
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            String line=null;
           
            
            while((line=br.readLine())!=null)
            {
                //lines.add(line);
                String Words[]= line.split(" ");
                Viterbi_Algorithm2(Words);
               
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Viterbi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Viterbi.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         
        for(String line : lines){
            String Words[] = line.split(" ");
            
            Viterbi_Algorithm2(Words);
            
        }
        output.append(bigOut);
        
    }
    
    
    
    void Viterbi_Algorithm(String[] Words)
    {
        
       HashMap<String,Double> prob = new HashMap<String,Double>();
       
       tree root= new tree(".",null);
       tree present=root;
       String history = ".";
       //printTrans();
       Iterator it=TagSet.entrySet().iterator();
       while(it.hasNext())
       {
            Map.Entry rowPair = (Map.Entry)it.next();
            String tagName = (String)rowPair.getKey();
            double tagProb ;
            //System.out.println("tag Name "+"._"+tagName);
            tagProb=TransistionMatrix.containsKey("._"+tagName)?(double)TransistionMatrix.get("._"+tagName):(double)0;
            prob.put(tagName, tagProb);
            //System.out.println(tagName+"    "+ tagProb);
            tree temp=new tree(tagName,present);
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
            double currentProb=TransistionMatrix.containsKey(prev+"_"+tagName)?(double)TransistionMatrix.get(prev+"_"+tagName):(double)0.000000001;
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
                double emissionProb = EmissionMatrix.containsKey(word+"_"+parentTag)?(double) EmissionMatrix.get(word+"_"+parentTag):(double) 0;
                
                if(knownWord==false)
                    emissionProb=1;
                
                  
                    
                Iterator tagIterator = TagSet.entrySet().iterator();
                //tagProb.clear();//to clear prev prob
                while(tagIterator.hasNext())
                {
                    Map.Entry tagPair = (Map.Entry) tagIterator.next();
                    String tagName = (String) tagPair.getKey();
                    double presentProb = tagProb.containsKey(tagName)?tagProb.get(tagName):(double)0;
                    double transmissionProb = TransistionMatrix.containsKey(parentTag+"_"+tagName)?(double)TransistionMatrix.get(parentTag+"_"+tagName):0.00000001;
                    
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
        String maxTag = "";
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
     //   System.out.println("last tag " + maxTag);
      // System.out.println("max prob of HMM "+ maxProb);
        
        
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
            for(int i=0;i<Words.length;i++)
            {
                bigOut.append(Words[i]+"_"+tags.pop()+" ");
               // String out=Words[i]+"_"+tags.pop()+" ";
        //    System.out.print(out);
                //output.write(out);
             }
        }
       // output.write("\n");
            
       
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
    
}