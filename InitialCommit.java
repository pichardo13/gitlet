package gitlet;

import java.io.IOException;

public class InitialCommit extends Commit {
    public InitialCommit() throws IOException {
        super("initial commit", true);
        this.commit(true);
    }
}
