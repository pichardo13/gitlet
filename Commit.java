package gitlet;

import java.io.Serializable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
import java.util.TreeSet;
import java.nio.file.Files;

public class Commit implements Serializable {
    static File cPath = new File(System.getProperty("user.dir") + "/.gitlet/Commits");
    static File sPath = new File(System.getProperty("user.dir") + "/.gitlet/Serialized");
    static File saPath = new File(System.getProperty("user.dir") + "/.gitlet/Staging Area");
    static File rPath = new File(System.getProperty("user.dir") + "/.gitlet/Removed Files");
    File prevFiles;
    String logMessage;
    String timeStamp;
    String parentHash;
    String commitHash;
    TreeSet<String> stageSet = new TreeSet<>();
    TreeMap<String, String> pointers;
    TreeSet<String> removedMark = new TreeSet<>();
    TreeSet<String> tracked = new TreeSet<>();

    /*Write method that converts a Commit
     to a string to be converted to a SHA-1 Hash
     */

    @Override
    public String toString() {
        return "Commit{"
                + "logMessage='" + logMessage + '\''
                + ", timeStamp='" + timeStamp + '\''
                + ", parentHash='" + parentHash
                + '\'' + '}';
    }

    /* Construct a commit using a log message and the
    serialized file that refers to its parent commit. */
    public Commit(String msg, boolean initial) {
        this.logMessage = msg;

        DateFormat df = new SimpleDateFormat(("yyyy-MM-dd HH:mm:ss"));
        this.timeStamp = df.format(new Date());

        try {
            ObjectInputStream inp = new ObjectInputStream(
                    new FileInputStream(sPath + "/pointers.txt"));
            this.pointers = (TreeMap) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            this.pointers = new TreeMap<>();
        }

        if (pointers.containsKey("HEAD")) {
            this.parentHash = pointers.get(pointers.get("HEAD"));
        }

        try {
            ObjectInputStream inp = new ObjectInputStream(
                    new FileInputStream(sPath + "/tracked.txt"));
            this.tracked = (TreeSet) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            this.tracked = new TreeSet<>();
        }

        try {
            ObjectInputStream inp = new ObjectInputStream(
                    new FileInputStream(sPath + "/removedMark" + pointers.get("HEAD") + ".txt"));
            this.removedMark = (TreeSet) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            this.removedMark = new TreeSet<>();
        }

        if (!initial) {
            this.prevFiles = (new File(cPath + "/" + this.parentHash));
        }

        if (saPath.listFiles().length != 0) {
            for (File f : saPath.listFiles()) {
                stageSet.add(f.getName());
            }
        }


    }

    public void commit(boolean initial) throws IOException {
        if (stageSet.isEmpty() && !initial && rPath.listFiles().length == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }

        if (rPath.listFiles().length > 0) {
            for (File f : rPath.listFiles()) {
                f.delete();
            }
        }

        if (logMessage == null || logMessage.isEmpty() || logMessage.equals("")) {
            System.out.println("Please enter a commit message.");
        }

        commitHash = Utils.sha1(this.toString());
        File newFolder = new File(cPath, commitHash);
        newFolder.mkdir();
        File lMFile = new File(cPath + "/" + commitHash + "/logMessage.txt");
        Utils.writeContents(lMFile, logMessage.getBytes());
        File tSFile = new File(cPath + "/" + commitHash + "/timeStamp.txt");
        Utils.writeContents(tSFile, timeStamp.getBytes());

        if (parentHash != null) {
            File pHFile = new File(cPath + "/" + commitHash + "/parentHash.txt");
            Utils.writeContents(pHFile, parentHash.getBytes());
        }

        if (prevFiles != null) {
            for (File f : prevFiles.listFiles()) {
                if (!stageSet.contains(f.getName())
                        && !(f.getName().equals("logMessage.txt"))
                        && !(f.getName().equals("timeStamp.txt"))
                        && !(f.getName().equals("parentHash.txt"))
                        && !(removedMark.contains(f.getName())))   {
                    Files.copy(f.toPath(), Paths.get(cPath + "/"
                            + commitHash + "/" + f.getName()));
                }
            }
        }

        if (!stageSet.isEmpty()) {
            for (File f : saPath.listFiles()) {
                f.renameTo(new File(cPath + "/" + commitHash + "/" + f.getName()));
                tracked.add(f.getName());
            }
        }

        if (!pointers.isEmpty()) {
            this.parentHash = pointers.get(pointers.get("HEAD"));
        }

        if (parentHash == null) {
            pointers.put("HEAD", "master");
            pointers.put("master", commitHash);
        } else {
            pointers.put(pointers.get("HEAD"), commitHash);
        }

        File pointerFile = new File(sPath + "/pointers.txt");
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(pointerFile));
            out.writeObject(pointers);
            out.close();
        } catch (IOException excp) {
            System.out.print("Map serialization failed.");
        }

        File trackerFile = new File(sPath + "/tracked.txt");
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(trackerFile));
            out.writeObject(tracked);
            out.close();
        } catch (IOException excp) {
            System.out.print("Map serialization failed.");
        }
    }

}




