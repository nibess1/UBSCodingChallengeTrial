
/**
 * Assumption 1 : Maximum euclidean distance is less than 100.
 * 
 */

import java.util.*;
import java.io.*;


public class Main {

    public final static int MAX_DIST = 100;

    public static String readJsonFileToString(String filePath) {
        StringBuilder jsonString = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonString.append(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString.toString();
    }

    public static void writeFile(String filePath, String content){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
            System.out.println("Successfully written to the file: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static double manhattan_distance(int[] loc1, int[] loc2){
        double vert = loc1[0]- loc2[0];
        double hor = loc1[1] - loc2[1];
        return vert + hor;
    }
    public static double euclidean_distance(int[] loc1, int[] loc2){
        double vert = loc1[0]- loc2[0];
        double hor = loc1[1] - loc2[1];
        return Math.sqrt(vert* vert + hor * hor);
    }

    public static double distance_score(int[] loc1, int[] loc2){
        double dist = manhattan_distance(loc1, loc2);

        //return 50.0 /(1 + dist);
        return 50 - (50.0 / MAX_DIST * dist);

    }
    public static double calculate_weightage(Student student, School school){
        double weight = 0;
        String schoolname = school.getName();
        if(schoolname.equals(student.getVolunteer())){
            weight += 20;
        }
        if(schoolname.equals(student.getAlumni())){
            weight += 30;
        }
        weight += distance_score(student.getHomeLocation(), school.getLocation());
        return weight;
    }

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();

        if (args.length != 1) {
            System.out.println("Usage: java Main <filename>");
            System.exit(1);
        }

        // Get the file name from the first argument
        String fileName = args[0];
        JSONParser input = new JSONParser(readJsonFileToString(fileName));
        List<School> schools = new ArrayList<>();
        List<Student> students = new ArrayList<>();
        
        JSONObject test = input.parseObject();
        JSONArray studs = (JSONArray) test.get("students");
        JSONArray schs = (JSONArray) test.get("schools");

        for(int i = 0; i < studs.size(); i++){
             
            Student tempStudent = new Student((JSONObject) studs.get(i));
            students.add(tempStudent);
        }
        for(int i = 0; i < schs.size(); i++){
             
            School tempschool = new School((JSONObject) schs.get(i));
            schools.add(tempschool);
        }

        long parseTime = System.currentTimeMillis();
        System.out.println("time to parse = " + (parseTime - startTime));


        List<Assignment> studentAssignment = new ArrayList<>();
        for(Student student : students){
            for(School school : schools){
                studentAssignment.add(new Assignment(student, school, calculate_weightage(student, school)));
            }
        }

        long mathTime = System.currentTimeMillis();
        System.out.println("time to calculate = " + (mathTime - parseTime));

        studentAssignment.sort(Comparator.comparing(Assignment::getWeight).reversed().thenComparing(Assignment::getStudentId));

        long sortTime = System.currentTimeMillis();
        System.out.println("time to sort = " + (sortTime - mathTime));

        Set<Integer> addedStudents = new HashSet<>();
        //for(Assignment a: studentAssignment){
        //   System.out.println("Student "+ a.getStudentId() + " going to " + a.getSchool().getName() + " with score of " + a.getWeight());
        //}

        for (Assignment a : studentAssignment){
            School s = a.getSchool();
            Integer sId = a.getStudentId();
            if(!addedStudents.contains(sId)){
                if(s.allocateStudent(sId)){
                    addedStudents.add(sId);
                }
            }
        }

        Map<String, Integer[]> result = new TreeMap<>();

        for(School s : schools){
            if(s.getCurrentAllocation() == 0){
                continue;
            }
            Integer[] studentId = s.getStudentAllocations().toArray(new Integer[s.getCurrentAllocation()]);
            result.put(s.getName(), studentId);
        }
        
        long allocateTime = System.currentTimeMillis();

        System.out.println("time to allocate = " + (allocateTime - sortTime));

        StringBuilder fileContent = new StringBuilder("[");
        
        for (Map.Entry<String, Integer[]> entry : result.entrySet()) {
            fileContent.append(System.lineSeparator()).append("\t").append("{").append(System.lineSeparator());      
            String schoolName = entry.getKey();
            Integer[] studentInts = entry.getValue();

            fileContent.append("\t\"" + schoolName +"\"" + ": " + Arrays.toString(studentInts));
            fileContent.append(System.lineSeparator()).append("\t").append("}").append(",");

        }

        //remove last ,
        if(fileContent.charAt(fileContent.length() - 1) == ','){
            fileContent.deleteCharAt(fileContent.length() - 1);
        }

        if(result.size() == 0){
            fileContent.append("]");
        } else {
            fileContent.append(System.lineSeparator()).append("]");
        }
        

        writeFile("output.json", fileContent.toString());
    
        
    }


}

class Assignment {
    Student student;
    School school;
    double weight;
    
    public Assignment(Student student, School school, double weight) {
        this.student = student;
        this.school = school;
        this.weight = weight;
    }

    public Student getStudent() {
        return student;
    }

    public int getStudentId() {
        return student.getId();
    }

    public School getSchool() {
        return school;
    }

