package gitlet;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;


public class Merge {
    public static String branchCommitString;
    public static String headCommitString;
    public static String splitPointString;
    public static File splitPointCommit;
    public static File branchCommit;
    public static File headCommit;
    public static HashSet<String> headModifiedFiles = new HashSet<>();
    public static HashSet<String> headUnmodifiedFiles = new HashSet<>();
    public static HashSet<String> headDeletedFiles = new HashSet<>();
    public static HashSet<String> branchModifiedFiles = new HashSet<>();
    public static HashSet<String> branchUnmodifiedFiles = new HashSet<>();
    public static HashSet<String> branchDeletedFiles = new HashSet<>();
    public static boolean conflicting = false;
    public static String head;


    public static void merge(String branchName) throws IOException {

        if (setUpVariables(branchName)) {
            return;
        }
        setCommitsUp(branchName);
        settingUpHashSets(branchName);

        if (splitPointString.equals(branchCommitString)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }

        //add files that have been deleted since split point to the modified & unmodified arrayLists.
        for (File file : splitPointCommit.listFiles()) {
            if (!new File(headCommit + "/" + file.getName()).exists()) {
                headDeletedFiles.add(file.getName());
            }
            if (!new File(branchCommit + "/" + file.getName()).exists()) {
                branchDeletedFiles.add(file.getName());
            }
        }

        for (String fileName : branchModifiedFiles) {
            if (headUnmodifiedFiles.contains(fileName)) {
                // change files in wd to given branch stuff
                new File(System.getProperty("user.dir") + "/" + fileName).delete();
                Files.copy(new File(branchCommit + "/" + fileName).toPath(),
                        new File(System.getProperty("user.dir") + "/" + fileName).toPath());
                // stage the file
                Files.copy(new File(branchCommit + "/" + fileName).toPath(),
                        new File(System.getProperty("user.dir") + "/.gitlet/Staging Area/" + fileName).toPath());
            }
        }

        //any files that have been modified in current but not given branch since split should stay same.
        //any files not present at split & present only in current branch should remain as they are.

        //any files not present at split point and present only in given branch should be checked out and staged.
        for (File file : branchCommit.listFiles()) {
            if (!new File(splitPointCommit + "/" + file.getName()).exists()
                    && !new File(headCommit + "/" + file.getName()).exists()) {
                new Checkout().checkout(branchCommitString, file.getName());
                Files.copy(new File(branchCommit + "/" + file.getName()).toPath(),
                        new File(System.getProperty("user.dir") + "/.gitlet/Staging Area/" +
                                file.getName()).toPath());
            }
        }

        //any files present at the split point, unmodified in the current branch,
        // and absent in the given branch should be removed (and untracked).

        for (File file : splitPointCommit.listFiles()) {
            if (headUnmodifiedFiles.contains(file.getName()) &&
                    !new File(branchCommit + "/" + file.getName()).exists()) {
                new File(System.getProperty("user.dir") + "/" + file.getName()).delete();
            }
        }

        //any files modified in different ways are in conflict.
        // first, loop through headCommit,
        for (String fileName : headModifiedFiles) {
            if (branchModifiedFiles.contains(fileName)) {
                String headHashID = Utils.sha1(new String(Utils.readContents(new
                        File(headCommit + "/" + fileName))));
                String branchHashID = Utils.sha1(new String(Utils.readContents(new
                        File(branchCommit + "/" + fileName))));
                if (!headHashID.equals(branchHashID)) {
                    conflicting = true;
                    File hFile = new File(headCommit + "/" + fileName);
                    File bFile = new File(branchCommit + "/" + fileName);
                    File wdFile = new File(System.getProperty("user.dir") + "/" + fileName);
                    String hFileContents = new String(Utils.readContents(hFile));
                    String bFileContents = new String(Utils.readContents(bFile));
                    FileOutputStream mergedFile = new FileOutputStream(wdFile, false);

                    mergedFile.write("<<<<<<< HEAD".getBytes()); mergedFile.write("\r\n".getBytes());
                    mergedFile.write(hFileContents.getBytes());
                    mergedFile.write("=======".getBytes()); mergedFile.write("\r\n".getBytes());
                    mergedFile.write(bFileContents.getBytes());
                    mergedFile.write(">>>>>>>".getBytes()); mergedFile.write("\r\n".getBytes());
                    mergedFile.close();

                }
            }
        }
        for (String fileName : headModifiedFiles) {
            if (branchDeletedFiles.contains(fileName)) {
                conflicting = true;
                File hFile = new File(headCommit + "/" + fileName);
                File wdFile = new File(System.getProperty("user.dir") + "/" + fileName);
                String hFileContents = new String(Utils.readContents(hFile));
                FileOutputStream mergedFile = new FileOutputStream(wdFile, false);

                mergedFile.write("<<<<<<< HEAD".getBytes()); mergedFile.write("\r\n".getBytes());
                mergedFile.write(hFileContents.getBytes());
                mergedFile.write("=======".getBytes()); mergedFile.write("\r\n".getBytes());
                mergedFile.write(">>>>>>>".getBytes()); mergedFile.write("\r\n".getBytes());
                mergedFile.close();
            }
        }
        for (String fileName : branchModifiedFiles) {
            if (headDeletedFiles.contains(fileName)) {
                conflicting = true;
                File bFile = new File(branchCommit + "/" + fileName);
                File wdFile = new File(System.getProperty("user.dir") + "/" + fileName);
                String bFileContents = new String(Utils.readContents(bFile));
                FileOutputStream mergedFile = new FileOutputStream(wdFile, false);
                mergedFile.write("<<<<<<< HEAD".getBytes()); mergedFile.write("\r\n".getBytes());
                mergedFile.write("=======".getBytes()); mergedFile.write("\r\n".getBytes());
                mergedFile.write(bFileContents.getBytes());
                mergedFile.write(">>>>>>>".getBytes()); mergedFile.write("\r\n".getBytes());
                mergedFile.close();
            }
        }

        if (conflicting) {
            System.out.println("Encountered a merge conflict.");
        } else {
            new Commit("Merged " + head + " with " + branchName + ".", false).commit(false);
        }

    }

