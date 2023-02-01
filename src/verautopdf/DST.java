/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package verautopdf;

import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.events.PdfPTableEventForwarder;



/**
 *
 * @author DELL PC
 */
public class DST extends PdfPTableEventForwarder{
    public void splitTable(PdfPTable table){
    super.splitTable(table);
    System.out.println("split table");
    }
    
}
