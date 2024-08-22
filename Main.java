
/**
 * Assumption 1 : Maximum euclidean distance is less than 100.
 * 
 */

import java.util.*;
import java.io.*;

public class Main {

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

    public static void main(String[] args) {
        JSONParser input = new JSONParser(readJsonFileToString("input.json"));
        List<School> schools = new ArrayList<>();
        List<Student> students = new ArrayList<>();
        
        JSONObject test = input.parseObject();
        System.out.println(test.get("students").getClass());
        JSONArray studs = (JSONArray) test.get("students");
        //System.out.println(studs.get(0).getClass());
        for(int i = 0; i < studs.size(); i++){
             
            Student tempStudent = new Student((JSONObject) studs.get(i));
            students.add(tempStudent);
        }
        for(int i = 0; i < studs.size(); i++){
             
            School tempschool = new School((JSONObject) studs.get(i));
            schools.add(tempschool);
        }


       
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

    public School getSchool() {
        return school;
    }

    public double getWeight() {
        return weight;
    }

    


}

class Student {
    int id;
    int[] homeLocation;
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
        if (input.get("homeLocation") instanceof int[] k) {
            homeLocation = k;
        }
        alumni = (String) input.getOrDefault("alumni", null);
        volunteer = (String) input.getOrDefault("volunteer", null);
    }

}

class School {
    String name;
    int[] location;
    int maxAllocation;

    public School(String name, int[] location, int maxAllocation) {
        this.name = name;
        this.location = location;
        this.maxAllocation = maxAllocation;
    }

    public School(JSONObject input) {
        name = (String) input.get("name");
        maxAllocation = (int) input.get("maxAllocation");
        if (input.get("location") instanceof int[] k) {
            location = k;
        }
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