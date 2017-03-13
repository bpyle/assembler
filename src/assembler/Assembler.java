/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assembler;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.*;

/**
 *
 * @author bpyle
 */
public class Assembler {

    public Assembler() {

        // Create the 
        initializeMap();
    }
    
    //Line class
    private static class Line{

        public String print() {
            return (ref + ", " + instruction + " " + Integer.toHexString(decNum));
        }

        public String getRef() {
            return ref;
        }

        public void setRef(String ref) {
            this.ref = ref;
        }

        public String getInstruction() {
            return instruction;
        }

        public void setInstruction(String instruction) {
            this.instruction = instruction;
        }

        public int getDecNum() {
            return decNum;
        }

        public void setDecNum(int decNum) {
            this.decNum = decNum;
        }

        public String getHexNum() {
            return hexNum;
        }

        public void setHexNum(String hexNum) {
            this.hexNum = hexNum;
        }

        public Line(String ref, String instruction, String hexNum) {
            this.ref = ref;
            this.instruction = instruction;
            this.hexNum = hexNum;
            
            this.decNum = new BigInteger(hexNum, 16).intValue();
        }
        
        
        
        String ref;
        String instruction;
        int decNum;
        String hexNum;
        
    }
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        initializeMap();
        // Get file name
        String filename = "C:/AssemblerTest/testFile2.asm";                         // Get File
        Path assemblyFile = Paths.get(filename);
        processedLines = new ArrayList<>();                                         // Initialize data structures
        orgCount = new ArrayList<>();
        
        lineArrList = new ArrayList<Line>();
        
        int orgNum = 0;
        int lineNum = 0;

