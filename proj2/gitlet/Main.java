package gitlet;

import static gitlet.Repository.*;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 * 
 * @author zdkk
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     * java gitlet.Main init
     * java gitlet.Main add hello.txt
     * ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                checkargs(args, 1);
                init();
                break;
            case "add":
                checkargs(args, 2);
                add(args[1]);
                break;
            case "commit":
                 checkargs(args, 2);
                 commit(args[1]);
                 break;
            case "rm":
                 checkargs(args, 2);
                 rm(args[1]);
                 break;
            case "log":
                 checkargs(args, 1);
                 log();
                 break;
            case "global-log":
                 checkargs(args, 1);
                 globalLog();
                 break;
            case "find":
                 checkargs(args, 2);
                 find(args[1]);
                 break;
            case "status":
                 checkargs(args, 1);
                 status();
                 break;
            case "checkout":
                 if (args.length == 2) {
                     checkout(args[1]);
                 } else if (args.length == 3){
                     checkout(args[1], args[2]);
                 } else if (args.length == 4) {
                     checkout(args[1], args[2], args[3]);
                 } else {
                     System.out.println("Incorrect operands.");
                     System.exit(0);
                 }
                 break;
             case "branch":
                 checkargs(args, 2);
                 branch(args[1]);
                 break;
             case "rm-branch":
                 checkargs(args, 2);
                 rmBranch(args[1]);
                 break;
             case "reset":
                 checkargs(args, 2);
                 reset(args[1]);
                 break;
            // case "merge":
            //     checkargs(args, 2);
            //     merge(args[1]);
            //     break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }

//        Repository.debug();
    }

    private static void checkargs(String[] args, int num) {
        if (args.length != num) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
