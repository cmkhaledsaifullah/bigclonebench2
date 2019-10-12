package util;

import bigclonebench1_0.BCB1_0;
import config.Config;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


/**
 * @author C M Khaled Saifullah
 * This class is created for some intermediate works. The user need not know about its functionality.
 */
public class TempUtil
{
    static String filepath = Config.BIGCLONEBENCH_2_0_PATH;
    public static void main(String[] args)
    {
        //checkFunctionId(7460);
        //checkTableData("clones");
        try {
            addLabel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String checkFunctionId(long functionID)
    {
        long functionId = functionID;
        String sCurrentLine=null;
        long fileindex = functionId/2500;
        String filePath = Config.BIGCLONEBENCH_2_0_PATH + "functions" + File.separator + fileindex + ".csv";
        //System.out.println(functionId+" " + filePath);
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
            while((sCurrentLine=br.readLine())!=null)
            {
                if(sCurrentLine.startsWith(functionId+","))
                {
                    br.close();
                    return sCurrentLine;
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sCurrentLine;
    }

    public static void checkTableData(String tableName) {
        String sql = "SELECT * FROM " + tableName + " WHERE functionality_id = 2";
        try
        {
            Connection conn = BCB1_0.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("testFile.csv")));
            bw.write("Function_Id_one,Function_Id_Two,Functionality_Id,Type,Syntactic_Type,Similairty_Line,Similarity_Token,Min_Size,Max_Size,Min_Preety_Size, Max_Pretty_Size, Min_Judges,Min_Confidence,Min_Tokens,Max_Tokens,Internal");
            bw.newLine();
            while (rs.next())
            {
                long functionality_id = rs.getLong("functionality_id");
                long function_id_one = rs.getLong("function_id_one");
                long function_id_two = rs.getLong("function_id_two");
                String type = rs.getString("type");
                int syntactic_type = rs.getInt("syntactic_type");
                double similarity_line = rs.getDouble("similarity_line");
                double similarity_token = rs.getDouble("similarity_token");
                int min_size = rs.getInt("min_size");
                int max_size = rs.getInt("max_size");
                int min_pretty_size = rs.getInt("min_pretty_size");
                int max_pretty_size = rs.getInt("max_pretty_size");
                int min_judges = rs.getInt("min_judges");
                int min_confidence = rs.getInt("min_confidence");
                String min_tokens = rs.getString("min_tokens");
                String max_tokens = rs.getString("max_tokens");
                String internal = rs.getString("internal");

                type = type.replace(",", ";");
                min_tokens = min_tokens.replace(",", ";");
                max_tokens = max_tokens.replace(",", ";");
                internal = internal.replace(",", ";");

                String line = function_id_one + "," + function_id_two + "," + functionality_id + "," + type + "," + syntactic_type + "," + similarity_line
                        + "," + similarity_token + "," + min_size + "," + max_size + "," + min_pretty_size + "," + max_pretty_size + "," + min_judges
                        + "," + min_confidence + "," + min_tokens + "," + max_tokens + "," + internal;
                bw.write(line);
                bw.newLine();

            }
            bw.close();
            stmt.close();
            conn.close();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void addLabel() throws IOException {
        for (int i = 0; i <= 8914; i++)
        {
            File file = new File(filepath+"functions"+File.separator+i+".csv");
            if(!file.exists())
            {
                String prev = filepath+"functions"+File.separator+(i-1)+".csv";
                BufferedReader br = new BufferedReader(new FileReader(new File(prev)));
                String sCurrentLine = null;
                int temp = 0;
                while ((sCurrentLine=br.readLine())!=null)
                {
                    temp++;
                }
                System.out.println(i +" "+prev+" "+temp);
            }
        }
    }
}
