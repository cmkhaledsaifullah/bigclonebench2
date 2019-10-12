package config;

import java.io.File;

/**
 * @author C M Khaled Saifullah
 * The class contains all the global variable need to run the project.
 */
public class Config {

    public static final String ROOT_PATH = "/home/khaledkucse/BigCloneBench";

    public static final String BIGCLONEBENCH_1_0_PATH = ROOT_PATH + File.separator+ "BigCloneBench1.0/";

    public static final String BIGCLONEBENCH_2_1_PATH = ROOT_PATH+File.separator+"BigCloneBench2.1/";

    public static final String BIGCLONEBENCH_2_0_PATH = ROOT_PATH+File.separator+"BigCloneBench2.0/";

    public static final String IJADATASET_PATH = ROOT_PATH+File.separator+"ijadataset/";

    public static final String CLONE_PAIRS_CSV_PATH = ROOT_PATH+File.separator+"input_CSV_file/Result_ClonePair.csv";

    public static final String OUTPUT_WITH_SOURCE_CODE_XML_PATH = ROOT_PATH+File.separator+"output_XML_file_with_source_code/Result_ClonePair";


    public static final Boolean IS_INPUT_CLONE_PAIR = Boolean.TRUE;


    public static final long maxFunctionId = 23725449;
    public static final long maxFunctionaliesId = 45;




    public static final String  BCB_2_0_functionalities_headings = "Id," +
                                                                   "Name," +
                                                                   "Description," +
                                                                   "Search_Heuristics";
    public static final String BCB_2_0_functions_headings = "Id," +
                                                            "Type," +
                                                            "Name," +
                                                            "Start_Line," +
                                                            "End_Line," +
                                                            "Normalized_Size," +
                                                            "Project," +
                                                            "Tokens," +
                                                            "Internal";

    public static final String BCB_2_0_clones_headings = "Function_Id_one," +
                                                         "Function_Id_Two," +
                                                         "Functionality_Id," +
                                                         "Type,Syntactic_Type," +
                                                         "Similairty_Line," +
                                                         "Similarity_Token," +
                                                         "Min_Size,Max_Size," +
                                                         "Min_Preety_Size," +
                                                         "Max_Pretty_Size," +
                                                         "Min_Judges," +
                                                         "Min_Confidence," +
                                                         "Min_Tokens," +
                                                         "Max_Tokens," +
                                                         "Internal";

    public static final String BCB_2_1_headings = "Function_Id_one," +
                                                  "Type_One,"+
                                                  "Name_One,"+
                                                  "Start_Line_One,"+
                                                  "End_Line_One,"+
                                                  "Function_Id_Two," +
                                                  "Type_Two,"+
                                                  "Name_Two,"+
                                                  "Start_Line_Two,"+
                                                  "End_Line_Two,"+
                                                  "Type,Syntactic_Type," +
                                                  "Similairty_Line," +
                                                  "Similarity_Token," +
                                                  "Min_Size,Max_Size," +
                                                  "Min_Preety_Size," +
                                                  "Max_Pretty_Size," +
                                                  "Min_Judges," +
                                                  "Min_Confidence," +
                                                  "Min_Tokens," +
                                                  "Max_Tokens," +
                                                  "Intra_Clone";

}
