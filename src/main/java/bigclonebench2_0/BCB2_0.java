package bigclonebench2_0;

import bigclonebench1_0.BCB1_0;
import config.Config;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author C M Khaled Saifullah
 * The class collects data from BigCloneBench1.0 jdbc h2 bigclonebench1_0 and create BigCloneBench2.0 dataset.
 */
public class BCB2_0
{
    /**
     * Executes the dunction describes bellow
     * @param args: An array of String, that is not used
     */
    public static void main(String[] args)
    {

        //choices are: 'functionalities', 'functions' and 'clones'
        String tableName = "clones";
        //getAllTableNames();
        //getTableColums(tableName);
        //numOfEntry(tableName);
        getTableData(tableName);
        //getMaximumID();

    }

    /**
     * The method collect all the table name of the BigCLoneBench1.0 jdbc h2 bigclonebench1_0.
     * @return A list of String where each represent name of each table at BigCloneBench1.0
     */
    public static List<String> getAllTableNames()
    {
        Connection conn = null;
        List<String> tableName = new ArrayList<String>();
        try {
            conn = BCB1_0.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            while (rs.next()) {
                tableName.add(rs.getString(3));
                System.out.println(rs.getString(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return tableName;
    }


    /**
     * The function retrives the column name of a particular table at BigCloneBench1.0
     * @param tablename: name of the table
     * @return A list of column names of the table passed through the parameter
     */

    public static List<String> getTableColums(String tablename)
    {
        List<String> columns = new ArrayList<String>();
        String sql;
        if(tablename.equals("functions"))
            sql = "SELECT * FROM " + tablename + " WHERE id = 1";
        else if(tablename.equals("clones"))
            sql = "SELECT * FROM " + tablename + " WHERE functionality_id = 1";
        else
            sql = "SELECT * FROM " + tablename + "";
        Connection conn = null;
        try {
            conn = BCB1_0.getConnection();
            Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
            //stmt.setFetchSize(Integer.MIN_VALUE);
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            // The column count starts from 1
            for (int i = 1; i <= columnCount; i++ ) {
                String name = rsmd.getColumnName(i);
                System.out.println(name);
                columns.add(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return columns;
    }


    /**
     * The method counts the number of rows for the table passed through parameter at BigCloneBench1.0
     * @param tablename: name of the table
     * @return A long number representing the number of rows at the table
     */
    public static long numOfEntry(String tablename)
    {
        long retval = 0;
        String sql = "SELECT count(1) FROM " + tablename + "";
        try {
            Connection conn = BCB1_0.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next())
                retval = rs.getLong(1);
            stmt.close();
            conn.close();
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        System.out.println("Number of rows for table "+tablename+" is: "+retval);
        return retval;
    }


    /**
     * The method collects the data from the table passed as the parameter and creates BigCloneBench2.0.
     * We collect data from 'functionalities', 'functions' and 'clones' table here.
     * @param tableName: name of the table. Choices are: 'functionalities', 'functions' and 'clones'
     */
    public static void getTableData(String tableName)
    {
        Connection conn = null;
        //Colelct data for functionalities table of BigccloneBench1.0
        if(tableName.equalsIgnoreCase("functionalities")) {
            String sql = "SELECT * FROM " + tableName + "";
            try {
                conn = BCB1_0.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    String search_heuristic = rs.getString("SEARCH_HEURISTIC");
                    name = name.replace(",", ";");
                    description = description.replace(",", ";");
                    search_heuristic = search_heuristic.replace(",", ";");
                    String line = rs.getLong("id") + "," +
                                    name + "," +
                                    description + "," +
                                    search_heuristic;
                    String filepath = Config.BIGCLONEBENCH_2_0_PATH+"/functionalities.csv";
                    writeData(filepath, line,Config.BCB_2_0_functionalities_headings);
                }
                stmt.close();
                conn.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }

        }
        //Colelct data for function table of BigccloneBench1.0
        else if(tableName.equalsIgnoreCase("functions"))
        {
            long index = 0;
            long counter = 0;
            long falseEnteryIndex = 0;
            String filepath = Config.BIGCLONEBENCH_2_0_PATH+"/functions/"+index+".csv";
            for(long i = 1; i <= Config.maxFunctionId; i++)
            {
                String sql = "SELECT * FROM " + tableName + " WHERE id ="+i;
                try {
                    conn = BCB1_0.getConnection();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    while (rs.next()) {

                        String name = rs.getString("name");
                        String type = rs.getString("type");
                        int startline = rs.getInt("startline");
                        int endline = rs.getInt("endline");
                        long id = rs.getLong("id");
                        int normalized_size = rs.getInt("normalized_size");
                        String project = rs.getString("project");
                        String tokens = rs.getString("tokens");
                        String internal = rs.getString("internal");



                        try {
                            name = name.replace(",", ";");
                            type = type.replace(",", ";");
                            project = project.replace(",", ";");
                            tokens = tokens.replace(",", ";");
                            internal = internal.replace(",",";");
                        }
                        catch (NullPointerException ex)
                        {
                            falseEnteryIndex = counter/2500;
                            String sLine = Config.BCB_2_0_functions_headings+"";
                            sLine = sLine.replace(",",";");
                            filepath = Config.BIGCLONEBENCH_2_0_PATH+"/functionsfalseentry/"+falseEnteryIndex+".csv";
                            String line = id+","+type+","+name+","+sLine+","+endline+","+normalized_size+","+project+","+tokens+","+internal;
                            counter++;
                            writeData(filepath,line,Config.BCB_2_0_functions_headings);
                            continue;
                        }


                        String line = id + "," +
                                        type + "," +
                                        name + "," +
                                        startline + "," +
                                        endline + "," +
                                        normalized_size + "," +
                                        project + "," +
                                        tokens + "," +
                                        internal;

                        if(i%2500 == 0)
                        {
                            System.out.println("Functions are stored upto: "+i);

                        }
                        index = i/2500;
                        filepath = Config.BIGCLONEBENCH_2_0_PATH+"/functions/"+index+".csv";
                        writeData(filepath, line,Config.BCB_2_0_functions_headings);
                    }
                    stmt.close();
                    conn.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }

        }
        //Colelct data for clones table of BigccloneBench1.0
        else if(tableName.equalsIgnoreCase("clones"))
        {
            long index = 0;
            String filepath = Config.BIGCLONEBENCH_2_0_PATH+"/clones/functionalityId_0"+index+".csv";

            for(int i = 1; i <= Config.maxFunctionaliesId; i++)
            {
                String sql = "SELECT * FROM " + tableName + " WHERE functionality_id ="+i;
                try {
                    conn = BCB1_0.getConnection();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    int temp = 0;
                    index = 0;
                    filepath = Config.BIGCLONEBENCH_2_0_PATH+"/clones/functionalityId_"+i+"_"+index+".csv";
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
                        min_tokens = min_tokens.replace(",",";");
                        max_tokens = max_tokens.replace(",",";");
                        internal = internal.replace(",",";");

                        String line = function_id_one + "," +
                                        function_id_two+ "," +
                                        functionality_id+ "," +
                                        type + "," +
                                        syntactic_type + "," +
                                        similarity_line + "," +
                                        similarity_token + "," +
                                        min_size + "," +
                                        max_size + "," +
                                        min_pretty_size + "," +
                                        max_pretty_size+ "," +
                                        min_judges + "," +
                                        min_confidence+ "," +
                                        min_tokens + "," +
                                        max_tokens+ "," +
                                        internal;


                        if(temp%2500 == 0)
                        {
                            System.out.println("Clone Pairs of function id " +i + " are stored upto: "+temp);
                            index = temp/2500;
                            filepath = Config.BIGCLONEBENCH_2_0_PATH+"/clones/functionalityId_"+i+"_"+index+".csv";

                        }
                        writeData(filepath, line,Config.BCB_2_0_clones_headings);
                        temp++;
                    }
                    stmt.close();
                    conn.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }

            }
        }
    }


    /**
     * The function simple Writes the entry (line) in the file path.
     * @param filepath: The path of the file where the line will be written
     * @param line: The String value that need to be written
     * @param startline: The Heading of the file.
     */

    public static void writeData(String filepath, String line, String startline)
    {
        try {
            BufferedWriter bw = null;

            File file = new File(filepath);

            if(file.exists()) {
                bw = new BufferedWriter(new FileWriter(file, true));
            }
            else
            {
                bw = new BufferedWriter(new FileWriter(file));
                bw.write(startline);
                bw.newLine();
            }
            bw.write(line);
            bw.newLine();
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The method collects the number of clone pair for each functionality id
     * @return A list of long variable that represent the number of clone pairs for each functionality id
     * @throws IOException: if the file is not found
     */
    private static List<Long> getNumberOfClonePairsPerFunctionalityID() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(Config.BIGCLONEBENCH_2_0_PATH+"/clonePairsPerFunctionalities.txt")));
        List<Long> retval = new ArrayList<Long>();
        String sCurrentLine = null;
        while ((sCurrentLine=br.readLine())!=null)
        {
            String[] token = sCurrentLine.split(" ");
            retval.add(Long.parseLong(token[token.length-1]));
        }
        br.close();
        return retval;
    }


    /**
     * The Method finds the maximum number of entry for the function table
     * @return The long variable that represents the maximum value of a clone id
     */
    public static long getMaximumID()
    {
        long maxID = 0;
        try {
            Connection conn = BCB1_0.getConnection();
            Statement s = conn.createStatement();
            s.execute("SELECT MAX(id) FROM functions");
            ResultSet rs = s.getResultSet(); //
            if ( rs.next() )
            {
                maxID = rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(maxID);
        return maxID;
    }

}
