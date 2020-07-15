# Gitlet

This is a project from UC Berkeley CS61BL summer 2018.

This is a version-control system that mimics some of the basic features of the popular version-control system git, but it is smaller and simpler.

To run the project, download and run Main.java. 

Commands:
- Init: Creates a new gitlet version-control system in the current directory. 
        java gitlet.Main init
- Add: Adds a copy of the file as it currently exists to the staging area.
        java gitlet.Main add [file name]
- Commiting: Saving backups of directories of files.
        java gitlet.Main commit [message]
- Remove: Untrack the file; that is, indicate (somewhere in the .gitlet directory) that it is not to be included in the next commit, even if it is tracked in the current commit (which will become the next commit’s parent)
        java gitlet.Main rm [file name]
- Log:  Viewing the history of your backups.
        java gitlet.Main log
- Global-log: Like log, except displays information about all commits ever made. 
        java gitlet.Main global-log
- Find: Prints out the ids of all commits that have the given commit message, one per line. 
        java gitlet.Main find [commit message]
- Status: Displays what branches currently exist, and marks the current branch with a *. Also displays what files have been staged or marked for untracking.
        java gitlet.Main status 
- Checking out: Restoring a backup version of one or more files or entire commits. 
        java gitlet.Main checkout -- [file name]
        java gitlet.Main checkout [commit id] -- [file name]
        java gitlet.Main checkout [branch name]
- Branches: Maintaining related sequences of commits.
        java gitlet.Main branch [branch name]
- Remove Branch: Deletes the branch with the given name. This only means to delete the pointer associated with the branch; it does not mean to delete all commits that were created under the branch, or anything like that.
        java gitlet.Main rm-branch [branch name]
- Reset: Checks out all the files tracked by the given commit. Removes tracked files that are not present in the given commit. Moves the current branch’s head pointer and the head pointer to that commit node. See the intro for an example of what happens to the head pointer after using reset. The [commit id] may be abbreviated as for checkout. The staging area is cleared. The command is essentially checkout of an arbitrary commit that also changes the current branch head pointer.
        java gitlet.Main reset [commit id]
- Merge: Merging changes made in one branch into another.
        java gitlet.Main merge [branch name]

For more information, please check out https://cs61bl.org/su18/projects/gitlet/.
Contributors: Maryam Sabeti, Ericka Pham, and Eric Yang
