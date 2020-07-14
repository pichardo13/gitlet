package gitlet;

import java.nio.file.Files;
import java.util.TreeMap;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.TreeSet;

public class Reset {
    TreeSet<String> tracked = new TreeSet<>();
    public void reset(String iD) {
        File wd = new File(System.getProperty("user.dir"));
        File cPath = new File(System.getProperty("user.dir") + "/.gitlet/Commits");
        File saPath = new File(System.getProperty("user.dir") + "/.gitlet/Staging Area");
        File sPath = new File(System.getProperty("user.dir") + "/.gitlet/Serialized");
        int inputLength = iD.length();
        boolean commitExists = false;
        TreeMap<String, String> pointers;
        try {
            ObjectInputStream inp = new ObjectInputStream(
                    new FileInputStream(sPath + "/pointers.txt"));
            pointers = (TreeMap) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            pointers = new TreeMap<>();
        }
        String fullID = new String();
        for (File commit : cPath.listFiles()) {
            String commitID = commit.getName().substring(0, inputLength);
            if (iD.equals(commitID)) {
                commitExists = true;
                fullID = commit.getName();
                break;
            }
        }
        if (!commitExists) {
            System.out.println("No commit with that id exists.");
            return;
        }
        for (File f: saPath.listFiles()) {
            f.delete();
        }
        String currentBranch = pointers.get(pointers.get("HEAD"));
        File cbPath = new File(cPath + "/" + currentBranch);
        File gPath = new File(cPath + "/" + fullID);
        for (File f : wd.listFiles()) {
            if (new File(cPath + "/" + fullID + "/" + f.getName()).exists()
                    && !(new File(cbPath + "/" + f.getName())).exists()) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it or add it first.");
                return;
            }
        }
        if (cbPath.listFiles().length > 0) {
            for (File f : cbPath.listFiles()) {
                if (!(f.getName().equals("logMessage.txt"))
                        && !(f.getName().equals("timeStamp.txt"))
                        && !(f.getName().equals("parentHash.txt"))) {
                    (new File(wd + "/" + f.getName())).delete();
                }
            }
        }
        if (gPath.listFiles().length > 0) {
            for (File f : gPath.listFiles()) {
                if (!(f.getName().equals("logMessage.txt"))
                        && !(f.getName().equals("timeStamp.txt"))
                        && !(f.getName().equals("parentHash.txt"))) {
                    File wdVersion = new File(wd + "/" + f.getName());
                    if (wdVersion.exists()) {
                        wdVersion.delete();
                    }
                    try {
                        Files.copy(f.toPath(), (new File(wd + "/" + f.getName())).toPath());
                    } catch (IOException e) {
                        System.out.println("Failed to copy");
                    }
                }
            }
        }
        pointers.put(pointers.get("HEAD"), fullID);
        File outFile = new File(sPath + "/pointers.txt");
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(pointers);
            out.close();
        } catch (IOException excp) {
            System.out.print("Map serialization failed.");
        }
    }

}
