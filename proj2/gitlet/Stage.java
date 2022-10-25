package gitlet;

import java.io.Serializable;
import java.io.File;
import java.util.*;

public class Stage implements Serializable {

    // filename-sha1
    private Map<String, String> blobs = new HashMap<>();


    public static Blob getBlobBySha1(String sha1) {
        File file = Utils.join(Repository.OBJECTS_DIR, sha1);
        return Utils.readObject(file, Blob.class);
    }

    public Map<String, String> getBlobs() {
        return blobs;
    }

    public boolean isEmpty() {
        return blobs.isEmpty();
    }

    public void clear() {
        blobs.clear();
    }


    public void addBlob(Blob blob) {
        blobs.put(blob.getFileName(), blob.getSha1());
    }

    public void addBlob(String filename, String sha1) {
        blobs.put(filename, sha1);
    }

    public boolean contains(Blob blob) {
        return blobs.containsKey(blob.getFileName());
    }

    public boolean contains(String filename) {
        return blobs.containsKey(filename);
    }

    public void removeBlob(Blob blob) {
        blobs.remove(blob.getFileName());
    }

    public void removeBlob(String filename) {
        blobs.remove(filename);
    }
}
