package gitlet;


import java.io.File;
import java.io.IOException;

/* Driver class for Gitlet, the tiny stupid version-control system.
   @Karina Pichardo, Maryam Sabeti, Erika Pham, Eric Yang
*/
public class Main {

    /* Usage: java gitlet.Main ARGS, where ARGS contains
       <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args[0].equals("init")) {
            Init initialize = new Init();
            initialize.init();
        } else if (args[0].equals("commit")) {
            Commit c = new Commit(args[1], false);
            c.commit(false);
        } else if (args[0].equals("add")) {
            (new Stage()).add(new File(args[1]));
        } else if (args[0].equals("rm")) {
            (new Stage()).rm(new File(args[1]));
        } else if (args[0].equals("branch")) {
            Branch.branch(args[1]);
        } else if (args[0].equals("rm-branch")) {
            Branch.rmBranch(args[1]);
        } else if (args[0].equals("status")) {
            (new Stage()).status();
        } else if (args[0].equals("log")) {
            (new Logging()).log();
        } else if (args[0].equals("global-log")) {
            (new Logging()).globalLog();
        } else if (args[0].equals("find")) {
            (new Find()).find(args[1]);
        } else if (args[0].equals("checkout")) {
            if (args[1].equals("--")) {
                (new Checkout()).checkout(args[2]);
            } else if (args.length > 2) {
                if (args[2].equals("--")) {
                    (new Checkout()).checkout(args[1], args[3]);
                } else {
                    System.out.println("Incorrect operands.");
                }
            } else {
                (new Checkout()).checkoutBranch(args[1]);
            }
        } else if (args[0].equals("reset")) {
            (new Reset()).reset(args[1]);
        } else if (args[0].equals("merge")) {
            (new Merge()).merge(args[1]);
        }

     }

}
