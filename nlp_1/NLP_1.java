/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nlp_1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
        BufferedWriter out=null;
        try {
            // TODO code application logic here
            training data=new training("/home/sanjeevmk/NetBeansProjects/PosTagger/src/nlp_1/input/train2.txt");
            data.parseFile();
            out = new BufferedWriter(new FileWriter("/home/sanjeevmk/NetBeansProjects/PosTagger/src/nlp_1/input/output2.txt"));
            Viterbi algo= new Viterbi("/home/sanjeevmk/NetBeansProjects/PosTagger/src/nlp_1/input/test2.txt", data,out);
            algo.parseFile();
            
            //  data.getResult();
        } catch (IOException ex) {
            Logger.getLogger(NLP_1.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
    }
    
}