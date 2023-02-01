package verautopdf;


import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;



public class rir_to_table{
    LinkedList rirlines=new LinkedList();
    Document D;
    String rirpath;
    
    rir_to_table(Document ID,String pathofRIR){
    this.D=ID;
    this.readfile(pathofRIR);
    this.rirpath=pathofRIR;
    //listrir(rirlines);
    if(this.sanitizetable(rirlines)){
     this.tableparse();
    }
    else
    {
    messagelog.dumpmsg();
    }
   
    }
    
    public void readfile(String pathofRIR){
        BufferedReader br;String Str;
        rirlines.clear();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(pathofRIR),"ISO-8859-1"));
            //replaced cp1256 with ISO-8859-1

                  while ((Str=br.readLine())!= null)
                    {
                        ////clean blank lines
                            if(Str.replaceAll("\\s","").length()>0)
                            { 
                             Str=Str.trim();
                            rirlines.add(Str);
                            }
                     }
                  //pre-padding
                  rirlines.add(0, "}}}");  rirlines.add(0, "}}}");  rirlines.add(0, "}}}");
                 //post-padding
                 rirlines.add("}}}");rirlines.add("}}}");rirlines.add("}}}");
                 //System.out.println("rirlines:"+rirlines.size());
                 //messagelog.mymessages.add("rirlines:"+rirlines.size());
                 br.close();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(rir_to_table.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(rir_to_table.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(rir_to_table.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//end of readfile,which just reads a file    
   
    /** sanitize will check if the correct number of delimiters are present,by counting starting table delimiters and then end table delimiters,as they should be equal.It is noted that a linked list of tablecoods is generated it seems in tablecoods*/
    /**notice that no errors manage to go beyondthe error log,which means table parse will execute even if celnumber insanity is occuring,that is
    number of start rows and number of rows in the table is not consistent in a table listing.
    This must be corrected to prevent running the program.
    */
    public  boolean sanitizetable (LinkedList rir){
        int start=0;
        int end=0;
        int count_initial=0;
        int count_cells=0;
        int startpos=0;
        int endpos=0;
        boolean sanity=true;boolean cellnumsanity=true;
        boolean position=false;//if outside the delimiters of a table position =false,if inside a table delimiter position=true
        LinkedList tablesCoods=new LinkedList();
      //System.out.println("_________________________________sanitize________________________________________________________");
        
        for (int i = 1; i < (rir.size()-1); i++)
        {
        if(("}}}".equals(rir.get(i).toString())) &&(rir.get(i+1).toString().length()>6))
            {
                /**
                 This section spots for }}} }}} on 2 lines.This indicates you are on the beginning of a table as the beginning contains }}} forllowed by a long element
                 **/
                
            //System.out.println("sanitize table start");
            //messagelog.mymessages.add("sanitize table starts");
                
                
            count_initial = rir.get(i+1).toString().length() - rir.get(i+1).toString().replace("}", "").length();//find number of } occurences in first line of table,the title line
            start=start+1;
            position=true;//inside a table data
            startpos=i+1;//position of table start inside the linked list
             
            }
        else if(("}}}".equals(rir.get(i).toString())) &&(rir.get(i-1).toString().length()>6))
            {
             /*
                This section scans the rirlines array until it finds the end of the table by looking for  a long element followed by }}}
                */
                
            //System.out.println("sanitize table end");
            //messagelog.mymessages.add("sanitize table end");
            end=end+1;
            position=false;//outside a table data
            endpos=i-1;//position of the end of the table in the list of rir elements.
            tablesCoods.add(Integer.toString(startpos)+","+Integer.toString(endpos)+","+Integer.toString(count_initial));//Apparently another arralysist called tabllecoods contains all the table delimiters.
             
            
            }
        else if(("}}}".equals(rir.get(i-1).toString())) &&("}}}".equals(rir.get(i).toString()))&&("}}}".equals(rir.get(i+1).toString())))
            {
                  
            //System.out.println(rirlines.get(i).toString()+"<--surplus-->");//nothing to do,just a surplus delimiter in the middle of 2 delimiters
                
                //surplus detetctor of three }}} }}} }}} in three consecutive elements
            }
        else
            {
            //normal data line full of columns,which is split then divided into unit cells
            count_cells=(rir.get(i).toString().length() - rir.get(i).toString().replace("}", "").length());//count number of occurences of } to determine number of cells in a line that is not the title line,that is not the first line
            
            //cellnumsanity check has not been implemented to be complementary to sanity check 
             if (position==true)
             {
                
                 if (count_cells != count_initial) {
                     
                     //Execute this if the first line and any subesequent line in the table do not have similar umber of cells.
                     System.out.println("count_initial:" + count_initial + "count_cells" + count_cells);
                     //messagelog.mymessages.add("count initial"+count_initial+"count_cells"+count_cells);
                     cellnumsanity = false;
                     System.out.println("celnuminsanity");
                     messagelog.mymessages.add("RIR files are corrupted");
                     System.out.println(rir.get(i).toString() + ">>error at line:"+i+"in file "+this.rirpath );
                     String errstring=rir.get(i).toString() + ">>error at line:"+i+"in file "+this.rirpath ;
                     String S;Paragraph p;
                     S = "Errors!! "+errstring;
                     
                     p = new Paragraph(S, new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, new BaseColor(255, 0, 0)));
                     p.setSpacingAfter(5);
                     p.setSpacingBefore(5);
                     p.setAlignment(Element.ALIGN_LEFT);
                     //p.getFont().setStyle(Font.UNDERLINE);
                     try {
                         D.add(p);
                     } catch (DocumentException ex) {
                         Logger.getLogger(rir_to_table.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
             }
            
            
            
            
            String temp[]=rir.get(i).toString().split("}");
           
                    
            }
        }//end of for loop for rir elements list
        
        if (start==end){
            //file has a sound number of start and end delimiters.
            if (cellnumsanity==true)
            {sanity= true;}
            else
            {sanity=false;}
            
        }
        else{
            //file does not have a sound number of delimiters.
            System.out.println("table delimiters do not match");
            sanity= false;        
        }
    //System.out.println("sanity:"+ Boolean.toString(sanity));
    //messagelog.mymessages.add("sanity"+Boolean.toString(sanity));
    //System.out.println("cellnumsanity:"+ Boolean.toString(cellnumsanity));
    //messagelog.mymessages.add("cellnumsanity:"+Boolean.toString(cellnumsanity));
    
    //System.out.println("________________________________end of sanitize__________________________________________________"); 
    //messagelog.mymessages.add("______________end of sanitize_____________________");
    
    return sanity;  
    
    }  
    /*cuts up the rirlines list into segments of start and end ,and initialises a tablestructure for each segment*/
    public void tableparse(){
        /*Create two linked lists. Inside them store only begining of table,and end of table coordinates of lines stored inside the rir files.
        These coordinates are then used to create a linkedlist of tableStructures that are egenrated by using these two coordinates.
        TableStructures it seems contain many kinds of required functions to generate tables and add them to the supplied document D.
        */
LinkedList TablecoodsStart=new LinkedList();
LinkedList TablecoodsEnd=new LinkedList();
//System.out.println("*************************************table parse for loop*********************************************");
//messagelog.mymessages.add("*************table parse for loop************************");
//>6 means you are on a data line not a delimiter line

        for (int i = 1; i < (rirlines.size() - 1); i++) {
            if (("}}}".equals(rirlines.get(i).toString())) && (rirlines.get(i + 1).toString().length() > 6))//"table start
            {
                TablecoodsStart.add(i + 1);
            } else if (("}}}".equals(rirlines.get(i).toString())) && (rirlines.get(i - 1).toString().length() > 6))//end of table is met,and therefore added to document
            {
                TablecoodsEnd.add(i - 1);
            } else if (("}}}".equals(rirlines.get(i - 1).toString())) && ("}}}".equals(rirlines.get(i).toString())) && ("}}}".equals(rirlines.get(i + 1).toString()))) {//nothing to do,just a surplus delimiter in the middle of 2 delimiters
            }
            else {//normal data line full of columns,which is split then divided into unit cells
            }
        }//end of for loop from processing lines
    //System.out.println("*********************************end of table parse for loop**********************");
   // messagelog.mymessages.add("**************end of table parse for loop******************");

LinkedList T=new LinkedList();
TableStructure tempT;
for (int i = 0; i < (TablecoodsStart.size()); i++)
{
   tempT=new TableStructure((Integer)TablecoodsStart.get(i),(Integer)TablecoodsEnd.get(i),rirlines,D);
   T.add(tempT);
   tempT=null;
     
}
 
//System.out.println("complete");
//messagelog.mymessages.add("complete");
    
    
}//end of table parse method
    public void listrir(LinkedList rir){
   //Just a debug function to list the rir files on sys out
        for (int i=0;i<rir.size();i++){
            System.out.println(rir.get(i));
        }
        
        
    
    }

}//end of class
