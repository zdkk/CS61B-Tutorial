package gitlet;

import java.io.Serializable;
import java.io.File;

public class Blob implements Serializable {

    // original file name
    private String fileName;
    // file data
    private String bytes;
    // hash(file)
    private String sha1;


    public Blob(String fileName) {
        this.fileName = fileName;
        this.bytes = readFile(fileName);
        this.sha1 = generatedSha1();
    }

    public Blob(String fileName, String bytes) {
        this.fileName = fileName;
        this.bytes = bytes;
        this.sha1 = generatedSha1();
    }

    public String getFileName() {
        return fileName;
    }
    public String getSha1() {
        return sha1;
    }

    public String getBytes() {
        return bytes;
    }


    public void save() {
        File saveFile = Utils.join(Repository.OBJECTS_DIR, sha1);
        Utils.writeObject(saveFile, this);
    }

    @Override
    public String toString() {
        return fileName + " " + sha1 + "\n" + bytes;
    }


    private String readFile(String filename) {
        File file = Utils.join(Repository.CWD, fileName);
        return Utils.readContentsAsString(file);
    }

    private String generatedSha1() {
        return Utils.sha1(fileName, this.bytes.length() + "", this.bytes);
    }
}
