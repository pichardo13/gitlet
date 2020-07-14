package gitlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.TreeMap;

public class Logging {
    static File cPath = new File(System.getProperty("user.dir") + "/.gitlet/Commits");
    static File headPath = new File(System.getProperty("user.dir")
            + "/.gitlet/Serialized/pointers.txt");
    TreeMap<String, String> head;
    String currentBranch;
    String currentCommit;
    String parentID;
    String commitFiles;
    String logmsg;
    String timestamp;

    public void log() {

        if (cPath.listFiles() != null) {
            try {
                FileInputStream fileIn = new FileInputStream(headPath);
                ObjectInputStream inp = new ObjectInputStream(fileIn);
                this.head = (TreeMap) inp.readObject();
                inp.close();
                fileIn.close();
            } catch (IOException | ClassNotFoundException excp) {
                return;
            }
            this.currentBranch = head.get("HEAD");
            this.currentCommit = head.get(currentBranch);

            if (new File(cPath + "/" + currentCommit + "/parentHash.txt").exists()) {
                this.parentID = new String(Utils.readContents(
                        new File(cPath + "/" + currentCommit + "/parentHash.txt")));
            }

            while (new File(cPath + "/" + currentCommit + "/parentHash.txt").exists()) {
                this.logmsg = new String(Utils.readContents(new File(cPath
                        + "/" + currentCommit + "/logMessage.txt")));
                this.timestamp = new String(Utils.readContents(new File(cPath
                        + "/" + currentCommit + "/timeStamp.txt")));
                System.out.println("===");
                System.out.println("Commit " + currentCommit);
                System.out.println(timestamp);
                System.out.println(logmsg);
                System.out.println("");
                currentCommit = parentID;
                if (new File(cPath + "/" + currentCommit + "/parentHash.txt").exists()) {
                    parentID = new String(Utils.readContents(new File(cPath
                            + "/" + currentCommit + "/parentHash.txt")));
                }
            }

            this.logmsg = new String(Utils.readContents(new File(cPath
                    + "/" + currentCommit + "/logMessage.txt")));
            this.timestamp = new String(Utils.readContents(new File(cPath
                    + "/" + currentCommit + "/timeStamp.txt")));
            System.out.println("===");
            System.out.println("Commit " + currentCommit);
            System.out.println(timestamp);
            System.out.println(logmsg);
            System.out.println("");
        }
    }

    public void globalLog() {
        if (cPath.listFiles() != null) {
            for (File f : cPath.listFiles()) {
                this.logmsg = new String(Utils.readContents(
                        new File(cPath + "/" + f.getName() + "/logMessage.txt")));
                this.timestamp = new String(Utils.readContents(
                        new File(cPath + "/" + f.getName() + "/timeStamp.txt")));
                System.out.println("===");
                System.out.println("Commit " + f.getName());
                System.out.println(timestamp);
                System.out.println(logmsg);
                System.out.println("");
            }
        }
    }
}

