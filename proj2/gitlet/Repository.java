package gitlet;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.*;

import static gitlet.Utils.*;

/**
 * Represents a gitlet repository.
 *
 * @author zdkk
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /**
     * |--.gitlet
     *      |--objects
     *          |commit and blot
     *      |--refs
     *          |--heads
     *              |master
     *              |test
     *      |HEAD
     *      |add_stage
     *      |remove_stage
     */

    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    public static final File ADD_STAGE_FILE = join(GITLET_DIR, "add_stage");
    public static final File REMOVE_STAGE_FILE = join(GITLET_DIR, "remove_stage");

    private static Commit currCommit;

    private static Stage addStage;
    private static Stage removeStage;


    /**
     * Creates a new Gitlet version-control system in the current directory. 
     * This system will automatically start with one commit: a commit that contains no 
     * files and has the commit message initial commit (just like that, with no 
     * punctuation). It will have a single branch: master, which initially points 
     * to this initial commit, and master will be the current branch. The timestamp 
     * for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970 
     * in whatever format you choose for dates. Since the initial commit in all
     * repositories created by Gitlet will have exactly the same content, it follows 
     * that all repositories will automatically share this commit (they will all have 
     * the same UID) and all commits in all repositories will trace back to it.
     * 
     * Note: If there is already a Gitlet version-control system in the current directory,
     *  it should abort. It should NOT overwrite the existing system with a new one. 
     * Should print the error message:
     * "A Gitlet version-control system already exists in the current directory."
     */
    public static void init() {
        if (GITLET_DIR.exists()) {
            String s = "A Gitlet version-control system already exists in the current directory.";
            System.out.println(s);
            System.exit(0);
        }
        mkdir(GITLET_DIR);
        mkdir(OBJECTS_DIR);
        mkdir(REFS_DIR);
        mkdir(HEADS_DIR);

        initCommit();
        initHEAD();
        initHeads();
    }

    /**
     * Adds a copy of the file as it currently exists to the staging area (see the 
     * description of the commit command). For this reason, adding a file is also called 
     * staging the file for addition. Staging an already-staged file overwrites the 
     * previous entry in the staging area with the new contents. The staging area should 
     * be somewhere in .gitlet. If the current working version of the file is identical 
     * to the version in the current commit, do not stage it to be added, and remove it 
     * from the staging area if it is already there (as can happen when a file is changed,
     *  added, and then changed back to it’s original version). The file will no longer be
     *  staged for removal (see gitlet rm), if it was at the time of the command.
     * 
     * Note: If the file does not exist, print the error message "File does not exist." 
     * and exit without changing anything.
     * @param name: the file name to add
     */
    public static void add(String name) {
        checkInit();

        name = unifiedFileName(name);
        // find the file in workspace
        File file = getFileFromCWD(name);
        // check if file:"name" exists
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        name = file.getName();
        // update the file in stage
        Blob blob = new Blob(name);
        storeBlob(blob);
    }

    /**
     * @param message: log message
     */
    public static void commit(String message) {
        checkInit();
        if (message.length() == 0) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        currCommit = readCurrCommit();
        addStage = readAddStage();
        removeStage = readRemoveStage();
        if (addStage.isEmpty() && removeStage.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        currCommit = getNewCommit(message);
        updateCurrBranch();
        addStage.clear();
        removeStage.clear();
        save(ADD_STAGE_FILE, addStage);
        save(REMOVE_STAGE_FILE, removeStage);
    }

    /**
     * Note:If the file is neither staged nor tracked by the head commit, print the error message
     * "No reason to remove the file."
     * @param name
     */
    public static void rm(String name) {
        checkInit();

        name = unifiedFileName(name);
        File file = getFileFromCWD(name);
        currCommit = readCurrCommit();
        addStage = readAddStage();
        removeStage = readRemoveStage();

        if (!addStage.contains(name) && !currCommit.containsBlob(name)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

        if (addStage.contains(name)) {
            addStage.removeBlob(name);
        }

        if (currCommit.containsBlob(name)) {
            removeStage.addBlob(name, currCommit.getSha1ByName(name));
            if (file.exists()) {
                file.delete();
            }
        }
        save(ADD_STAGE_FILE, addStage);
        save(REMOVE_STAGE_FILE, removeStage);
    }

    public static void log() {
        checkInit();

        Commit commit = readCurrCommit();
        while (commit != null) {
            System.out.println(commit.toString());
            commit = commit.findParent();
        }
    }

    public static void globalLog() {
        checkInit();

        List<String> files = plainFilenamesIn(OBJECTS_DIR);
        for (String s : files) {
            File file = Utils.join(OBJECTS_DIR, s);
            Serializable serializable = Utils.readObject(file, Serializable.class);
            if (serializable instanceof Commit) {
                Commit commit = (Commit) (serializable);
                System.out.println(commit.toString());
            }
        }
    }

    /**
     * Note:
     * If no such commit exists, prints the error message "Found no commit with that message."
     */
    public static void find(String message) {
        checkInit();
        List<String> files = plainFilenamesIn(OBJECTS_DIR);
        boolean flag = false;
        for (String s : files) {
            File file = Utils.join(OBJECTS_DIR, s);
            Serializable serializable = Utils.readObject(file, Serializable.class);
            if (serializable instanceof Commit) {
                Commit commit = (Commit) (serializable);
                if (commit.getMessage().equals(message)) {
                    System.out.println(commit.getSha1());
                    flag = true;
                }
            }
        }

        if (!flag) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void status() {
        checkInit();

        addStage = readAddStage();
        removeStage = readRemoveStage();
        String currBranch = readCurrBranch();
        List<String> branch = Utils.plainFilenamesIn(HEADS_DIR);
        Collections.sort(branch);
        System.out.println("=== Branches ===");
        for (String s : branch) {
            if (s.equals(currBranch)) {
                System.out.println("*" + s);
            } else {
                System.out.println(s);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        List<String> addStageFiles = new ArrayList<>(addStage.getBlobs().keySet());
        Collections.sort(addStageFiles);
        for (String s : addStageFiles) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        List<String> removeStageFiles = new ArrayList<>(removeStage.getBlobs().keySet());
        Collections.sort(removeStageFiles);
        for (String s : removeStageFiles) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String s : getModifiedFile()) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String s : getUntrackedFile()) {
            System.out.println(s);
        }
        System.out.println();
    }

    private static List<String> getModifiedFile() {
        currCommit = readCurrCommit();
        addStage = readAddStage();
        removeStage = readRemoveStage();
        Map<String, String> map = new HashMap<>();

        for (String fileName : currCommit.getBlobs().keySet()) {
            File file = Utils.join(CWD, fileName);
            if (!addStage.getBlobs().containsKey(fileName)) {
                if (file.exists()) {
                    Blob blob = new Blob(fileName);
                    if (!blob.getSha1().equals(currCommit.getBlobs().get(fileName))) {
                        map.put(fileName, "(modified)");
                    }
                }
            }
            if (!removeStage.getBlobs().containsKey(fileName)) {
                if (!file.exists()) {
                    map.put(fileName, "(deleted)");
                }
            }
        }

        for (String fileName : addStage.getBlobs().keySet()) {
            File file = Utils.join(CWD, fileName);
            if (file.exists()) {
                Blob blob = new Blob(fileName);
                if (!blob.getSha1().equals(addStage.getBlobs().get(fileName))) {
                    map.put(fileName, "(modified)");
                }
            } else {
                map.put(fileName, "(deleted)");
            }
        }

        List<String> res = new ArrayList<>();
        for (String s : map.keySet()) {
            res.add(s + " " + map.get(s));
        }
        return res;
    }

    private static List<String> getUntrackedFile() {
        currCommit = readCurrCommit();
        addStage = readAddStage();
        removeStage = readRemoveStage();

        List<String> res = new ArrayList<>();
        for (String fileName : Utils.plainFilenamesIn(CWD)) {
            if (!addStage.getBlobs().containsKey(fileName)
                    && !currCommit.getBlobs().containsKey(fileName)) {
                res.add(fileName);
            } else if (removeStage.getBlobs().containsKey(fileName)) {
                res.add(fileName);
            }
        }
        return res;
    }

    public static void checkout(String branchName) {
        checkInit();
        File branchFile = Utils.join(HEADS_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        String currBranchName = readCurrBranch();
        if (branchName.equals(currBranchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        Commit targetCommit = readCommitBySha1(Utils.readContentsAsString(branchFile));
        currCommit = readCurrCommit();

        boolean flag = checkUntrackedFileExists(currCommit, targetCommit);
        if (!flag) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            System.exit(0);
        }

        checkoutModifyWorkspace(targetCommit, currCommit);
        clearStage();
        currCommit = targetCommit;
        updateHEAD(branchName);
    }

    public static void checkout(String mark, String fileName) {
        if (!mark.equals("--")) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        checkInit();
        fileName = unifiedFileName(fileName);
        currCommit = readCurrCommit();
        if (!currCommit.containsBlob(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        File targetFile = Utils.join(CWD, fileName);
        File originFile = Utils.join(OBJECTS_DIR, currCommit.getBlobs().get(fileName));

        Blob blob = Utils.readObject(originFile, Blob.class);
        Utils.writeContents(targetFile, blob.getBytes());
    }

    public static void checkout(String commitID, String mark, String fileName) {
        if (!mark.equals("--")) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        checkInit();
        fileName = unifiedFileName(fileName);

        String res = readFullCommitSha1(commitID);
        if (res == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        Commit commit = readCommitBySha1(res);
        if (!commit.containsBlob(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        File targetFile = Utils.join(CWD, fileName);
        File originFile = Utils.join(OBJECTS_DIR, commit.getBlobs().get(fileName));
        Utils.writeContents(targetFile, Utils.readObject(originFile, Blob.class).getBytes());
    }

    public static void branch(String branchName) {
        checkInit();
        File branchFile = Utils.join(HEADS_DIR, branchName);
        if (branchFile.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        currCommit = readCurrCommit();
        Utils.writeContents(branchFile, currCommit.getSha1());
    }

    public static void rmBranch(String branchName) {
        checkInit();
        File branchFile = Utils.join(HEADS_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        String currBranchName = readCurrBranch();
        if (branchFile.getName().equals(currBranchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        branchFile.delete();
    }

    public static void reset(String commitID) {
        checkInit();
        commitID = readFullCommitSha1(commitID);
        if (commitID == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit targetCommit = readCommitBySha1(commitID);
        currCommit = readCurrCommit();

        boolean flag = checkUntrackedFileExists(currCommit, targetCommit);
        if (!flag) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            System.exit(0);
        }

        checkoutModifyWorkspace(targetCommit, currCommit);
        clearStage();
        currCommit = targetCommit;
        updateCurrBranch();
    }

    public static void merge(String branchName) {
        checkInit();
        addStage = readAddStage();
        removeStage = readRemoveStage();
        if (!addStage.isEmpty() || !removeStage.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        File branchFile = Utils.join(HEADS_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchFile.getName().equals(readCurrBranch())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        currCommit = readCurrCommit();
        Commit commit = readCommitBySha1(Utils.readContentsAsString(branchFile));
        Commit ancestor = findAncestor(currCommit, commit);

        if (ancestor.getSha1().equals(commit.getSha1())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (ancestor.getSha1().equals(currCommit.getSha1())) {
            System.out.println("Current branch fast-forwarded.");
        }
        boolean flag = dealMerge(commit, currCommit, ancestor, branchName);
        if (!flag) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            System.exit(0);
        }
    }

    private static void changeWorkSpaceFile(String fileName, String sha1) {
        File file = Utils.join(CWD, fileName);
        String s = readBlobContentBySha1(sha1);
        Utils.writeContents(file, s);
    }

    private static boolean subDealMerge(Commit a, Commit b, Commit c,
                                        Map<String, String> map, Set<String> set, boolean[] bool) {
        boolean flag = false;
        for (String fileName : c.getBlobs().keySet()) {
            String sha1 = c.getBlobs().get(fileName);
            String commitSha1 = a.getBlobs().getOrDefault(fileName, null);
            String currCommitSha1 = b.getBlobs().getOrDefault(fileName, null);
            if (currCommitSha1 != null && sha1.equals(commitSha1)
                    && !sha1.equals(currCommitSha1)) {
                map.put(fileName, currCommitSha1);
            } else if (commitSha1 != null && !sha1.equals(commitSha1)
                    && sha1.equals(currCommitSha1)) {
                map.put(fileName, commitSha1);
                addStage.getBlobs().put(fileName, commitSha1);
            } else if (commitSha1 != null && commitSha1.equals(currCommitSha1)) {
                map.put(fileName, currCommitSha1);
            } else if (commitSha1 == null && currCommitSha1 == null) {
                continue;
            } else if (commitSha1 == null && sha1.equals(currCommitSha1)) {
                set.add(fileName);
                removeStage.getBlobs().put(fileName, currCommitSha1);
            } else if (currCommitSha1 == null && sha1.equals(commitSha1)) {
                continue;
            } else {
                String s1 = currCommitSha1 == null ? "" : readBlobContentBySha1(currCommitSha1);
                String s2 = commitSha1 == null ? "" : readBlobContentBySha1(commitSha1);
                Blob blob = new Blob(fileName, generateBlobByMerge(s1, s2));
                File file = join(CWD, fileName);
                if (file.exists() && currCommitSha1 == null
                        && !blob.getSha1().equals(commitSha1)) {
                    return false;
                }
                blob.save();
                map.put(fileName, blob.getSha1());
                addStage.getBlobs().put(fileName, blob.getSha1());
                flag = true;
            }
        }

        for (String fileName : a.getBlobs().keySet()) {
            String sha1 = c.getBlobs().getOrDefault(fileName, null);
            if (sha1 != null) {
                continue;
            }
            String commitSha1 = a.getBlobs().getOrDefault(fileName, null);
            String currCommitSha1 = b.getBlobs().getOrDefault(fileName, null);
            if (currCommitSha1 == null) {
                File file = join(CWD, fileName);
                if (file.exists() && !new Blob(fileName).getSha1().equals(commitSha1)) {
                    return false;
                }
                map.put(fileName, commitSha1);
                addStage.getBlobs().put(fileName, commitSha1);
            } else {
                if (commitSha1.equals(currCommitSha1)) {
                    map.put(fileName, currCommitSha1);
                } else {
                    Blob blob = new Blob(fileName,
                            generateBlobByMerge(readBlobContentBySha1(currCommitSha1),
                                    readBlobContentBySha1(commitSha1)));
                    blob.save();
                    map.put(fileName, blob.getSha1());
                    addStage.getBlobs().put(fileName, blob.getSha1());
                    flag = true;
                }
            }
        }
        bool[0] = flag;
        return true;
    }
    private static boolean dealMerge(Commit a, Commit b, Commit c, String ss) {
        Map<String, String> map = new HashMap<>();
        Set<String> set = new HashSet<>();
        boolean[] bool = new boolean[1];

        if (!subDealMerge(a, b, c, map, set, bool)) {
            return false;
        }
        boolean flag = bool[0];
        if (flag) {
            System.out.println("Encountered a merge conflict.");
        }
        for (String fileName : map.keySet()) {
            String sha1 = map.get(fileName);
            String s = readBlobContentBySha1(sha1);
            writeContents(join(CWD, fileName), s);
        }
        for (String fileName : set) {
            File file = join(CWD, fileName);
            if (file.exists()) {
                file.delete();
            }
        }
        String message = String.format("Merged %s into %s.", ss, readCurrBranch());
        if (addStage.getBlobs().isEmpty() && removeStage.getBlobs().isEmpty()) {
            System.out.println("No changes added to the commit.");
        }
        List<String> parents = new ArrayList<>();
        parents.add(b.getSha1());
        parents.add(a.getSha1());
        Map<String, String> blobs = new HashMap<>(currCommit.getBlobs());
        blobs.putAll(addStage.getBlobs());
        for (Map.Entry<String, String> entry : removeStage.getBlobs().entrySet()) {
            blobs.remove(entry.getKey());
        }
        Commit res = new Commit(message, blobs, parents);
        res.save();
        currCommit = res;
        updateCurrBranch();
        return true;
    }

    private static String generateBlobByMerge(String curr, String target) {
        return "<<<<<<< HEAD\n" + curr + "=======\n" + target + ">>>>>>>\n";
    }


    private static Commit findAncestor(Commit a, Commit b) {
        Set<String> aParents = getParents(a);
        Set<String> bParents = getParents(b);
        String res = null;
        for (String s : aParents) {
            if (bParents.contains(s)) {
                if (res == null) {
                    res = s;
                } else {
                    File resFile = Utils.join(OBJECTS_DIR, res);
                    File file = Utils.join(OBJECTS_DIR, s);
                    if (resFile.lastModified() < file.lastModified()) {
                        res = s;
                    }
                }
            }
        }
        return readCommitBySha1(res);
    }

    private static Set<String> getParents(Commit a) {
        Set<String> set = new HashSet<>();
        Queue<String> q = new LinkedList<>();
        q.offer(a.getSha1());
        while (!q.isEmpty()) {
            String sha1 = q.poll();
            set.add(sha1);
            Commit commit = readCommitBySha1(sha1);
            for (String s : commit.getParents()) {
                q.offer(s);
            }
        }
        return set;
    }

    private static boolean checkUntrackedFileExists(Commit curr, Commit targetCommit) {
        boolean flag = true;
        List<String> targetFileNames = new ArrayList<>(targetCommit.getBlobs().keySet());
        Set<String> fileNames = new HashSet<>(curr.getBlobs().keySet());
        for (String s : targetFileNames) {
            if (fileNames.contains(s)) {
                continue;
            } else {
                File file = getFileFromCWD(s);
                if (!file.exists()) {
                    continue;
                }
                Blob blob = new Blob(s);
                if (blob.getSha1() != targetCommit.getBlobs().get(s)) {
                    flag = false;
                    break;
                }
            }
        }
        return flag;
    }

    private static String readFullCommitSha1(String commitID) {
        if (commitID.length() > Commit.SHA1_LENGTH) {
            return null;
        }
        List<String> files = plainFilenamesIn(OBJECTS_DIR);
        String res = null;
        int len = commitID.length();
        for (String s : files) {
            if (commitID.equals(s.substring(0, len))) {
                Serializable serializable = Utils.readObject(Utils.join(OBJECTS_DIR, s),
                        Serializable.class);
                if (serializable instanceof Commit) {
                    res = s;
                    break;
                }
            }
        }
        return res;
    }

    private static void clearStage() {
        addStage = new Stage();
        removeStage = new Stage();
        save(ADD_STAGE_FILE, addStage);
        save(REMOVE_STAGE_FILE, removeStage);
    }
    private static void checkoutModifyWorkspace(Commit targetCommit, Commit curr) {
        Map<String, String> targetBlobs = targetCommit.getBlobs();
        Map<String, String> currBlobs = curr.getBlobs();
        for (String s : targetBlobs.keySet()) {
            File file = Utils.join(CWD, s);
            String content = readBlobContentBySha1(targetBlobs.get(s));
            Utils.writeContents(file, content);
        }
        for (String s : currBlobs.keySet()) {
            if (!targetBlobs.containsKey(s)) {
                File file = Utils.join(CWD, s);
                file.delete();
            }
        }
    }


    private static String readBlobContentBySha1(String name) {
        File file = Utils.join(OBJECTS_DIR, name);
        Blob blob = readObject(file, Blob.class);
        return blob.getBytes();
    }

    private  static Commit readCommitBySha1(String sha1) {
        File file = Utils.join(OBJECTS_DIR, sha1);
        return Utils.readObject(file, Commit.class);
    }

    private static Commit getNewCommit(String message) {
        Map<String, String> blobs = new HashMap<>(currCommit.getBlobs());
        List<String> parents = new ArrayList<>();
        parents.add(currCommit.getSha1());
        for (Map.Entry<String, String> entry : addStage.getBlobs().entrySet()) {
            blobs.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : removeStage.getBlobs().entrySet()) {
            blobs.remove(entry.getKey());
        }
        Commit commit = new Commit(message, blobs, parents);
        commit.save();
        return commit;
    }

    private static void updateHEAD(String branchName) {
        Utils.writeContents(HEAD_FILE, branchName);
    }
    private static void updateCurrBranch() {
        String currBranch = readCurrBranch();
        File currBranchFile = Utils.join(HEADS_DIR, currBranch);
        Utils.writeContents(currBranchFile, currCommit.getSha1());
    }

    private static void storeBlob(Blob blob) {
        currCommit = readCurrCommit();
        addStage = readAddStage();
        removeStage = readRemoveStage();

        blob.save();
        addStage.addBlob(blob);
        if (removeStage.contains(blob)) {
            removeStage.removeBlob(blob);
            save(REMOVE_STAGE_FILE, removeStage);
        }
        if (currCommit.containsBlob(blob)) {
            addStage.removeBlob(blob);
        }
        save(ADD_STAGE_FILE, addStage);
    }

    private static void save(File file, Serializable serializable) {
        Utils.writeObject(file, serializable);
    }


    private static Commit readCurrCommit() {
        String currCommitSha1 = readCurrCommitSha1();
        File currCommitFile = Utils.join(OBJECTS_DIR, currCommitSha1);
        return Utils.readObject(currCommitFile, Commit.class);
    }

    private static String readCurrCommitSha1() {
        String currBranch = readCurrBranch();
        File curBranchFile = Utils.join(HEADS_DIR, currBranch);
        return Utils.readContentsAsString(curBranchFile);
    }

    private static String readCurrBranch() {
        return Utils.readContentsAsString(HEAD_FILE);
    }

    private static Stage readAddStage() {
        if (!ADD_STAGE_FILE.exists()) {
            return new Stage();
        }
        return readObject(ADD_STAGE_FILE, Stage.class);
    }

    private static Stage readRemoveStage() {
        if (!REMOVE_STAGE_FILE.exists()) {
            return new Stage();
        }
        return readObject(REMOVE_STAGE_FILE, Stage.class);
    }

    private static void mkdir(File file) {
        if (!file.mkdir()) {
            throw new IllegalArgumentException(String.format("mkdir: %s \nFailed to create!",
                    file.getPath()));
        }
    }

    private static void initCommit() {
        Commit initCommit = new Commit();
        currCommit = initCommit;
        initCommit.save();
    }

    private static void initHEAD() {
        Utils.writeContents(HEAD_FILE, "master");
    }

    private static void initHeads() {
        File masterFile = join(HEADS_DIR, "master");
        Utils.writeContents(masterFile, currCommit.getSha1());
    }

    private static String  unifiedFileName(String name) {
        File file = getFileFromCWD(name);
        return file.getName();
    }

    private static File getFileFromCWD(String file) {
        return Paths.get(file).isAbsolute() ? new File(file) : join(CWD, file);
    }

    /**
     * used for every command except init
     */
    private static void checkInit() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    public static void debug() {
        currCommit = readCurrCommit();
        addStage = readAddStage();
        removeStage = readRemoveStage();
        System.out.println(addStage.getBlobs());
        System.out.println(removeStage.getBlobs());
        System.out.println();
        System.out.println(currCommit.toString());
    }
}
