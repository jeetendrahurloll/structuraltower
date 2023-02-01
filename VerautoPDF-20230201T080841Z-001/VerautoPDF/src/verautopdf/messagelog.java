/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package verautopdf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author DELL PC
 */




public class messagelog { 
  public static LinkedList mymessages = new LinkedList(); 
    public void addmsg(String S){
        
        mymessages.add(S);
    }
    public static void dumpmsg(){
        BufferedWriter out =null;
         try {
             out = new BufferedWriter(new FileWriter("C:\\NoteCalc\\LesErreurs.txt"));
             
              for(int i = 0; i < (mymessages.size()) ; i++) {
                 System.out.println("----dump----"+mymessages.get(i).toString());
                  out.write(mymessages.get(i).toString()+ "\n");
                }
             out.close();
         } catch (IOException ex) {
             Logger.getLogger(messagelog.class.getName()).log(Level.SEVERE, null, ex);
         }
        
        
        
    
     
    
    }
    
    
}
