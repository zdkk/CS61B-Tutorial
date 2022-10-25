package gitlet;

import java.io.Serializable;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Represents a gitlet commit object.
 *
 * @author zdkk
 */
public class Commit implements Serializable {

    /** The message of this Commit. */
    private String message;
    // filename-sha1
    private Map<String, String> blobs;
    // parents nodes' sha1
    private List<String> parents;
    // init time
    private String timestamp;
    private String sha1;

    public Commit(String message, Map<String, String> blobs, List<String> parents) {
        this.message = message;
        this.blobs = blobs;
        this.parents = parents;
        this.timestamp = calcTimestamp(new Date());
        this.sha1 = generatedSha1();
    }

    public Commit() {
        this.message = "initial commit";
        this.blobs = new HashMap<>();
        this.parents = new ArrayList<>();
        this.timestamp = calcTimestamp(new Date(0));
        this.sha1 = generatedSha1();
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getBlobs() {
        return blobs;
    }

    public List<String> getParents() {
        return parents;
    }

    public String getSha1() {
        return sha1;
    }


    public void save() {
        File saveFile = Utils.join(Repository.OBJECTS_DIR, sha1);
        Utils.writeObject(saveFile, this);
    }

    public boolean containsBlob(Blob blob) {
        return blob.getSha1().equals(blobs.getOrDefault(blob.getFileName(), null));
    }

    public boolean containsBlob(String filename) {
        return blobs.containsKey(filename);
    }

    public String getSha1ByName(String filename) {
        return blobs.getOrDefault(filename, null);
    }

    public Commit findParent() {
        if (parents.isEmpty()) return null;
        File parentFile = Utils.join(Repository.OBJECTS_DIR, parents.get(0));
        return Utils.readObject(parentFile, this.getClass());
    }

    @Override
    public String toString() {
        return String.format("===\ncommit %s\nDate: %s\n%s\n", sha1, timestamp, message);
    }

    private String generatedSha1() {
        return Utils.sha1(this.message, this.timestamp, parents.toString(), blobs.toString());
    }

    private String calcTimestamp(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("E yyyy-MM-dd HH:mm:ss z");
        return sdf.format(date);
    }
}
