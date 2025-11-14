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

    
        TreeNode getNodeWithName(String targetName) {
            if (this.name.equals(targetName)) return this;   
          
            for (TreeNode child : children) {
                TreeNode found = child.getNodeWithName(targetName);
                if (found != null) return found;
            }
            return null; 
        }
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


    public FamilyTree() throws IOException, TreeException {
     
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Family tree text files", "txt");
        File dirf = new File("data");
        if (!dirf.exists()) dirf = new File(".");

        JFileChooser chooser = new JFileChooser(dirf);
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) System.exit(1);
        File treeFile = chooser.getSelectedFile();

 
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
    private void addLine(String line) throws TreeException {
       
        int colonIndex = line.indexOf(':');
        if (colonIndex < 0) {
            throw new TreeException("Bad line (missing colon): " + line);
        }

        String parent = line.substring(0, colonIndex);
        String childrenString = line.substring(colonIndex + 1); 
        String[] childrenArray = childrenString.isEmpty() ? new String[0] : childrenString.split(",");

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

        
        for (String kid : childrenArray) {
            String childName = kid.trim();
            if (childName.isEmpty()) continue;

            TreeNode existing = root.getNodeWithName(childName);
            TreeNode child = (existing != null) ? existing : new TreeNode(childName);
            parentNode.addChild(child);
        }
    }

    TreeNode getMostRecentCommonAncestor(String name1, String name2) throws TreeException {
  
        TreeNode node1 = root.getNodeWithName(name1);  
        if (node1 == null) throw new TreeException("No such person: " + name1);

        TreeNode node2 = root.getNodeWithName(name2);  
        if (node2 == null) throw new TreeException("No such person: " + name2);

        
        ArrayList<TreeNode> ancestorsOf1 = node1.collectAncestorsToList();
        ArrayList<TreeNode> ancestorsOf2 = node2.collectAncestorsToList();

      
        for (TreeNode n1 : ancestorsOf1) {
            if (ancestorsOf2.contains(n1)) return n1;
        }
     
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