    static void settingUpHashSets (String branchName){
        //add files to headCommit modified & unmodified files arraylists. (adds new files too)
        for (File file : headCommit.listFiles()) {
            if (!file.getName().equals("logMessage.txt")
                    && !file.getName().equals("timeStamp.txt")
                    && !file.getName().equals("parentHash.txt")) {
                if (new File(splitPointCommit + "/" + file.getName()).exists()) {
                    String headHashID = Utils.sha1(new String(Utils.readContents(file)));
                    String splitHashID = Utils.sha1(new
                            String(Utils.readContents(new File(splitPointCommit + "/" + file.getName()))));
                    if (headHashID.equals(splitHashID)) {
                        headUnmodifiedFiles.add(file.getName());
                    } else {
                        headModifiedFiles.add(file.getName());
                    }
                } else {
                    headModifiedFiles.add(file.getName());
                }
            }
        }

        //add files to branchCommit modified & unmodified files arrayLists. (adds new files too)
        for (File file : branchCommit.listFiles()) {
            if (!file.getName().equals("logMessage.txt")
                    && !file.getName().equals("timeStamp.txt")
                    && !file.getName().equals("parentHash.txt")) {
                if (new File(splitPointCommit + "/" + file.getName()).exists()) {
                    String branchHashID = Utils.sha1(new String(Utils.readContents(file)));
                    String splitHashID = Utils.sha1(new
                            String(Utils.readContents(new File(splitPointCommit + "/" + file.getName()))));
                    if (branchHashID.equals(splitHashID)) {
                        branchUnmodifiedFiles.add(file.getName());
                    } else {
                        branchModifiedFiles.add(file.getName());
                    }
                } else {
                    branchModifiedFiles.add(file.getName());
                }
            }
        }
    }

    static boolean setUpVariables(String branchName) {
        if (new File(System.getProperty("user.dir") + "/.gitlet/Staging Area").listFiles().length != 0) {
            System.out.println("You have uncommitted changes.");
            return true;
        }

        TreeMap<String, String> pointers = new TreeMap<>();

        String pointerPath = System.getProperty("user.dir") + "/.gitlet/Serialized/pointers.txt";
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(pointerPath));
            pointers = (TreeMap) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            pointers = new TreeMap<>();
        }

        head = pointers.get("HEAD");
        headCommitString = pointers.get(head);

        if (pointers.containsKey(branchName)) {
            branchCommitString = pointers.get(branchName);
        } else {
            System.out.println("A branch with that name does not exist.");
            return true;
        }

        if (branchName.equals(head)) {
            System.out.println("Cannot merge a branch with itself.");
            return true;
        }

        if (branchName.compareTo(head) > 0) {
            splitPointString = pointers.get(head + "/" + branchName);
        } else {
            splitPointString = pointers.get(branchName + "/" + head);
        }


       if (splitPointString.equals(headCommitString)) {
            pointers.replace(head, branchCommitString);
            System.out.println("Current branch fast-forwarded.");
            return true;
        }

        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(pointerPath));
            out.writeObject(pointers);
            out.close();
        } catch (IOException excp) {
            System.out.print("Map serialization failed.");
        }
        return false;
    }

    static void setCommitsUp(String branchName) {
        splitPointCommit = new File(System.getProperty("user.dir") + "/.gitlet/Commits/" + splitPointString);
        headCommit = new File(System.getProperty("user.dir") +
                "/.gitlet/Commits/" + headCommitString);
        branchCommit = new File(System.getProperty("user.dir") +
                "/.gitlet/Commits/" + branchCommitString);
    }
}
