package bigclonebench2_1;

import bigclonebench2_0.BCB2_0;
import config.Config;
import util.TempUtil;

import java.io.*;



public class BCB2_1
{
    /**
     * @author C M Khaled Saifullah
     * The main functiontion that need to be run.
     * The method collects all files from the BigCloneBench2.0/clones directory and
     * merge then with BigCloneBench2.0/functions to create BigCloneBench2.1
     * @param args: Not necessary
     */
    public static void main(String[] args)
    {
        File folder = new File(Config.BIGCLONEBENCH_2_0_PATH+ "clones");
        File[] listofFiles = folder.listFiles();
        if (listofFiles.length > 0)
        {
            for(File file:listofFiles)
            {
                mergeFunctionsClones(file);
            }
        }

    }

    /**
     * The Method collect each line of the clonefile and collect information of both code fragment from BCB2.0 and merge them to create a single entry
     * @param clonefile: The clonefile from BCB2.0
     */
    private static void mergeFunctionsClones(File clonefile)
    {
        //Splitting file name to get the fileid
        String cloneFilePath = clonefile.getPath();
        String[] file_name_token = cloneFilePath.split("_");
        String sLabel = file_name_token[file_name_token.length-1].substring(0,file_name_token[file_name_token.length-1].length()-4);

        long file_id=0;
        try {
            file_id= Long.parseLong(sLabel);
        }
        catch (NumberFormatException ex)
        {
            System.out.println("File ID cannot be retrived in bigclonebench2_1.BCB2_1 Class on mergeFunctionsClones");
            ex.printStackTrace();
        }


        //Reading each line of file and merging function and clones
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(clonefile));
            String sCurrentLine = null;
            sCurrentLine = br.readLine();
            System.out.println("Merging Functions and Clones file of " + cloneFilePath);
            while ((sCurrentLine=br.readLine())!=null)
            {
                String[] token = sCurrentLine.split(",");
                String functionality_id = token[2];
                //Sending the tokens to line processor to process the tokens and create entry for BCB2.1
                String line = lineProcessor(token);

                //Write tje entry in BCB2.1/bcb/functionalityid/ directory
                String filePath = Config.BIGCLONEBENCH_2_1_PATH+"bcb"+ File.separator+functionality_id+File.separator+functionality_id+"_"+file_id+".csv";
                BCB2_0.writeData(filePath,line,Config.BCB_2_1_headings);

            }
            br.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * The method takes the tokens of a line seperated by commas and then collect code fragements info and create a single line for BCB2.1
     * @param token: A list of tokens from the entry of BCB2.0/clones
     * @return The entry for the BCB2.1
     */
    private static String lineProcessor(String[] token)
    {
        long functionIdOne = Long.parseLong(token[0]);
        long functionIdtwo = Long.parseLong(token[1]);
        //Checking the function id from the BCB2.0/functions/ directory
        String code1 = TempUtil.checkFunctionId(functionIdOne);
        String code2 = TempUtil.checkFunctionId(functionIdtwo);
        String[] code1token = code1.split(",");
        String[] code2token = code2.split(",");
        String type1 = code1token[1];
        String name1 = code1token[2];
        String startLine1 = code1token[3];
        String endLine1 = code1token[4];
        String project1 = code1token[6];
        String type2 = code2token[1];
        String name2 = code2token[2];
        String startLine2 = code2token[3];
        String endLine2 = code2token[4];
        String project2 = code2token[6];
        String intraStatus = "FALSE";
        if(project1.equalsIgnoreCase(project2))
            intraStatus = "TRUE";


        String line = functionIdOne+","+
                            type1+","+
                            name1+","+
                            startLine1+","+
                            endLine1+","+
                            functionIdtwo+","+
                            type2+","+
                            name2+","+
                            startLine2+","+
                            endLine2+",";

        for(int i = 3; i <= 14; i++ )
        {
            line += token[i]+",";
        }

        line += intraStatus;
        return line;
    }


}
