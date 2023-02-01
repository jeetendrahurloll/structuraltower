
package verautopdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
public class TableStructure {
public LinkedList TableExtract;//the pointer to point to the linked list where the table row elements are stored.
public int start;//where the start of the table is inside the linked list where the elemenst are stored
public int end;//where the end of the table is inside the linked list where the elemensta re stored
String[][] TableData;//an array of strings in which the Tableextract is splist and cut up into individual segments
public int pdfTableW;//the displayed width of the table,as width_array has more lements,some of which are rejected for display but kept for other information
int width_array;//the original number of elements  found in a row element. SHould be the same everywhere, with "}" as delimiter
int height_array;//the original number of row elements found between the start /end of the table,calculated from finding the width of the row elements as delimitter,and also =="}}" as delimiter
Boolean[] visibility;//a small linear aray that denotes if a column should be visible or not
LinkedList visibilitylist;//a small list of boolean [] visibility compiled as a list of indices where it is correct to display the column
int[] maxcolwidth;//a small linear aray to denote the max width of each column, in terms of characters.(maybe will update to width in terms of points on the pdf doc)
float []maxcolwidthpts;//a small array used to compute width of each column in points.
float []visible_maxcolwidthpts;//same as maxcolwidthpts, but with only elements that can be viewed , reduced by using visibilitylinkedlist.
PdfPCell[][] pdfcellarray;//an array of TableData converted to cell elements.
float totaltablewidth;//the total width of the pdftable,computed by cum sum maxcolwidthpts;
/*constructor*/
TableStructure(int start, int end,LinkedList L,Document D){
    this.start=start;
    this.end=end;
    this.TableExtract=L;
    this.makearray();
    this.makepdfcellarray();
    this.makecolwidth();
    this.maketable(D);
    
   
}

/*splits elements from table extract into string elements inside tabledata,and computes visibility and displayed width of table,pdftableW*/
public void makearray(){
    //System.out.println("makearray"+start+">>"+end);
height_array=end-start+1;
String temp[]=TableExtract.get(start).toString().split("}");
width_array=temp.length;
pdfTableW=width_array;
visibility=new Boolean[temp.length];
visibilitylist=new LinkedList();
//Block A.clean first row to remove surplus columns which have no title.
//very often,some tables exhibit blank columns

    for (int i = 0; i < temp.length; i++) {
        if (temp[i].replaceAll("\\s", "").length() == 0) {
            visibility[i] = false;
            pdfTableW--;

        } else {
            visibility[i] = true;
            visibilitylist.add(i);//this adds an index of where the columns can be made visible
            
        }


    }
//pdfTableW is therefore the true width of the table,as some columns are left blank,and also some columns will be used as markers ,ex asterisk,to denote an anomaly which neds to be highlighted as a colour.
//Because the asterisk that appears in the table is in a column that is eliminated by visibility and visibilitylist (i think,one of these or both),i will therefore extract occurences of * and concatenate it with the text of the rpevious cell on the row .
//There is another segment where the * or other remarks are converted into formatting information. Thus,this asterisk which gets appended to the data in the previous cell is then visible with the data together in the same string.
//Ex 1234}*} is converted into 1234*,which is then converted into a purple background cell in the pdf.
//End of Block A





TableData = new String[height_array][width_array];
int z;int y;
    for (z = start; z <= end; z++) {
        String temp2[] = TableExtract.get(z).toString().split("}");
        for (y = 0; y < (temp2.length); y++) {
            TableData[z - start][y] = temp2[y].trim();//z-start as z starts at index of first dataline which might not be zero.
            if(TableData[z - start][y].contains("*")){
                if(y==0){
               
                messagelog.mymessages.add("high strength bolts are being flagged on column index  <0 in a rir file");
                }
                else
                {
                
                TableData[z - start][y-1]=TableData[z - start][y-1]+"*hbt";
                //now the tag "hsbolttag" will be used later on in cell formatting to
                //change the background to purplre and the hsbolttag removed
                }
            
            }
        }
    }

////Debugging visibilitylist
//System.out.println("debugging visibility list from makearray");
//    for (int q=0;q<visibilitylist.size();q++)
//    {
//        System.out.print(visibilitylist.get(q)+"!");
//    }
//    System.out.println(" ");
//
//    for (int i = 0; i < width_array; i++) {
//        if (visibility[i]) {
//            System.out.print("1");
//        } else {
//            System.out.print("0");
//        }
//    }
//System.out.println("end of debugging visibility from makearray");

//System.out.print("PdfWidth:"+pdfTableW);System.out.println("Array w:"+width_array);
//
//
//    for (int x = 0; x < (height_array); x++) {
//
//        for (int w = 0; w < (width_array); w++) {
//            if (visibility[w]) {
//                System.out.print(TableData[x][w]);
//                System.out.print("!");
//            }
//        }
//        System.out.println("");
//    }
//System.out.println("**********end of debugging makearray*************");

}

/*add pdfcells to pdftable and insert it into document*/
public void maketable(Document D){
    //System.out.println("maketable"+start+">>"+end);
/*obtainscell data from pdfcellarray;
had to be separated from cell array  
because filters might have to be applied on each cell
to format it properly for display

Update...this section will make clever use of different indixes 
to access tabledata array and pdfcell array as pdfcellarray has less elements 
which have to be matched*/


try {
PdfPTable mytable = new PdfPTable(visibilitylist.size());
mytable.setTotalWidth(visible_maxcolwidthpts);

mytable.setLockedWidth(true);
mytable.setSpacingBefore(10);
mytable.setSpacingAfter(10);
    for (int x = 0; x < (height_array); x++) {
        for (int w = 0; w < (visibilitylist.size()); w++) {
               mytable.addCell(pdfcellarray[x][w]);
       }
    }
D.add(mytable);
//System.out.println("*******pdpf******");
//System.out.println(D.getPageNumber());

} catch (DocumentException ex) {
Logger.getLogger(rir_to_table.class.getName()).log(Level.SEVERE, null, ex);
}


}

/* makes an array of pdfcells based on visibility and data from tabledata */
public  void makepdfcellarray(){
    //System.out.println("makepdfcellarray"+start+">>"+end);
//the pdfcell array will have all the cells as the tablecata string array.
//This is becasue the logic of filling the pdfcellarray with for loop gets cumbersome
// as the index is no longer the same for the width if one array gets offset
//tabldata has different number of columns than pdfcellarray,so the indexng mechanism depends on 
//visibilitylist

    pdfcellarray=new PdfPCell[height_array][visibilitylist.size()]; 
    Phrase ph;
    String TextUnderFormatting;//a string to contain the text,to analyse any formatting text,remove the formatting text from the actualdata,then apply the formatting to the cell,while inserting the data in the cell.
    String [][] testarray=new String[height_array][visibilitylist.size()];
     for (int t = 0; t < height_array; t++) { 
         for (int s = 0; s < visibilitylist.size(); s++) {
             testarray[t][s] = TableData[t][(Integer) visibilitylist.get(s)];
             TextUnderFormatting = TableData[t][(Integer) visibilitylist.get(s)];
             //ph = new Phrase(TableData[t][(Integer)visibilitylist.get(s)], FontFactory.getFont(FontFactory.HELVETICA, 8));
             ph = new Phrase(TableData[t][(Integer) visibilitylist.get(s)], FontFactory.getFont(FontFactory.HELVETICA, 8));
             pdfcellarray[t][s] = new PdfPCell(ph);
               
                
               
                
                
                if(t<1){
                pdfcellarray[t][s].setHorizontalAlignment(0);
                pdfcellarray[t][s].setBackgroundColor(new BaseColor(200, 200, 200));
               
                }
                else
                {
                pdfcellarray[t][s].setHorizontalAlignment(2);
                pdfcellarray[t][s].setBackgroundColor(new BaseColor(255, 255, 255));
               
                }
              
                
             if (TextUnderFormatting.indexOf("tg")>-1){//searching for tg
                //if the remark tg is present,format background to something else
              
                pdfcellarray[t][s].setBackgroundColor(new BaseColor(255, 200, 200));
                pdfcellarray[t][s].setPhrase(new Phrase(TextUnderFormatting.replace("tg", ""), FontFactory.getFont(FontFactory.HELVETICA, 8)));

                
                }
             if (TextUnderFormatting.indexOf("hbt")>-1){//searching for tg
                //if the remark tg is present,format background to something else
              
                pdfcellarray[t][s].setBackgroundColor(new BaseColor(220, 200, 255));
                pdfcellarray[t][s].setPhrase(new Phrase(TextUnderFormatting.replace("hbt", ""), FontFactory.getFont(FontFactory.HELVETICA, 8)));

                
                }
                
               
        }
        
    }
}

/*makes a linear array showing the maxwidth of each column,to make a proper table with properly sized columns*/
public void makecolwidth(){
    //System.out.println("makecolwidth"+start+">>"+end);
    totaltablewidth=0;
    maxcolwidth=new int[width_array];
    maxcolwidthpts=new float[width_array];
   
    
    String[] maxwidthelem=new String[width_array];//just to display the elements in that column that have max length
    
    /////////////////////////minor cleanup to remove nulls and replace them with "" as some cells contain null and cause null pointer exception
    for (int col=0;col<width_array;col++)
    {
        for(int row=0;row<height_array;row++)
            {
                if(TableData[row][col]==null){
                    TableData[row][col]="";
                }
            }
    }
    ////////////////////////
    
    for (int col=0;col<width_array;col++)
    {
        maxcolwidth[col]=0;
        for(int row=0;row<height_array;row++)
        {
            //System.out.println("col"+col+"row"+row);
         
            
            
         if (TableData[row][col].length()>maxcolwidth[col])
          {
              maxcolwidth[col]=TableData[row][col].length();
              maxwidthelem[col]=TableData[row][col];
              Chunk chunk = new Chunk(TableData[row][col], FontFactory.getFont(FontFactory.HELVETICA,11));
              maxcolwidthpts[col]= chunk.getWidthPoint();
              totaltablewidth=totaltablewidth+maxcolwidthpts[col];
              
          }       
        }
    }
   // System.out.println("totaltablewidth"+totaltablewidth);
  
 
   

//populate visible_maxcolwidthspts,by reducing maxcolwidthpts based on visibilitylist
    visible_maxcolwidthpts=new float[visibilitylist.size()];
    for (int q=0;q<visibilitylist.size();q++){
    visible_maxcolwidthpts[q]=(float) (maxcolwidthpts[(Integer)visibilitylist.get(q)] * 1.2f);//multiply by 1.3 and convert to float,as the bitch is complaining about being given double.
        //System.out.print(visible_maxcolwidthpts[q]);System.out.print(" ");
    }
    //System.out.println(" ");
    
    
    
    //problems with width of a table .so instead of making weird kinds of proper scaling, i just patched the width of the columns of the third table which conctains the legreinf words...hahahahahaha
    if(TableData[0][4].contains("LEG.REINF")){//that effing table was too wide. Did not really investigate why it was so large despite no having a totalwidth that bad. ANyway i made a scalefactor to control its effing width.
    visible_maxcolwidthpts[4]=visible_maxcolwidthpts[4]*0.7f;
    visible_maxcolwidthpts[3]=visible_maxcolwidthpts[3]*0.7f;
    visible_maxcolwidthpts[11]=visible_maxcolwidthpts[11]*0.7f;
    }

}



}//end of class tablestructure
