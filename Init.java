package gitlet;

import java.io.File;
import java.io.IOException;

public class Init {
    static boolean initialized = false;
    static InitialCommit iC;

    public void init() throws IOException {
        File wd = new File(System.getProperty("user.dir"));
        File gitletDir = new File(wd, ".gitlet");

        if (gitletDir.exists()) {
            System.out.println("A gitlet version-control system already"
                    + " exists in the current directory.");
        } else {
            gitletDir.mkdir();

            initialized = true;

            File stagingArea = new File(wd + "/.gitlet", "Staging Area/");
            stagingArea.mkdir();

            File removedFiles = new File(wd + "/.gitlet", "Removed Files/");
            removedFiles.mkdir();

            File commitFolders = new File(wd + "/.gitlet", "Commits/");
            commitFolders.mkdir();

            File serializedFolders = new File(wd + "/.gitlet", "Serialized/");
            serializedFolders.mkdir();

            iC = new InitialCommit();



        }
    }
}


