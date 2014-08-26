/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nlp_1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author msankith
 */
public class NLP_1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        String a = "hello";
        System.out.println(a.length());
        System.out.println(a.substring(0,1));
        BufferedWriter out=null;
        try {
        	
        	
            // TODO code application logic here
            training data=new training("/home/sanjeevmk/NetBeansProjects/PosTagger/src/nlp_1/input/train2.txt");
            data.parseUnknownFile(new File("/home/sanjeevmk/NetBeansProjects/PosTagger/src/nlp_1/input/unknownprobs.csv"));
            data.parseFile();
            
            
            FileOutputStream fout = new FileOutputStream("/home/sanjeevmk/NetBeansProjects/PosTagger/src/nlp_1/input/training.ser");
    		ObjectOutputStream oos = new ObjectOutputStream(fout);   
    		oos.writeObject(data);
    		
    		oos.close();
    		
    		System.out.println("Done");
            
    		//out = new BufferedWriter(new FileWriter("/home/sanjeevmk/NetBeansProjects/PosTagger/src/nlp_1/input/output5_goodT2_1.txt"));
            //Viterbi algo= new Viterbi("/home/sanjeevmk/NetBeansProjects/PosTagger/src/nlp_1/input/test5.txt", data,out);
            //algo.parseFile();
            
            //  data.getResult();
        } catch (IOException ex) {
            Logger.getLogger(NLP_1.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //out.close();
        }
    }
    
}