package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataExporter {

    private List <String> list = new ArrayList<>();
    private  String fileName;
    private  boolean condition;

    public DataExporter() {

    }
    public DataExporter(List <String> list, String fileName, boolean condition) {
        this.fileName = fileName;
        this.condition = condition;
        this.list = list;

    }

    public void writeFile(List<String> list, String fileName, boolean condition){

        try(FileWriter writer = new FileWriter(fileName + ".txt", condition))
        {
            for(String s : list){
                writer.write(s);
                writer.append('\n');
            }
            writer.flush();
        }
        catch(IOException ex){

            System.out.println(ex.getMessage());
        }

    }

}
