package gitlet;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.TreeMap;
import java.util.TreeSet;

public class Branch {

    public static void branch(String name) throws IOException {
        TreeMap<String, String> pointers = new TreeMap<>();
        TreeSet<String> removedMark = new TreeSet<>();

        File sPath = new File(System.getProperty("user.dir") + "/.gitlet/Serialized");
        String fileName = System.getProperty("user.dir") + "/.gitlet/Serialized/pointers.txt";

        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(fileName));
            pointers = (TreeMap) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            pointers = new TreeMap<>();
        }

        if (pointers.containsKey(name)) {
            System.out.println("A branch with that name already exists.");
            return;
        }

        if (new File(sPath + "/removedMark" + pointers.get("HEAD") + ".txt").exists()) {
            Files.copy(new File(sPath + "/removedMark" + pointers.get("HEAD") + ".txt").toPath(),
                    new File(sPath + "/removedMark" + name + ".txt").toPath());
        }

        String currentCommit = pointers.get(pointers.get("HEAD"));

        pointers.put(name, currentCommit);

        String oG = pointers.get("HEAD");

        if (name.compareTo(oG) > 0) {
            pointers.put(pointers.get("HEAD") + "/" + name, currentCommit);
        } else {
            pointers.put(name + "/" + pointers.get("HEAD"), currentCommit);
        }

        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
            out.writeObject(pointers);
            out.close();
        } catch (IOException excp) {
            System.out.print("Map serialization failed.");
        }
    }

    public static void rmBranch(String name) {
        TreeMap<String, String> pointers = new TreeMap<>();

        String fileName = System.getProperty("user.dir") + "/.gitlet/Serialized/pointers.txt";
        File sPath = new File(System.getProperty("user.dir") + "/.gitlet/Serialized");

        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(fileName));
            pointers = (TreeMap) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            pointers = new TreeMap<>();
        }

        pointers.remove(name);

        (new File(sPath + "/removedMark" + name + ".txt")).delete();

        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
            out.writeObject(pointers);
            out.close();
        } catch (IOException excp) {
            System.out.print("Map serialization failed.");
        }
    }

}
