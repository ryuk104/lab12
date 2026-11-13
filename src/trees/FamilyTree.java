package trees;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;

public class FamilyTree {

    private static class TreeException extends Exception {
        TreeException(String message) {
            super(message);
        }
    }

    private static class TreeNode<T> {
        private T data;
        private TreeNode<T> parent;
        private ArrayList<TreeNode<T>> children;

        TreeNode(T data) {
            this.data = data;
            children = new ArrayList<>();
        }

        T getData() {
            return data;
        }

        void addChild(TreeNode<T> childNode) {
            if (childNode == null)
                throw new IllegalArgumentException("Cannot add a null child");
            children.add(childNode);
            childNode.parent = this;
        }

        TreeNode<T> getNodeWithData(T targetData) {
            if (Objects.equals(data, targetData))
                return this;
                    
            for (TreeNode<T> child: children)
            {
                TreeNode<T> result = child.getNodeWithData(targetData);
                if (result != null)
                    return result;
            }
            
            return null;
        }

        ArrayList<TreeNode<T>> collectAncestorsToList() {
            ArrayList<TreeNode<T>> ancestors = new ArrayList<>();

            TreeNode<T> current = parent;
            while (current != null) {
                ancestors.add(current);
                current = current.parent;
            }

            return ancestors;
        }

        public String toString() {
            return toStringWithIndent("");
        }

        private String toStringWithIndent(String indent) {
            String s = indent + data + "\n";
            indent += "  ";
            for (TreeNode<T> childNode : children)
                s += childNode.toStringWithIndent(indent);
            return s;
        }
    }

    private TreeNode<String> root;

    //
    // Displays a file browser so that user can select the family tree file.
    //
    public FamilyTree() throws IOException, TreeException {
        // User chooses input file. This block doesn't need any work.
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Family tree text files", "txt");
        File dirf = new File("data");
        if (!dirf.exists()) dirf = new File(".");

        JFileChooser chooser = new JFileChooser(dirf);
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) System.exit(1);
        File treeFile = chooser.getSelectedFile();

        // Parse the input file. Create a FileReader that reads treeFile. Create a BufferedReader
        // that reads from the FileReader.
        FileReader fr = new FileReader(treeFile);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null)
            addLine(line);
        br.close();
        fr.close();
    }

    //
    // Line format is "parent:child1,child2 ..."
    // Throws TreeException if line is illegal.
    //
    private void addLine(String line) throws TreeException
    {
        // Extract parent and array of children.
        if (line == null || line.trim().isEmpty())
            throw new TreeException("Family-tree line cannot be empty");

        int colonIndex = line.indexOf(':');
        if (colonIndex < 0)
            throw new TreeException("Missing ':' in line: " + line);
        if (line.indexOf(':', colonIndex + 1) >= 0)
            throw new TreeException("Too many ':' characters in line: " + line);

        String parent = line.substring(0, colonIndex).trim();
        if (parent.isEmpty())
            throw new TreeException("Missing parent name in line: " + line);

        String childrenString = line.substring(colonIndex + 1).trim();
        if (childrenString.isEmpty())
            throw new TreeException("Missing child name(s) in line: " + line);
        String[] childrenArray = childrenString.split(",");
        
        // Find parent node. If root is null then the tree is empty and the
        // parent node must be constructed. Otherwise the parent node should be 
        // somewhere in the tree.
        TreeNode<String> parentNode;
        if (root == null)
            parentNode = root = new TreeNode<String>(parent);
        else
        {
            parentNode = root.getNodeWithData(parent);
            if (parentNode == null)
                throw new TreeException("Parent '" + parent + "' was not found in the existing tree");
        }
        
        // Add child nodes to parentNode.
        for (String childName : childrenArray) {
            childName = childName.trim();
            if (childName.isEmpty())
                throw new TreeException("Missing child name in line: " + line);
            if (parentNode.getNodeWithData(childName) != null)
                throw new TreeException("Duplicate name or cycle involving '" + childName + "'");
            parentNode.addChild(new TreeNode<String>(childName));
        }
    }

    // Returns the "deepest" node that is an ancestor of the node named name1, and
    // also is an
    // ancestor of the node named name2.
    //
    // "Depth" of a node is the "distance" between that node and the root. The depth
    // of the root is 0. The
    // depth of the root's immediate children is 1, and so on.
    //
    TreeNode<String> getMostRecentCommonAncestor(String name1, String name2) throws TreeException
    {
        // Get nodes for input names.
        if (root == null)
            throw new TreeException("Cannot search an empty family tree");

        TreeNode<String> node1 = root.getNodeWithData(name1);        // node whose name is name1
        if (node1 == null)
            throw new TreeException("Person not found: " + name1);
        TreeNode<String> node2 = root.getNodeWithData(name2);        // node whose name is name2
        if (node2 == null)
            throw new TreeException("Person not found: " + name2);
        
        // Get ancestors of node1 and node2.
        ArrayList<TreeNode<String>> ancestorsOf1 = node1.collectAncestorsToList();
        ArrayList<TreeNode<String>> ancestorsOf2 = node2.collectAncestorsToList();

        // If the two names are the same, that node is their most recent common ancestor.
        if (node1 == node2)
            return node1;

        // The common ancestor can be one of the two input nodes when one
        // person is an ancestor of the other.
        if (ancestorsOf2.contains(node1))
            return node1;
        if (ancestorsOf1.contains(node2))
            return node2;
        
        // Check members of ancestorsOf1 in order until you find a node that is also
        // an ancestor of 2. 
        for (TreeNode<String> n1: ancestorsOf1)
            if (ancestorsOf2.contains(n1))
                return n1;
        
        // No common ancestor.
        return null;
    }

    public String toString() {
        return "Family Tree:\n\n" + root;
    }

    public static void main(String[] args) {
        try {
            FamilyTree tree = new FamilyTree();
            System.out.println("Tree:\n" + tree + "\n**************\n");
            //TreeNode<String> ancestor = tree.getMostRecentCommonAncestor("Bilbo", "Frodo");
            //System.out.println("Most recent common ancestor of Bilbo and Frodo is " + ancestor.getData());
        } catch (IOException x) {
            System.out.println("IO trouble: " + x.getMessage());
        } catch (TreeException x) {
            System.out.println("Input file trouble: " + x.getMessage());
        }
    }
}
