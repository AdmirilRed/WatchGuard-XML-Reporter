/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reporting;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import javax.swing.JOptionPane;
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
    
    public static long numFiles = 0;
    public static long fileNum = 0;
    
    private static ArrayList<File> files = new ArrayList<>();
    private static UserInterface panel;

    /**
     * @param args the command line arguments Command line arguments are NOT supported.
     * @throws java.io.IOException Throws an IOException if there is an issue writing to the destination file.
     * 
     * The main method that controls the report.
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        
        panel = new UserInterface();
        panel.setVisible(true);
        
        while(!panel.isReady()) {
            System.out.print(""); //Does nothing
        }
        
        panel.writeln("Searching for files...");
        
        File root = panel.getRootFile(); //Asks user for target folder
        
        String report = generateReport(root); //Goes through each directory and extracts relevant information
        
        panel.writeln("");
        panel.writeln(report);
        
        File dest = new File("WatchGuard CSV Report.csv");
        dest.createNewFile(); //Creates a file in the same location as the executable
        
        try (FileWriter f = new FileWriter(dest)) {
            f.write("Folder, Start Time, Stop Time, Officer, Vehicle, Category\n"); //Creates the header for the CSV
            f.write(report); //Saves the report to a txt file
            f.flush();
            
            panel.writeln("REPORT WRITTEN TO "+dest.getAbsolutePath());
        }
        
        if(JOptionPane.showConfirmDialog(null,
                "Would you like to open the CSV report?", "Open report?", JOptionPane.YES_NO_OPTION) == 0) {
            Desktop desktop = Desktop.getDesktop();
            desktop.open(dest);
            
        }
        
    }
    
    public static String generateReport(File f) {
        String result = "";
        
        traverse(f);
        panel.setMaxSize(files.size());
        panel.writeln(files.size()+" XML records found.");
        for(File ff:files) {
            panel.incrementFile();
            result+=extract(ff)+"\n";    
        }
        return result;       
    }
    
    //Traverses directories and sub-directories
    private static void traverse(File f) {
        numFiles+=f.listFiles().length;
        for(File ff:f.listFiles()) { //Loops through all files within the given directory
            fileNum++;
            panel.updateIndeterminate((int) fileNum);
            
            if(ff.isDirectory() && Files.isReadable(ff.toPath())) //Checks if the given file is a directory
                traverse(ff); //If the file is a directory, calls method again passing the new directory to search
            else if(ff.getName().toLowerCase().startsWith("tick") && ff.getName().toLowerCase().endsWith(".xml") && Files.isReadable(ff.toPath())) //Checks if a given file is XML
                files.add(ff);
        }
    }
    
    //Extracts relevant information from an XML file
    private static String extract(File file) {
        String result = "";
        try {
            result+=file.getParentFile().getAbsolutePath()+","; //Adds the parent folder path to the result String
            
            //Converts the File into readable XML for Java
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();
            
            //Collects the start and stop times for the recording
            NodeList nList = doc.getElementsByTagName("recording-event");
            for(int x=0;x<nList.getLength();x++) {
                Node node = nList.item(x);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

			Element eElement = (Element) node;

			result+= eElement.getAttribute("start-time")+",";
                        result+= eElement.getAttribute("stop-time")+",";
		}
            }
            
            //Collects the officer name and vehicle ID
            nList = doc.getElementsByTagName("info");
            for(int x=0;x<nList.getLength();x++) {
                Node node = nList.item(x);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

			Element eElement = (Element) node;

			if(eElement.getElementsByTagName("officer").getLength()>0)
                            result+= eElement.getElementsByTagName("officer").item(0).getTextContent()+",";
                        if(eElement.getElementsByTagName("vehicle").getLength()>0)
                            result+= eElement.getElementsByTagName("vehicle").item(0).getTextContent()+",";
		}
            }
            
            //Collects the encounter category
            nList = doc.getElementsByTagName("etl");
            for(int x=0;x<nList.getLength();x++) {
                Node node = nList.item(x);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

			Element eElement = (Element) node;

			if(eElement.getElementsByTagName("eti").getLength()>0)
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
