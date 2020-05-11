import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteData {
    private String fileName;
    private String fieldTerminator = ",";
    private String lineTerminator = "\n";

    private BufferedWriter out;

    // to see if this is the first field in the line
    // helps with adding field terminators
    private boolean firstField = true;

    public WriteData(String fileName){
        this.fileName = fileName;
        try {
            new File(fileName).createNewFile();
            out = new BufferedWriter(new FileWriter(fileName, false));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void addField(String field){
        try {
            String toWrite = "";
            if (!firstField) toWrite += ",";
            toWrite += field;
            out.write(toWrite);
            firstField = false;

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void newLine(){
        try {
            out.write(lineTerminator);

            firstField = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void close(){
        try {
            out.flush();
            out.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main (String[] args){
        WriteData test = new WriteData("stars.txt");
        test.addField("sup");
        test.addField("yo");
        test.close();
    }

    public String getFieldTerminator() {
        return fieldTerminator;
    }

    public String getLineTerminator() {
        return lineTerminator;
    }

    public String getFileName() {
        return fileName;
    }
}
