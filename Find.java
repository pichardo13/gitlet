package gitlet;

import java.io.File;

public class Find {
    String logmsg;
    String msg;
    boolean tracker;
    static File cPath = new File(System.getProperty("user.dir") + "/.gitlet/Commits");

    public void find(String message) {
        this.msg = message;
        tracker = false;
        if (cPath.listFiles() != null) {
            for (File f : cPath.listFiles()) {
                logmsg = new String(Utils.readContents(
                        new File(cPath + "/" + f.getName() + "/logMessage.txt")));
                if (logmsg.equals(msg)) {
                    tracker = true;
                    System.out.println(f.getName());
                }
            }
            if (!tracker) {
                System.out.println("Found no commit with that message.");
            }
        }
    }

}

