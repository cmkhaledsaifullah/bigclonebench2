package bigclonebench2_1;


/**
 * @author C M Khaled Saifullah
 * The class is a model class for each clone pair of BCB 2.1.
 * All the entity of a clone pair with its getter and setter are defined in the class.
 */
public class ClonePair
{
    String function_id_1;
    String type_1;
    String name_1;
    long starLine_1;
    long endLine_1;

    String function_id_2;
    String type_2;
    String name_2;
    long starLine_2;
    long endLine_2;

    String type;
    String syntactic_type;
    String similarity_line;
    String similarity_token;
    String min_size;
    String max_size;
    String min_preety_size;
    String max_preety_size;
    String min_judge;
    String min_confidence;
    String min_token;
    String max_token;
    String is_intra_clone;

    public ClonePair(String function_id_1, String type_1, String name_1, long starLine_1, long endLine_1, String function_id_2, String type_2, String name_2, long starLine_2, long endLine_2, String type, String syntactic_type, String similarity_line, String similarity_token, String min_size, String max_size, String min_preety_size, String max_preety_size, String min_judge, String min_confidence, String min_token, String max_token, String is_intra_clone) {
        this.function_id_1 = function_id_1;
        this.type_1 = type_1;
        this.name_1 = name_1;
        this.starLine_1 = starLine_1;
        this.endLine_1 = endLine_1;
        this.function_id_2 = function_id_2;
        this.type_2 = type_2;
        this.name_2 = name_2;
        this.starLine_2 = starLine_2;
        this.endLine_2 = endLine_2;
        this.type = type;
        this.syntactic_type = syntactic_type;
        this.similarity_line = similarity_line;
        this.similarity_token = similarity_token;
        this.min_size = min_size;
        this.max_size = max_size;
        this.min_preety_size = min_preety_size;
        this.max_preety_size = max_preety_size;
        this.min_judge = min_judge;
        this.min_confidence = min_confidence;
        this.min_token = min_token;
        this.max_token = max_token;
        this.is_intra_clone = is_intra_clone;
    }

    public ClonePair() {
    }

    public String getFunction_id_1() {
        return function_id_1;
    }

    public void setFunction_id_1(String function_id_1) {
        this.function_id_1 = function_id_1;
    }

    public String getType_1() {
        return type_1;
    }

    public void setType_1(String type_1) {
        this.type_1 = type_1;
    }

    public String getName_1() {
        return name_1;
    }

    public void setName_1(String name_1) {
        this.name_1 = name_1;
    }

    public long getStarLine_1() {
        return starLine_1;
    }

    public void setStarLine_1(long starLine_1) {
        this.starLine_1 = starLine_1;
    }

    public long getEndLine_1() {
        return endLine_1;
    }

    public void setEndLine_1(long endLine_1) {
        this.endLine_1 = endLine_1;
    }

    public String getFunction_id_2() {
        return function_id_2;
    }

    public void setFunction_id_2(String function_id_2) {
        this.function_id_2 = function_id_2;
    }

    public String getType_2() {
        return type_2;
    }

    public void setType_2(String type_2) {
        this.type_2 = type_2;
    }

    public String getName_2() {
        return name_2;
    }

    public void setName_2(String name_2) {
        this.name_2 = name_2;
    }

    public long getStarLine_2() {
        return starLine_2;
    }

    public void setStarLine_2(long starLine_2) {
        this.starLine_2 = starLine_2;
    }

    public long getEndLine_2() {
        return endLine_2;
    }

    public void setEndLine_2(long endLine_2) {
        this.endLine_2 = endLine_2;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSyntactic_type() {
        return syntactic_type;
    }

    public void setSyntactic_type(String syntactic_type) {
        this.syntactic_type = syntactic_type;
    }

    public String getSimilarity_line() {
        return similarity_line;
    }

    public void setSimilarity_line(String similarity_line) {
        this.similarity_line = similarity_line;
    }

    public String getSimilarity_token() {
        return similarity_token;
    }

    public void setSimilarity_token(String similarity_token) {
        this.similarity_token = similarity_token;
    }

    public String getMin_size() {
        return min_size;
    }

    public void setMin_size(String min_size) {
        this.min_size = min_size;
    }

    public String getMax_size() {
        return max_size;
    }

    public void setMax_size(String max_size) {
        this.max_size = max_size;
    }

    public String getMin_preety_size() {
        return min_preety_size;
    }

    public void setMin_preety_size(String min_preety_size) {
        this.min_preety_size = min_preety_size;
    }

    public String getMax_preety_size() {
        return max_preety_size;
    }

    public void setMax_preety_size(String max_preety_size) {
        this.max_preety_size = max_preety_size;
    }

    public String getMin_judge() {
        return min_judge;
    }

    public void setMin_judge(String min_judge) {
        this.min_judge = min_judge;
    }

    public String getMin_confidence() {
        return min_confidence;
    }

    public void setMin_confidence(String min_confidence) {
        this.min_confidence = min_confidence;
    }

    public String getMin_token() {
        return min_token;
    }

    public void setMin_token(String min_token) {
        this.min_token = min_token;
    }

    public String getMax_token() {
        return max_token;
    }

    public void setMax_token(String max_token) {
        this.max_token = max_token;
    }

    public String getIs_intra_clone() {
        return is_intra_clone;
    }

    public void setIs_intra_clone(String is_intra_clone) {
        this.is_intra_clone = is_intra_clone;
    }
}
