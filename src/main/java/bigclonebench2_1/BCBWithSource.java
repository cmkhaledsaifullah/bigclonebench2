package bigclonebench2_1;

import bigclonebench2_0.BCB2_0;
import config.Config;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/*
@author C M Khaled Saifullah
This class combine the bigclonebench 2.1 with the source code in a single file. Prerequisite: BCB2.1.java
 */
public class BCBWithSource
{
    /**
     * This is the main file that create the BCB2.1 with the source of the participating code fragments.
     * @param args: Used as defult.
     */
    public static void main(String[] args)
    {
        File bcbfolder = new File(Config.BIGCLONEBENCH_2_1_PATH+ "bcb/");
        File[] bcblistofFiles = bcbfolder.listFiles();
        for (File bcbfile: bcblistofFiles)
        {
            //Pick only five functionality id for test purpose. To take all data just omit the conditional statement.
            if(bcbfile.getName().equals("2")||bcbfile.getName().equals("3")||bcbfile.getName().equals("4")||bcbfile.getName().equals("30")||bcbfile.getName().equals("35")) {
                try {
                    System.out.println("Working on Directory: "+bcbfile.getName());
                    addSource(bcbfile.getName(),bcbfile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Process Fininshed!!!");
    }

    /**
     * The method is responsible to extract the data from IJA Dataset and combine the code fragements with clone pair.
     * @param bcbFileName: The name of the file that we will be used
     * @param bcbFileAbsPath: The absolute path of the BCB file
     * @throws IOException: If the file or any IO operation failed to execute
     * @throws NullPointerException: when no files in the listFiles is found
     */
    private static void addSource(String bcbFileName, String bcbFileAbsPath) throws IOException,NullPointerException
    {
        List<ClonePair> allclonePairs;
        File bcbfolder = new File(bcbFileAbsPath);
        File[] bcblistofFiles = bcbfolder.listFiles();
        for (File bcbfile: bcblistofFiles)
        {
            allclonePairs = new ArrayList<>();
            BufferedReader br =new BufferedReader(new FileReader(bcbfile));
            String sCurrentLine = br.readLine();
            while((sCurrentLine = br.readLine())!=null)
            {
                String[] token = sCurrentLine.split(",");
                ClonePair clonePair = new ClonePair(token[0],token[1],token[2],Long.parseLong(token[3]),Long.parseLong(token[4]),token[5],token[6],token[7],Long.parseLong(token[8]),Long.parseLong(token[9]),
                        token[10],token[11],token[12],token[13],token[14],token[15],token[16],token[17],token[18],token[19],token[20],token[21],token[22]);
                allclonePairs.add(clonePair);
            }
            br.close();

            for(ClonePair clonePair: allclonePairs)
            {
                String path1 = bcbFileName+"/"+clonePair.getType_1()+"/"+clonePair.getName_1();
                String path2 = bcbFileName+"/"+clonePair.getType_2()+"/"+clonePair.getName_2();
                List<String> code1 = getSourceCode(path1,clonePair.getStarLine_1(),clonePair.getEndLine_1());
                List <String> code2 = getSourceCode(path2,clonePair.getStarLine_2(),clonePair.getEndLine_2());
                writeSourceCode(bcbFileName,bcbfile.getName(),clonePair,code1,code2);
            }
        }
    }

    /**
     * The method is a supporting method for addSource. It collects the source code of both code fragments.
     * @param path: Path of the IJA dataset
     * @param startLine: Start line of the code fragement
     * @param endLine: End line of the code fragment
     * @return List of String that represent the code fragement
     * @throws IOException: When file is not found for IJA dataset's file
     */
    private static List<String> getSourceCode(String path, long startLine, long endLine) throws IOException {
        File ijaFile = new File(Config.IJADATASET_PATH+ "bcb_reduced/"+path);
        List<String> returnedCode = new ArrayList<>();
        if(ijaFile.exists())
        {
            if(startLine > endLine)
            {
                long temp = startLine;
                startLine = endLine;
                endLine = temp;
            }
            BufferedReader ijabr = new BufferedReader(new FileReader(ijaFile));
            int linenumber = 1;
            String sCurrentLine = "";
            while ((sCurrentLine= ijabr.readLine())!=null)
            {
                if(linenumber >= startLine && linenumber <= endLine)
                {
                    returnedCode.add(sCurrentLine);
                }
                linenumber++;
            }
            ijabr.close();
        }
        else
        {
            System.err.println("No Such file exists: "+ ijaFile.getAbsolutePath());
        }

        return returnedCode;
    }


    /**
     * The method writes the BCB2.1 with source database.
     * @param bcbfoldername: The string value represents the folder name/ functionality id.
     * @param fileName: The string value represents the file name where the data will be read
     * @param clonePair: The information of the clone pair need to be written
     * @param code1: Code of the 1st participating member of clone pair
     * @param code2: Code of the 2nd participating member of clone pair
     */
    private static void writeSourceCode(String bcbfoldername, String fileName, ClonePair clonePair, List<String> code1, List<String> code2)
    {
        String path1 = bcbfoldername+"/"+clonePair.getType_1()+"/"+clonePair.getName_1();
        String path2 = bcbfoldername+"/"+clonePair.getType_2()+"/"+clonePair.getName_2();

        String newfilePath = Config.BIGCLONEBENCH_2_1_PATH+"bcb_with_source_code"+File.separator+bcbfoldername+ File.separator+fileName+"_with_source.txt";
        String line = "================================================================================\n";
        line += "Function_Id_One = "+clonePair.getFunction_id_1()+"\n";
        line += "Function_Id_Two = "+clonePair.getFunction_id_2()+"\n";
        line += "Type = "+clonePair.getType()+"\n";
        line += "Syntactic Type = "+clonePair.getSyntactic_type()+"\n";
        line += "Similarity Line = "+clonePair.getSimilarity_line()+"\n";
        line += "Similarity Token = "+clonePair.getSimilarity_token()+"\n";
        line += "Min Size = "+clonePair.getMin_size()+"\n";
        line += "Max Size = "+clonePair.getMax_size()+"\n";
        line += "Min Preety Size = "+clonePair.getMin_preety_size()+"\n";
        line += "Max Preety Size = "+clonePair.getMax_preety_size()+"\n";
        line += "Min Judge = "+clonePair.getMin_judge()+"\n";
        line += "Min Confidence = "+clonePair.getMin_confidence()+"\n";
        line += "Min Token = "+clonePair.getMin_token()+"\n";
        line += "Max Token = "+clonePair.getMax_token()+"\n";
        line += "Intra Clone = "+clonePair.getIs_intra_clone()+"\n";
        line += "================================================================================\n";
        line += "================================================================================\n";
        line += "Code Fragment 1: "+path1+", StartLine = "+clonePair.getStarLine_1()+", End Line = "+clonePair.getEndLine_1()+"\n";
        line += "================================================================================\n";
        for (String codeline:code1)
        {
            line += codeline+"\n";
        }
        line += "================================================================================\n";
        line += "Code Fragment 2: "+path2+", StartLine = "+clonePair.getStarLine_2()+", End Line = "+clonePair.getEndLine_2()+"\n";
        line += "================================================================================\n";
        for (String codeline:code2)
        {
            line += codeline+"\n";
        }
        line += "================================================================================\n";
        line += "\n";

        BCB2_0.writeData(newfilePath,line,"");

    }

}