    public double getWeight() {
        return weight;
    }
 
    


}

class Student {
    int id;
    int[] homeLocation = new int[2];
    String alumni;
    String volunteer;

    public Student(int id, int[] homeLocation, String alumni, String volunteer) {
        this.id = id;
        this.homeLocation = homeLocation;
        this.alumni = alumni;
        this.volunteer = volunteer;
    }

    public Student(JSONObject input) {
        id = (int) input.get("id");
        JSONArray loc = (JSONArray) input.get("homeLocation");
        homeLocation[0] = (int) loc.get(0);
        homeLocation[1] = (int) loc.get(1);

        alumni = (String) input.getOrDefault("alumni", null);
        volunteer = (String) input.getOrDefault("volunteer", null);
    }

    public int getId() {
        return id;
    }

    public int[] getHomeLocation() {
        return homeLocation;
    }

    public String getAlumni() {
        return alumni;
    }

    public String getVolunteer() {
        return volunteer;
    }

    

}

class School {
    String name;
    int[] location = new int[2];
    int maxAllocation;
    int currentAllocation = 0;
    List<Integer> studentAllocations = new ArrayList<>();

    public School(String name, int[] location, int maxAllocation) {
        this.name = name;
        this.location = location;
        this.maxAllocation = maxAllocation;
    }

    public School(JSONObject input) {
        name = (String) input.get("name");
        JSONArray loc = (JSONArray) input.get("location");
        location[0] = (int) loc.get(0);
        location[1] = (int) loc.get(1);
        maxAllocation = (int) input.get("maxAllocation");
        
    }

    public String getName() {
        return name;
    }

    public int[] getLocation() {
        return location;
    }

    public int getMaxAllocation() {
        return maxAllocation;
    }

    public int getCurrentAllocation() {
        return currentAllocation;
    }

    public List<Integer> getStudentAllocations() {
        return studentAllocations;
    }

    public boolean allocateStudent(int studentId){
        if(currentAllocation == maxAllocation){
            return false;
        }

        studentAllocations.add(studentId);
        currentAllocation++;
        return true;
    }

    

    
}

// parsing JSON

class JSONObject {
    private Map<String, Object> map = new HashMap<>();

    public void put(String key, Object value) {
        map.put(key, value);
    }

    public Object get(String key) {
        return map.get(key);
    }

    public Object getOrDefault(String key, Object def){
        return map.getOrDefault(key, def);
    }

    @Override
    public String toString() {
        return map.toString();
    }
}

class JSONArray {
    private List<Object> list = new ArrayList<>();

    public void add(Object value) {
        list.add(value);
    }

    public Object get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }
    

    @Override
    public String toString() {
        return list.toString();
    }
}

class JSONParser {

    private int index = 0;
    private String json;

    public JSONParser(String json) {
        this.json = json.trim();
    }

    public JSONObject parseObject() {
        JSONObject jsonObject = new JSONObject();
        if (json.charAt(index) == '{') {
            index++;
            while (index < json.length() && json.charAt(index) != '}') {
                String key = parseString();
                index++; // skip ':'
                if (json.charAt(index) == ' ') {
                    index++;
                }
                Object value = parseValue();
                jsonObject.put(key, value);
                if (json.charAt(index) == ',') {
                    index++; // skip ','
                }
            }
            index++; // skip '}'
        }
        return jsonObject;
    }

    public JSONArray parseArray() {
        JSONArray jsonArray = new JSONArray();
        if (json.charAt(index) == '[') {
            index++;
            while (index < json.length() && json.charAt(index) != ']') {
                Object value = parseValue();
                jsonArray.add(value);
                if (json.charAt(index) == ',') {
                    index++; // skip ','
                }
                if (json.charAt(index) == ' ') {
                    index++; // skip ' '
                }
            }
            index++; // skip ']'
        }
        return jsonArray;
    }

    private Object parseValue() {
        char currentChar = json.charAt(index);
        if (currentChar == '"') {
            return parseString();
        } else if (currentChar == '{') {
            return parseObject();
        } else if (currentChar == '[') {
            return parseArray();
        } else if (Character.isDigit(currentChar) || currentChar == '-') {
            return parseNumber();
        } else if (json.startsWith("true", index)) {
            index += 4;
            return true;
        } else if (json.startsWith("false", index)) {
            index += 5;
            return false;
        } else if (json.startsWith("null", index)) {
            index += 4;
            return null;
        } else if (json.charAt(index) == ' ') {
            index++;
            return null;
        } else {
            throw new IllegalStateException("Unexpected value: " + json.charAt(index) + ", at index " + index);

        }
    }

    private String parseString() {
        index++; // skip opening '"'
        int start = index;
        while (json.charAt(index) != '"') {
            index++;
        }
        String result = json.substring(start, index);
        // System.out.println(result);
        index++; // skip closing '"'
        return result;
    }

    private Number parseNumber() {
        int start = index;
        if (json.charAt(index) == '-') {
            index++;
        }
        while (index < json.length() && (Character.isDigit(json.charAt(index)) || json.charAt(index) == '.')) {
            index++;
        }
        String number = json.substring(start, index);
        if (number.contains(".")) {
            return Double.parseDouble(number);
        } else {
            return Integer.parseInt(number);
        }
    }
}