        try {            
            
            PrintStream highMemOutput = new PrintStream(new FileOutputStream(highFilename));
            PrintStream lowMemOutput = new PrintStream(new FileOutputStream(lowFilename));
            PrintStream symTableOutput = new PrintStream(new FileOutputStream(symFilename));
            PrintStream stdOut = System.out;
            boolean orgFlag = false;
            String latestOrg = "";
            lines = (ArrayList) Files.readAllLines(
                    assemblyFile, Charset.defaultCharset());                        // Fill String ArLs with lines from file

            String instruction = "";
            orgArray = new ArrayList<>();
            String label = "   ";
            String line;
            for (int i = 0; i < lines.size(); i++) // For each String in the ArLs
            {
                line = lines.get(i);

                if (line.length() < 5) {
                    continue;
                }
                
                instruction = line.substring(5, 8);
                
                

                switch (instruction) {
                    case "ORG":
                        orgFlag = true;
                        org = line.substring(9, 12);
                    case "END":
                        break;
                    default:
                        if (orgFlag) {
                            orgFlag = false;
                        } else {
                            incrementOrg();
                        }
                        orgArray.add(org);
                        lineNum++;
                        label = line.substring(0, 3);
                        if (!"   ".equals(label)) {
                            labelMap.put(label, org);
                            inverseLabelMap.put(org, label);
//                            symTable+= label + ": " + org;
                            System.setOut(symTableOutput);
                            System.out.println(label + ": " + org);
                        }
                        if(line.length() >= 14){
                            line = line.substring(0, 14);
                        }
                        else
                        {
                            
                            //Pad string
                            line = String.format("%1$-" + 14 + "s", line);
                        }
                        processedLines.add(line);
//                        System.out.println(org + ":    " + line);
                }
                Line s = new Line(label, instruction, org);
                lineArrList.add(s);
            }
            
            String reference = "";
            instruction = "";
            String machInstruction = "";
            
            for( int i = 0; i < lineNum; i++)
            {
                // make an easy
                line = processedLines.get(i);
                
                
                reference = line.substring(9, 12).trim();
                
                // Replace label if necessary
                if( labelMap.get(reference) != null)
                {
                    reference = labelMap.get(reference);
                }
                
                // convert instruction
                instruction = line.substring(5, 8);
                machInstruction = instructionMap.get(instruction);
                if( machInstruction == null )
                {
                    switch( instruction ){
                        case "DEC":
                            int val = Integer.parseInt( reference, 10);
                            machInstruction = Integer.toHexString(val);
                            while( machInstruction.length() < 4)
                            {
                                machInstruction = "0" + machInstruction;
                            }
                            break;
                            
                            
                        case "HEX":
                            machInstruction = reference;
                            while( machInstruction.length() < 4)
                            {
                                machInstruction = "0" + machInstruction;
                            }
                            break;
                    }
                }
                else
                {
                    if( machInstruction.length() < 2)
                    {
                        // TODO: INDIRECT PROCESSING
                        if(line.charAt(13) == 'I')
                        {
                            int val = (Integer.parseInt(machInstruction)) + 8;
                            machInstruction = Integer.toHexString(val);
                        }
                        
                        
                        machInstruction += reference;
                    }
                }
                String highMem = "";
                String lowMem = "";
                if( machInstruction != null ){
                highMem = machInstruction.substring(0, 2);
                lowMem = machInstruction.substring(2, 4);
                }
                String machineCode = orgArray.get(i) + ":     " + highMem + "      " + lowMem;
                assembledLines.add(orgArray.get(i)+highMem+lowMem);
                lowMemory += orgArray.get(i) + ": " + lowMem;
                highMemory += orgArray.get(i) + ": " + highMem;
                System.setOut(highMemOutput);
                System.out.print(orgArray.get(i) + ": " + highMem);
                System.setOut(lowMemOutput);
                System.out.print(orgArray.get(i) + ": " + lowMem);
                System.setOut(stdOut);
                System.out.println( machineCode );
                machineInstructions.add(machineCode);
            }
            

            
            
            PrintStream unAssemOut = new PrintStream(unAssemFilename);
            System.setOut(unAssemOut);
            
            // Now, to un-assemble the code:
            
            
            
            for( int i = 0; i < assembledLines.size(); i++)
            {
                String lineData = assembledLines.get(i);
                String org = lineData.substring(0, 3);
                String instr = "";
                
                if(lineData.length() > 3){
                    instr = lineData.substring(4, 7);
                }
//                
//                if(i > 0){
//                    if(assembledLines.get(i-1).substring(0, i))
//                }
                
                if(inverseLabelMap.containsKey(org)){
                    System.out.print(inverseLabelMap.get(org) + ", ");
                }
                else{
                    System.out.print("     ");
                }
                
                if(inverseInstrMap.containsKey(instr)){
                    System.out.print(inverseInstrMap.get(instr));
                }
                else if(inverseInstrMap.containsKey(instr.substring(0, 1))){
                    System.out.print(inverseInstrMap.get(instr) + instr.substring(1, 3));
                    
                }
                else{
                    if("ORG".equals(instr)){
                        
                    }
                }
                
                
                // First line must always be ORG                
                if(i == 0){
                    System.out.print("ORG ");
                    System.out.println(assembledLines.get(0).substring(0, 3));
                }
               
            }
            

        } catch (IOException e) {
            //TODO: something
        }

    }
    

    private static void incrementOrg() {
        int orgVal = Integer.parseInt(org, 16);
        orgVal++;
        if (orgVal < 100) {
            org = String.format("%03d", orgVal);
        } else {
            org = Integer.toHexString(orgVal);
        }
    }


    public static void initializeMap() {
        instructionMap.put("AND", "0");
        instructionMap.put("ADD", "1");
        instructionMap.put("LDA", "2");
        instructionMap.put("STA", "3");
        instructionMap.put("BUN", "4");
        instructionMap.put("BSA", "5");
        instructionMap.put("ISZ", "6");

        instructionMap.put("CLA", "7800");
        instructionMap.put("CLE", "7400");
        instructionMap.put("CMA", "7200");
        instructionMap.put("CME", "7100");
        instructionMap.put("CIR", "7080");
        instructionMap.put("CIL", "7040");
        instructionMap.put("INC", "7020");
        instructionMap.put("SPA", "7010");
        instructionMap.put("SNA", "7008");
        instructionMap.put("SZA", "7004");
        instructionMap.put("SZE", "7002");
        instructionMap.put("HLT", "7001");

        instructionMap.put("INP", "f800");
        instructionMap.put("OUT", "f400");
        instructionMap.put("SKI", "f200");
        instructionMap.put("SKO", "f100");
        instructionMap.put("ION", "f080");
        instructionMap.put("IOF", "f040");
        
        // Inverse Instr
        inverseInstrMap.put("0", "AND");
        inverseInstrMap.put("1", "ADD");
        inverseInstrMap.put("2", "LDA");
        inverseInstrMap.put("3", "STA");
        inverseInstrMap.put("4", "BUN");
        inverseInstrMap.put("5", "BSA");
        inverseInstrMap.put("6", "ISZ");

        inverseInstrMap.put("7800", "CLA");
        inverseInstrMap.put("7400", "CLE");
        inverseInstrMap.put("7200", "CMA");
        inverseInstrMap.put("7100", "CME");
        inverseInstrMap.put("7080", "CIR");
        inverseInstrMap.put("7040", "CIL");
        inverseInstrMap.put("7020", "INC");
        inverseInstrMap.put("7010", "SPA");
        inverseInstrMap.put("7008", "SNA");
        inverseInstrMap.put("7004", "SZA");
        inverseInstrMap.put("7002", "SZE");
        inverseInstrMap.put("7001", "HLT");

        inverseInstrMap.put("f800", "INP");
        inverseInstrMap.put("f400", "OUT");
        inverseInstrMap.put("f200", "SKI");
        inverseInstrMap.put("f100", "SKO");
        inverseInstrMap.put("f080", "ION");
        inverseInstrMap.put("f040", "IOF");
        
        
    }

    static HashMap<String, String> instructionMap = new HashMap<>();
    static HashMap<String, String> labelMap = new HashMap<>();
    static HashMap<String, String> inverseInstrMap = new HashMap<>();
    static HashMap<String, String> inverseLabelMap = new HashMap<>();
    private static String org;
    private static ArrayList<String> orgCount;
    static ArrayList<String> lines = new ArrayList<>();
    static ArrayList<String> orgArray;
    static ArrayList<String> processedLines = new ArrayList<>();
    static ArrayList<String> machineInstructions = new ArrayList<>();
    static ArrayList<Line> lineArrList = new ArrayList<>();
    static String highMemory = "";
    static String lowMemory = "";
    static String symTable = "";
    static String symFilename = "C:/AssemblerTest/table.sym";
    static String highFilename = "C:/AssemblerTest/highmem.txt";
    static String lowFilename = "C:/AssemblerTest/lowmem.txt";
    static String unAssemFilename = "C:/AssemblerTest/unassem.txt";
    static ArrayList<String> assembledLines = new ArrayList<>();
    static ArrayList<String> unAssembledLines = new ArrayList<>();
    
}
