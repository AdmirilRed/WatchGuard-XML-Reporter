/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reporting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author admir
 */
public class ReportGenerator {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        File root = getRootFile();
        String report = traverse(root);
        
        System.out.println();
        System.out.println(report);
        
        File dest = new File("WatchGuard XML Report.txt");
        dest.createNewFile();
        
        try (FileWriter f = new FileWriter(dest)) {
            f.write(report);
            f.flush();
            
            System.out.println("REPORT WRITTEN TO "+dest.getAbsolutePath());
        }
        
    }
    
    /**
     *
     * @return
     */
    public static File getRootFile() {
        File f = null;
        try {
            Scanner kb = new Scanner(System.in);
            System.out.print("ROOT FOLDER PATH: ");
            String path = kb.nextLine().trim();
            f = new File(path);
            
            if(!f.exists()) {
                System.out.println("INVALID PATH\n");
                return getRootFile();
            }
        }
        catch(Exception e) {
            System.out.println("AN ERROR OCCURRED WHILE FINDING THE ROOT FILE");
            System.exit(99);
        }
        
        System.out.println();
        return f;
    }
    
    public static String traverse(File f) {
        String result = "";
        for(File ff:f.listFiles()) {
            if(ff.isDirectory())
                traverse(ff);
            else if(ff.getName().toLowerCase().endsWith(".xml"))
                result+=extract(ff)+"\n";
        }
        return result;
    }
    
    public static String extract(File file) {
        String result = "";
        try {
            System.out.println(file.getName());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

            doc.getDocumentElement().normalize();
                        
            NodeList nList = doc.getElementsByTagName("recording-event");
            for(int x=0;x<nList.getLength();x++) {
                Node node = nList.item(x);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

			Element eElement = (Element) node;

			result+= eElement.getAttribute("reid")+",";
		}
            }
            
            nList = doc.getElementsByTagName("info");
            for(int x=0;x<nList.getLength();x++) {
                Node node = nList.item(x);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

			Element eElement = (Element) node;

			result+= eElement.getElementsByTagName("officer").item(0).getTextContent()+",";
                        result+= eElement.getElementsByTagName("vehicle").item(0).getTextContent()+",";
		}
            }
            
            nList = doc.getElementsByTagName("etl");
            for(int x=0;x<nList.getLength();x++) {
                Node node = nList.item(x);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

			Element eElement = (Element) node;

			result+= eElement.getElementsByTagName("eti").item(0).getTextContent();
		}
            }
              
        }
        catch(ParserConfigurationException | SAXException | IOException e)
        {
            System.out.println("AN ERROR OCCURRED READING FILE "+file.getName());
            System.out.println(e);
        }
        
        return result; 
    }
    
}
