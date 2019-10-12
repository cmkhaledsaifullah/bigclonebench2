package bigclonebench2_1;

import config.Config;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author C M Khaled Saifullah
 * The class takes csv input of clone pair and clone group file and add source code for each code fragment and
 * return a xml formatted file representing clone pair and clone gropu
 */
public class BCBWithXMLSource
{
    /**
     * This is the first strting point of the program that convert a csv clone pair and clone group file into a xml file with code attached.
     * @param args: default, no need to change
     * @throws IOException: If IO related failure happens
     */
    public static void main(String[] args) throws IOException {
        //Taking global vairables
        String ijadataset = Config.IJADATASET_PATH;
        String inputFilePath = Config.CLONE_PAIRS_CSV_PATH;
        String outputFilePath = Config.OUTPUT_WITH_SOURCE_CODE_XML_PATH;
        //Creating folders if not available
        new File(outputFilePath).mkdirs();



        //Accumulating all IJA Dataset files in a list
        File ijaFile = new File(ijadataset);
        List<File> allFiles = new ArrayList<>();
        allFiles = getAllFiles(ijaFile,allFiles);

        //Creating a hashmap for all dataset files. It is used to search the file. The hash map is <file name, absolute path of the file>
        Map<String,String> namePath= new HashMap<>();
        for (File file: allFiles)
        {
            String path = file.getAbsolutePath();
            String[] token = path.split(File.separator);
            String name = token[token.length-2]+File.separator+token[token.length-1];
            namePath.put(name,path);
        }


        //Reading each line of the input CSV file
        BufferedReader br = new BufferedReader(new FileReader(new File(inputFilePath)));
        String sCurrentLine = "";
        int counter = 1;
        List<String> texts = new ArrayList<>();
        while((sCurrentLine = br.readLine())!=null)
        {
            //If an empty line found, then a new clonegroup is created.
            if (sCurrentLine.isEmpty())
            {
                writeinFile(outputFilePath+File.separator+"group_"+counter+".xml",texts,true);
                System.out.println("Printing in: "+ outputFilePath+File.separator+"group_"+counter+".xml");
                counter++;
                texts.clear();
                continue;
            }
            //checking whether we are convering clone pair or clone group
            else if(Config.IS_INPUT_CLONE_PAIR)
            {
                if(counter%10000 == 0)
                {
                    writeinFile(outputFilePath+File.separator+"pair"+counter+".xml",texts,false);
                    System.out.println("Printing in: "+ outputFilePath+File.separator+"pair_"+counter+".xml");
                    texts.clear();
                }
                counter++;
            }

            //adding clone information from each line of the input csv
            if(!sCurrentLine.isEmpty())
            {
                texts.add("<clone>");

                String[] token = sCurrentLine.split(",");
                if (token.length<8)
                {
                    System.err.println("You need to provide input in CSV(Comman Seperated Value formate!!!). Error at Line: "+ counter);
                }
                ClonePair clonePair = new ClonePair();
                try
                {
                    clonePair.setType_1(token[0]);
                    clonePair.setName_1(token[1]);
                    clonePair.setStarLine_1(Long.parseLong(token[2]));
                    clonePair.setEndLine_1(Long.parseLong(token[3]));
                    clonePair.setType_2(token[4]);
                    clonePair.setName_2(token[5]);
                    clonePair.setStarLine_2(Long.parseLong(token[6]));
                    clonePair.setEndLine_2(Long.parseLong(token[7]));
                }
                catch (Exception ex)
                {
                    System.err.println("The Exact format would be: Type1,Name1,StartLine1,EndLine1,Type2,Name2,StartLine2,EndLine2 \n all StartLine1, EndLine1, StartLine2, EndLine2 need to be numeric.\n Error for Line Number "+counter );
                }


                //adding source code to the clone pair
                String fileName1 = clonePair.getType_1()+File.separator+clonePair.getName_1();
                String fileName2 = clonePair.getType_2()+File.separator+clonePair.getName_2();

                if(namePath.containsKey(fileName1) && namePath.containsKey(fileName2))
                {
                    texts.addAll(addCode(fileName1,namePath,clonePair,1));

                    texts.addAll(addCode(fileName2,namePath,clonePair,2));

                }
                else
                {
                    System.err.println("Cant find the source files in the ija dataset given at line: "+counter);
                }
                texts.add("</clone>");

            }
        }

        br.close();




    }

    /**
     * The method collect all files from the folder and subfolder of a directory
     * @param file: the location of the directory
     * @param files: A list of files stored previously
     * @return A list of files from the directory and subdirectory of the path given as the parameter
     */
    public static List<File> getAllFiles(File file, List<File> files)
    {
        if(file.isDirectory())
        {
            File[] lisofFiles = file.listFiles();
            for(File subfile: lisofFiles)
            {
                if(subfile.isDirectory())
                {
                    getAllFiles(subfile,files);
                }
                else {
                    files.add(subfile);
                }

            }

        }

        return files;

    }

    /**
     * The method collects source code from the IJA dataset and return to the output xml
     * @param fileName: The name of the IJA dataset filt
     * @param namePath: path of the file
     * @param clonePair: Clone pair object that has all information of the clone pair
     * @param pairNo: Is the clone is 1st or the seconf one
     * @return A list of String representing the code associated with xml tag
     * @throws IOException: If the file read is inturpted.
     */
    private static List<String> addCode(String fileName, Map<String,String> namePath, ClonePair clonePair, int pairNo) throws IOException
    {
        long starLine;
        long endLine;
        if (pairNo == 1)
        {
            starLine = clonePair.getStarLine_1();
            endLine = clonePair.getEndLine_1();

        }
        else
        {
            starLine = clonePair.getStarLine_2();
            endLine = clonePair.getEndLine_2();
        }
        List<String> texts = new ArrayList<>();
        String file_path = namePath.get(fileName);
        texts.add("<source file= \""+file_path+"\" startline= \""+starLine+"\" endline= \""+endLine+"\" />");
        texts.add("<code>");

        BufferedReader br = new BufferedReader(new FileReader(new File(file_path)));
        int line_number = 1;
        String text = "";
        while((text = br.readLine())!=null && line_number <= endLine)
        {
            if(line_number >= starLine)
            {
                text = text.replace("<","");
                text = text.replace(">","");
                text = text.replace("&&","");
                text = text.replace("&","");
                text = text.replaceAll("\\p{C}", "");

                texts.add(text);
            }
            line_number++;
        }

        br.close();

        texts.add("</code>");
        return texts;
    }

    /**
     * The method that write the xml formated clone pair and clone group in a file
     * @param fileName: The name of the file we like to write
     * @param texts: the List of string we like to write
     * @param isGroup: Is the information is clone group?
     * @throws IOException: When the file writing is intrupted.
     */

    public static void writeinFile(String fileName, List<String> texts, boolean isGroup) throws IOException {

        BufferedWriter bw =new BufferedWriter(new FileWriter(new File(fileName)));

        if(isGroup)
        {
            bw.write("<clonegroup>");
            bw.newLine();
        }


        else
        {
            bw.write("<clones>");
            bw.newLine();
        }


        for(String text: texts)
        {
            bw.write(text);
            bw.newLine();

        }

        if (isGroup)
        {
            bw.write("</clonegroup>");
            bw.newLine();
        }

        else
        {
            bw.write("</clones>");
            bw.newLine();
        }


        bw.close();

    }
}
