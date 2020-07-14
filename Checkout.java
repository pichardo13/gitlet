package gitlet;

import java.nio.file.Files;
import java.util.TreeMap;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Checkout {

    /* Takes version of file as it exists in the head commit
    (the front of the current branch), and puts it in the working
    directory, overwriting the version of the file that's already
    there if there is one. The new version of the file is not
    staged.
     */
    public void checkout(String name) {
        File wd = new File(System.getProperty("user.dir"));
        File cPath = new File(System.getProperty("user.dir") + "/.gitlet/Commits");
        File sPath = new File(System.getProperty("user.dir") + "/.gitlet/Serialized");

        TreeMap<String, String> pointers;

        try {
            ObjectInputStream inp = new ObjectInputStream(
                    new FileInputStream(sPath + "/pointers.txt"));
            pointers = (TreeMap) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            pointers = new TreeMap<>();
        }

        String currentHash = pointers.get(pointers.get("HEAD"));

        File toCheckout = new File(cPath + "/" + currentHash + "/" + name);

        if (!toCheckout.exists()) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        File wdVersion = new File(wd + "/" + toCheckout.getName());

        if (wdVersion.exists()) {
            wdVersion.delete();
        }

        try {
            Files.copy(toCheckout.toPath(), (new File(wd + "/" + name)).toPath());
        } catch (IOException e) {
            System.out.println("Failed to copy");
        }
    }

    /* Takes version of the file as it exists in the commit
    within the given id, and puts it in the working directory,
    overwriting the version of the file that's already there if
    there is one. The new version of the file is not staged.
     */
    public void checkout(String iD, String name) {
        File wd = new File(System.getProperty("user.dir"));
        File cPath = new File(System.getProperty("user.dir") + "/.gitlet/Commits");
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

        File toCheckout = new File(cPath + "/" + fullID + "/" + name);

        if (!toCheckout.exists()) {
            System.out.println("File does not exist in that commit.");
            return;
        }


        File wdVersion = new File(wd + "/" + toCheckout.getName());

        if (wdVersion.exists()) {
            wdVersion.delete();
        }

        try {
            Files.copy(toCheckout.toPath(), (new File(wd + "/" + name)).toPath());
        } catch (IOException e) {
            System.out.println("Failed to copy");
        }
    }

    public void checkoutBranch(String name) {
        File wd = new File(System.getProperty("user.dir"));
        File cPath = new File(System.getProperty("user.dir") + "/.gitlet/Commits");
        File sPath = new File(System.getProperty("user.dir") + "/.gitlet/Serialized");
        TreeMap<String, String> pointers;
        try {
            ObjectInputStream inp = new ObjectInputStream(
                    new FileInputStream(sPath + "/pointers.txt"));
            pointers = (TreeMap) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            pointers = new TreeMap<>();
        }
        if (!pointers.containsKey(name)) {
            System.out.println("No such branch exists.");
            return;
        }
        String commitID = pointers.get(name);

        if (pointers.get("HEAD").equals(name)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        File commit = new File(cPath + "/" + commitID);
        File current = new File(cPath + "/" + pointers.get(pointers.get("HEAD")));

        if (commit.listFiles().length > 0) {
            for (File f : commit.listFiles()) {
                if (!((new File(current + "/" + f.getName())).exists())
                    && (new File(wd + "/" + f.getName())).exists()) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it or add it first.");
                    return;
                }
                if (!(f.getName().equals("logMessage.txt"))
                        && !(f.getName().equals("timeStamp.txt"))
                        && !(f.getName().equals("parentHash.txt"))) {
                    File toCheckout = new File(commit + "/" + f.getName());
                    if (!toCheckout.exists()) {
                        System.out.println("File does not exist in that commit.");
                        return;
                    }
                    File wdVersion = new File(wd + "/" + f.getName());
                    if (wdVersion.exists()) {
                        wdVersion.delete();
                    }
                    try {
                        Files.copy(toCheckout.toPath(),
                                (new File(wd + "/" + f.getName())).toPath());
                    } catch (IOException e) {
                        System.out.println("Failed to copy");
                    }
                }
            }
        }

        File head = new File(cPath + "/" + pointers.get(pointers.get("HEAD")));
        if (head.listFiles().length > 0) {
            for (File f : head.listFiles()) {
                if (!(f.getName().equals("logMessage.txt"))
                        && !(f.getName().equals("timeStamp.txt"))
                        && !(f.getName().equals("parentHash.txt"))
                        && !(new File(cPath + "/" + commitID + "/" + f.getName()).exists())) {
                    File toDelete = new File(wd + "/" + f.getName());
                    toDelete.delete();
                }
            }
        }
        pointers.put("HEAD", name);
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
