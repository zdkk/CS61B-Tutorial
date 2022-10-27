package gitlet;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.*;
import gitlet.GitletException;
import gitlet.Stage;
import gitlet.Commit;

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

    public static Commit currCommit;

    public static Stage addStage;
    public static Stage removeStage;


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
            System.out.println("A Gitlet version-control system already exists in the current directory.");
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
     * Saves a snapshot of tracked files in the current commit and staging area so they can be
     * restored at a later time, creating a new commit. The commit is said to be tracking the saved files.
     * By default, each commit’s snapshot of files will be exactly the same as its parent commit’s snapshot
     * of files; it will keep versions of files exactly as they are, and not update them. A commit will only
     * update the contents of files it is tracking that have been staged for addition at the time of commit,
     * in which case the commit will now include the version of the file that was staged instead of the version
     * it got from its parent. A commit will save and start tracking any files that were staged for addition but
     * weren’t tracked by its parent. Finally, files tracked in the current commit may be untracked in the new
     * commit as a result being staged for removal by the rm command (below).
     * Note: If stage is empty, print "No changes added to the commit."
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
     * Unstage the file if it is currently staged for addition. If the file is tracked in the current commit, stage it
     * for removal and remove the file from the working directory if the user has not already done so (do not remove it
     * unless it is tracked in the current commit).
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

    /**
     * Starting at the current head commit, display information about each commit backwards along the commit tree until
     * the initial commit, following the first parent commit links, ignoring any second parents found in merge commits.
     * (In regular Git, this is what you get with git log --first-parent). This set of commit nodes is called the
     * commit’s history. For every node in this history, the information it should display is the commit id, the time
     * the commit was made, and the commit message.
     */
    public static void log() {
        checkInit();

        Commit commit = readCurrCommit();
        while (commit != null) {
            System.out.println(commit.toString());
            commit = commit.findParent();
        }
    }

    /**
     * Like log, except displays information about all commits ever made. The order of the commits does not matter.
     * Hint: there is a useful method in gitlet.Utils that will help you iterate over files within a directory.
     */
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
     * Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such
     * commits, it prints the ids out on separate lines. The commit message is a single operand; to indicate a multiword
     * message, put the operand in quotation marks, as for the commit command below. Hint: the hint for this command is
     * the same as the one for global-log.
     * Note:If no such commit exists, prints the error message "Found no commit with that message."
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

    /**
     * Displays what branches currently exist, and marks the current branch with a *. Also displays what files have been
     * staged for addition or removal. An example of the exact format it should follow is as follows.
     * Note:There is an empty line between sections, and the entire status ends in an empty line as well.
     */
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
            } else System.out.println(s);
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
                    if (blob.getSha1() != currCommit.getBlobs().get(fileName)) {
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
                if (blob.getSha1() != addStage.getBlobs().get(fileName)) {
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
            if (!addStage.getBlobs().containsKey(fileName) && !currCommit.getBlobs().containsKey(fileName)) {
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
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        checkoutModifyWorkspace(targetCommit, currCommit);
        clearStage();
        currCommit = targetCommit;
        updateHEAD(branchName);
    }

    private static boolean checkUntrackedFileExists(Commit currCommit, Commit targetCommit) {
        boolean flag = true;
        List<String> targetFileNames = new ArrayList<>(targetCommit.getBlobs().keySet());
        Set<String> fileNames = new HashSet<>(currCommit.getBlobs().keySet());
        for (String s : targetFileNames) {
            if (fileNames.contains(s))
                continue;
            else {
                File file = getFileFromCWD(s);
                if (!file.exists()) continue;
                Blob blob = new Blob(s);
                if (blob.getSha1() != targetCommit.getBlobs().get(s)) {
                    flag = false;
                    break;
                }
            }
        }
        return flag;
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
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        checkoutModifyWorkspace(targetCommit, currCommit);
        clearStage();
        currCommit = targetCommit;
        updateCurrBranch();
    }

    private static String readFullCommitSha1(String commitID) {
        if (commitID.length() > Commit.SHA1_LENGTH) return null;
        List<String> files = plainFilenamesIn(OBJECTS_DIR);
        String res = null;
        int len = commitID.length();
        for (String s : files) {
            if (commitID.equals(s.substring(0, len))) {
                Serializable serializable = Utils.readObject(Utils.join(OBJECTS_DIR, s), Serializable.class);
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
    private static void checkoutModifyWorkspace(Commit targetCommit, Commit currCommit) {
        Map<String, String> targetBlobs = targetCommit.getBlobs();
        Map<String, String> currBlobs = currCommit.getBlobs();
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
        if (!ADD_STAGE_FILE.exists())
            return new Stage();
        return readObject(ADD_STAGE_FILE, Stage.class);
    }

    private static Stage readRemoveStage() {
        if (!REMOVE_STAGE_FILE.exists())
            return new Stage();
        return readObject(REMOVE_STAGE_FILE, Stage.class);
    }

    private static void mkdir(File file) {
        if (!file.mkdir())
            throw new IllegalArgumentException(String.format("mkdir: %s \nFailed to create!", file.getPath()));
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
