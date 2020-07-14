package gitlet;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeMap;
import java.util.TreeSet;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Stage {

    static File wd = new File(System.getProperty("user.dir"));
    TreeMap<String, String> fileNametoID = new TreeMap<>();
//    TreeMap<String, Boolean> removedMark = new TreeMap<>();
    TreeMap<String, String> pointerTree = new TreeMap<>();
    static File sPath = new File(System.getProperty("user.dir") + "/.gitlet/Serialized");
    TreeSet<String> tracked = new TreeSet<>();
    TreeSet<String> removedMark = new TreeSet<>();



    /*Checks if the file exists in the working directory.
     If it does then it checks if the file
     * name is set to be removed from the staging class
     * (removeMark hashmap keeps track of this).
     * If it is then it is changed to be false. If it is
     * the first time adding the file then the fileName
     * is added to the fileNametoID hashmap with the value
     * being the hashID , the removeMark hashMap, and the
     * tracking hasMap. If the file is a new version then \
     * we check that the hashID's are not the same and
     * update them. */

    public void add(File fileName) {
        try {
            ObjectInputStream inp = new ObjectInputStream(
                    new FileInputStream(sPath + "/fileNametoID.txt"));
            fileNametoID = (TreeMap) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            fileNametoID = new TreeMap<>();
        }

        try {
            ObjectInputStream inp = new ObjectInputStream(
                    new FileInputStream(sPath + "/removedMark" + pointerTree.get("HEAD") + ".txt"));
            removedMark = (TreeSet) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            removedMark = new TreeSet<>();
        }

        if (!fileName.exists()) {
            System.out.println("File does not exist.");
        } else if ((new File(wd + "/.gitlet/Removed Files/" + fileName.getName())).exists()) {
            (new File(wd + "/.gitlet/Removed Files/" + fileName.getName())).delete();
        } else {
            String name = fileName.getName();
            String hashID = Utils.sha1(new String(Utils.readContents(fileName)));
            String hashIDnumber = fileNametoID.get(name);

            if (!fileNametoID.containsKey(name)) {
                fileNametoID.put(name, hashID);
                removedMark.remove(name);
                try {
                    Files.copy(fileName.toPath(), Paths.get(wd + "/.gitlet/Staging Area/" + name));
                } catch (IOException e) {
                    return;
                }

            } else if (!hashIDnumber.equals(hashID)) {
                fileNametoID.replace(name, hashIDnumber, hashID);
                try {
                    Files.copy(fileName.toPath(), Paths.get(".gitlet/Staging Area/" + name));
                } catch (IOException e) {
                    return;
                }
                if (removedMark.contains(name)) {
                    removedMark.remove(name);
                }
            }
        }

        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(sPath + "/fileNametoID.txt"));
            out.writeObject(fileNametoID);
            out.close();
        } catch (IOException excp) {
            System.out.print("Map serialization failed.");
        }

        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(sPath + "/removedMark" + pointerTree.get("HEAD") + ".txt"));
            out.writeObject(removedMark);
            out.close();
        } catch (IOException excp) {
            System.out.print("Map serialization failed.");
        }

    }

    public void rm(File file) {
        //access head key in tree map, key is HEAD, value is the branch;
        //in tree map get key master which will return the most recent commit
        try {
            ObjectInputStream inp = new ObjectInputStream(
                    new FileInputStream(sPath + "/pointers.txt"));
            pointerTree = (TreeMap) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            pointerTree = new TreeMap<>();
        }

        try {
            ObjectInputStream inp = new ObjectInputStream(
                    new FileInputStream(sPath + "/tracked.txt"));
            tracked = (TreeSet) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            tracked = new TreeSet<>();
        }

        try {
            ObjectInputStream inp = new ObjectInputStream(
                    new FileInputStream(sPath + "/removedMark" + pointerTree.get("HEAD") + ".txt"));
            removedMark = (TreeSet) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            removedMark = new TreeSet<>();
        }

        //deletes from working directory if the file was being tracked
        String head = pointerTree.get("HEAD");
        String branch = pointerTree.get(head);
        String commitHash = pointerTree.get(branch);

        File commit = new File(wd + "/.gitlet/Commit/" + commitHash);

        File staging = new File(wd + "/.gitlet/Staging Area/" + file.getName());

        if (tracked.contains(file.getName())) {
            tracked.remove(file.getName());
            removedMark.add(file.getName());
            File wdVersion = new File(wd + "/" + file.getName());
            if (wdVersion.exists()) {
                wdVersion.delete();
            }
            try {
                new File(wd + "/.gitlet/Removed Files/" + file.getName()).createNewFile();
            } catch (IOException e) {
                System.out.println("File not moved to Removed Files folder.");
            }
            if (staging.exists()) {
                staging.delete();
            }
        } else if (!tracked.contains(file.getName()) && staging.exists()) {
            staging.delete();
        } else if (!tracked.contains(file.getName()) && !staging.exists()) {
            System.out.println("No reason to remove the file.");
            return;
        }

        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(sPath + "/tracked.txt"));
            out.writeObject(tracked);
            out.close();
        } catch (IOException excp) {
            System.out.print("Map serialization failed.");
        }

        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(sPath + "/removedMark" + pointerTree.get("HEAD") + ".txt"));
            out.writeObject(removedMark);
            out.close();
        } catch (IOException excp) {
            System.out.print("Map serialization failed.");
        }

    }


    public void status() {
        TreeMap<String, String> pointers;

        try {
            ObjectInputStream inp = new ObjectInputStream(
                    new FileInputStream(sPath + "/pointers.txt"));
            pointers = (TreeMap) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            pointers = new TreeMap<>();
        }

        System.out.println("=== Branches ===");
        for (String key : pointers.keySet()) {
            if (!key.equals("HEAD") && !key.contains("/")) {
                if (key.equals(pointers.get("HEAD"))) {
                    System.out.println("*" + key);
                } else {
                    System.out.println(key);
                }
            }
        }
        System.out.print("\n");

        File stagedFiles = new File(wd + "/.gitlet/Staging Area");
        System.out.println("=== Staged Files ===");
        for (File file: stagedFiles.listFiles()) {
            System.out.println(file.getName());
        }
        System.out.print("\n");

        File removedFiles = new File(wd + "/.gitlet/Removed Files");
        System.out.println("=== Removed Files ===");
        for (File file : removedFiles.listFiles()) {
            System.out.println(file.getName());
        }
        System.out.print("\n");

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.print("\n");

        System.out.println("=== Untracked Files ===");
    }
}
