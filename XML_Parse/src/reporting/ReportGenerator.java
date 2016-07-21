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
 * @author Joseph Manahan
 * 
 *  This program is intended to generate reports from XML files created by the
 * WatchGuard video service. It searches for all XML files within a directory
 * and stores the Officer's name, the vehicle, the category of encounter,
 * and the video ID in a CSV file. If you encounter any issues or need to
 * request a feature, please contact me at JManahan@cityofrockport.com
 * 
 * FOR EXCLUSIVE USE BY THE ROCKPORT POLICE DEPARTMENT 
 * MUST OBTAIN EXPRESS WRITTEN PERMISSION FOR OTHER USE
 * 
 */
public class ReportGenerator {

    /**
     * @param args the command line arguments Command line arguments are NOT supported.
     * @throws java.io.IOException Throws an IOException if there is an issue writing to the destination file.
     * 
     * The main method that controls the report.
     */
    public static void main(String[] args) throws IOException {
        File root = getRootFile(); //Asks user for target folder
        String report = traverse(root); //Goes through each directory and extracts relevant information
        
        System.out.println();
        System.out.println(report);
        
        File dest = new File("WatchGuard XML Report.txt");
        dest.createNewFile(); //Creates a file in the same location as the executable
        
        try (FileWriter f = new FileWriter(dest)) { 
            f.write(report); //Saves the report to a txt file
            f.flush();
            
            System.out.println("REPORT WRITTEN TO "+dest.getAbsolutePath());
        }
        
    }
    
    //Determines the target directory
    private static File getRootFile() { 
        File f = null;
        try {
            Scanner kb = new Scanner(System.in); //Keyboard input scanner
            System.out.print("ROOT FOLDER PATH: ");
            String path = kb.nextLine().trim(); 
            f = new File(path); //Attempts to link user input to file
            
            if(!f.exists()) { //Checks to see if the specified file exists
                System.out.println("INVALID PATH\n");
                return getRootFile(); //Calls method again if file does not exist
            }
        }
        catch(Exception e) { //Catches any errors occurring 
            System.out.println("AN ERROR OCCURRED WHILE FINDING THE ROOT FILE");
            System.exit(99); //Exits with error level 99 if something goes wrong
        }
        
        System.out.println();
        return f; //Returns the target directory
    }
    
    //Traverses directories and sub-directories
    private static String traverse(File f) {
        String result = "";
        for(File ff:f.listFiles()) { //Loops through all files within the given directory
            if(ff.isDirectory()) //Checks if the given file is a directory
                traverse(ff); //If the file is a directory, calls method again passing the new directory to search
            else if(ff.getName().toLowerCase().endsWith(".xml")) //Checks if a given file is XML
                result+=extract(ff)+"\n"; //If the file is XML, extracts the relevant information and adds it to the result String
        }
        return result; //Returns the result String which is in CSV format
    }
    
    //Extracts relevant information from an XML file
    private static String extract(File file) {
        String result = "";
        try {
            result+=file.getName()+","; //Adds the file name to the result String
            
            //Converts the File into readable XML for Java
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();
            
            //Collects the Event ID
            NodeList nList = doc.getElementsByTagName("recording-event");
            for(int x=0;x<nList.getLength();x++) {
                Node node = nList.item(x);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

			Element eElement = (Element) node;

			result+= eElement.getAttribute("reid")+",";
		}
            }
            
            //Collects the officer name and vehicle ID
            nList = doc.getElementsByTagName("info");
            for(int x=0;x<nList.getLength();x++) {
                Node node = nList.item(x);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

			Element eElement = (Element) node;

			result+= eElement.getElementsByTagName("officer").item(0).getTextContent()+",";
                        result+= eElement.getElementsByTagName("vehicle").item(0).getTextContent()+",";
		}
            }
            
            //Collects the encounter category
            nList = doc.getElementsByTagName("etl");
            for(int x=0;x<nList.getLength();x++) {
                Node node = nList.item(x);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

			Element eElement = (Element) node;

			result+= eElement.getElementsByTagName("eti").item(0).getTextContent();
		}
            }
              
        }
        catch(ParserConfigurationException | SAXException | IOException e) //Catches any errors occurring during data extraction
        {
            System.out.println("AN ERROR OCCURRED READING FILE "+file.getName());
            System.out.println(e);
        }
        
        return result; //Returns a single line of CSV data for the report
    }
    
}
