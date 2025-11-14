package trees;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;

public class FamilyTree {

    private static class TreeNode {
        private String name;
        private TreeNode parent;
        private ArrayList<TreeNode> children;

        TreeNode(String name) {
            this.name = name;
            this.children = new ArrayList<>();
        }

        String getName() {
            return name;
        }

        void addChild(TreeNode childNode) {
            // Link both ways
            childNode.parent = this;
            children.add(childNode);
        }

        // Searches the subtree at this node for a node with the given name.
        // Returns the node, or null if not found.
        TreeNode getNodeWithName(String targetName) {
            if (this.name.equals(targetName)) return this;   // found here
            // Recurse through children
            for (TreeNode child : children) {
                TreeNode found = child.getNodeWithName(targetName);
                if (found != null) return found;
            }
            return null; // not found
        }

        // Returns a list of ancestors of this node, starting with this node’s parent
        // and ending at the root (order: recent → ancient).
        ArrayList<TreeNode> collectAncestorsToList() {
            ArrayList<TreeNode> ancestors = new ArrayList<>();
            TreeNode p = this.parent;
            while (p != null) {
                ancestors.add(p);
                p = p.parent;
            }
            return ancestors;
        }

        public String toString() {
            return toStringWithIndent("");
        }

        private String toStringWithIndent(String indent) {
            String s = indent + name + "\n";
            String next = indent + "  ";
            for (TreeNode childNode : children) {
                s += childNode.toStringWithIndent(next);
            }
            return s;
        }
    }

    private TreeNode root;

    // Displays a file browser so that user can select the family tree file.
    public FamilyTree() throws IOException, TreeException {
        // User chooses input file. (Provided code; no changes needed.)
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Family tree text files", "txt");
        File dirf = new File("data");
        if (!dirf.exists()) dirf = new File(".");

        JFileChooser chooser = new JFileChooser(dirf);
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) System.exit(1);
        File treeFile = chooser.getSelectedFile();

        // Parse the input file.
        FileReader fr = new FileReader(treeFile);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty()) addLine(line);
        }
        br.close();
        fr.close();
    }

    // Line format is "parent:child1,child2,..."
    // Throws TreeException if line is illegal.
    private void addLine(String line) throws TreeException {
        // Extract parent and array of children.
        int colonIndex = line.indexOf(':');
        if (colonIndex < 0) {
            throw new TreeException("Bad line (missing colon): " + line);
        }

        String parent = line.substring(0, colonIndex);
        String childrenString = line.substring(colonIndex + 1); // may be empty
        String[] childrenArray = childrenString.isEmpty() ? new String[0] : childrenString.split(",");

        // Find parent node. If root is null then the tree is empty and the
        // parent node must be constructed. Otherwise the parent node should be in the tree.
        TreeNode parentNode;
        if (root == null) {
            root = new TreeNode(parent);
            parentNode = root;
        } else {
            parentNode = root.getNodeWithName(parent);
            if (parentNode == null) {
                throw new TreeException("Parent not found in tree so far: " + parent);
            }
        }

        // Add child nodes to parentNode.
        for (String kid : childrenArray) {
            String childName = kid.trim();
            if (childName.isEmpty()) continue;

            // Reuse an existing node with that name if present; otherwise create it.
            TreeNode existing = root.getNodeWithName(childName);
            TreeNode child = (existing != null) ? existing : new TreeNode(childName);
            parentNode.addChild(child);
        }
    }

    // Returns the "deepest" node that is an ancestor of both name1 and name2
    TreeNode getMostRecentCommonAncestor(String name1, String name2) throws TreeException {
        // Get nodes for input names.
        TreeNode node1 = root.getNodeWithName(name1);   // node whose name is name1
        if (node1 == null) throw new TreeException("No such person: " + name1);

        TreeNode node2 = root.getNodeWithName(name2);   // node whose name is name2
        if (node2 == null) throw new TreeException("No such person: " + name2);

        // Get ancestors (recent → ancient)
        ArrayList<TreeNode> ancestorsOf1 = node1.collectAncestorsToList();
        ArrayList<TreeNode> ancestorsOf2 = node2.collectAncestorsToList();

        // First ancestor of node1 that’s also an ancestor of node2 is the MRCA
        for (TreeNode n1 : ancestorsOf1) {
            if (ancestorsOf2.contains(n1)) return n1;
        }
        // No common ancestor (shouldn't happen in a valid single-root tree)
        return null;
    }

    public String toString() {
        return "Family Tree:\n\n" + root;
    }

    public static void main(String[] args) {
        try {
            FamilyTree tree = new FamilyTree();
            System.out.println("Tree:\n" + tree + "\n**************\n");
            TreeNode ancestor = tree.getMostRecentCommonAncestor("Bilbo", "Frodo");
            System.out.println("Most recent common ancestor of Bilbo and Frodo is " + ancestor.getName());
        } catch (IOException x) {
            System.out.println("IO trouble: " + x.getMessage());
        } catch (TreeException x) {
            System.out.println("Input file trouble: " + x.getMessage());
        }
    }
}
