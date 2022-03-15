import java.util.HashMap;

class Map {
    private HashMap<Character, String> map;
    private HashMap<String, Character> reverseMap;

    public Map(){
        map = new HashMap<>();
        reverseMap = new HashMap<>();
    }

    public void put(char c, String s){
        map.put(c, s);
        reverseMap.put(s, c);
    }

    public String getValue(char c){
        if(map.containsKey(c))
            return map.get(c);
        return null;
    }

    public char getKey(String s){
        if(reverseMap.containsKey(s))
            return reverseMap.get(s);
        return 0;
    }
}

class node {
    int count;
    char c;
    int rank;
    node left, right, parent;

    public node(int count, char c, int rank) {
        this.count = count;
        this.c = c;
        this.rank = rank;
    }
}

class tree {
    public node root;
    private Map shortCodes;

    public tree() {
        root = new node(0, '$', 100);// nyt node
        shortCodes = new Map();
        shortCodes.put('A', "00");
        shortCodes.put('B', "01");
        shortCodes.put('C', "10");
    }

    private node findNode(int rank){
        node temp = root;
        while (temp.left != null){
            if (temp.left.c == '&' || temp.left.c == '$') {
                if(temp.right.rank == rank)
                    return temp.right;
                if(temp.left.rank == rank)
                    return temp.left;
                temp = temp.left;
            } else {
                if(temp.left.rank == rank)
                    return temp.left;
                if(temp.right.rank == rank)
                    return temp.right;
                temp = temp.right;
            }
        }
        return null;
    }

    private node swapIfRequired(node n) {
        node swapnode;
        int rank = 99;
        while (rank > n.rank) {
            swapnode = findNode(rank);
            if (swapnode.count <= n.count) {
                int newRank = n.rank;
                int oldRank = swapnode.rank;

                // Keep track of parents of both nodes getting swapped.
                node oldParent = swapnode.parent;
                node newParent = n.parent;

                // Need to know if nodes were left or right child.
                boolean oldNodeWasOnRight, newNodeWasOnRight;
                oldNodeWasOnRight = newNodeWasOnRight = false;

                if (n.parent.right == n) {
                    newNodeWasOnRight = true;
                }
                if (swapnode.parent.right == swapnode) {
                    oldNodeWasOnRight = true;
                }
                if (newNodeWasOnRight) {
                    newParent.right = swapnode;
                } else {
                    newParent.left = swapnode;
                }
                if (oldNodeWasOnRight) {
                    oldParent.right = n;
                } else {
                    oldParent.left = n;
                }
                // Update the parent pointers.
                swapnode.parent = newParent;
                n.parent = oldParent;
                n.rank = oldRank;
                swapnode.rank = newRank;
                break;
            }
            rank--;
        }
        return n;
    }

    public void insert(char c) {
        node n = search(root, c);
        node nyt = search(root, '$');
        if (n == null) {
            nyt.left = new node(0, '$', nyt.rank - 2);
            nyt.left.parent = nyt;
            nyt.right = new node(1, c, nyt.rank - 1);
            nyt.right.parent = nyt;
            nyt.c = '&';// empty node
            nyt.count = 1;
            if (nyt.parent == null)
                return;
            n = nyt.parent;
        }
        boolean firstIteration = true;
        do {
            if (firstIteration) {
                firstIteration = false;
            } else {
                n = n.parent;
            }
            n = swapIfRequired(n);
            n.count++;
        } while (n.parent != null);
    }

    public String getCode(char c, String code, node root) {
        if (root.c == c)
            return code;
        if (root.left == null && root.right == null)
            return "";
        if (getCode(c, code + "0", root.left).equals(""))
            return getCode(c, code + "1", root.right);
        return getCode(c, code + "0", root.left);
    }

    public node search(node root, char c) {
        if (root.c == c)
            return root;
        if (root.left == null && root.right == null)
            return null;
        if (search(root.left, c) == null)
            return search(root.right, c);
        return search(root.left, c);
    }

    public String getShortCode(char c) {
        return shortCodes.getValue(c);
    }

    public char getCharOfCode(String code){
        node temp = root;
        if (getCode(temp.c, "", root).equals(code))
            return temp.c;
        while (temp.left != null){
            if (temp.left.c == '&' || temp.left.c == '$') {
                if(getCode(temp.right.c, "", root).equals(code))
                    return temp.right.c;
                if(getCode(temp.left.c, "", root).equals(code))
                    return temp.left.c;
                temp = temp.left;
            } else {
                if(getCode(temp.left.c, "", root).equals(code))
                    return temp.left.c;
                if(getCode(temp.right.c, "", root).equals(code))
                    return temp.right.c;
                temp = temp.right;
            }
        }
        return 0;
    }

    public char getCharOfShortCode(String shortCode){
        return shortCodes.getKey(shortCode);
    }
}

public class adaptive {

    static String encode(String s) {
        String encoded = "";
        tree t = new tree();
        for (int i = 0; i < s.length(); i++) {
            if (t.getCode(s.charAt(i), "", t.root).equals(""))
                encoded += t.getCode('$', "", t.root) + t.getShortCode(s.charAt(i));
            else
                encoded += t.getCode(s.charAt(i), "", t.root);
            t.insert(s.charAt(i));
        }
        return encoded;
    }

    static String decode(String code){
        String decoded = "";
        tree t = new tree();
        String sub;
        for (int i = 0; i < code.length(); i++) {
            for (int subSize = 0; subSize <= code.length() - i; subSize++) {
                sub = code.substring(i, i + subSize);
                if(t.getCharOfCode(sub) != 0){
                    if(t.getCharOfCode(sub) == '$'){
                        i += subSize;
                        for (int j = 1; j <= code.length() - i; j++) {
                            sub = code.substring(i, i + j);
                            if (t.getCharOfShortCode(sub) != 0){
                                decoded += "" + t.getCharOfShortCode(sub);
                                t.insert(t.getCharOfShortCode(sub));
                                break;
                            }
                        }
                        i += sub.length() - 1;
                        break;
                    } else if (t.getCharOfCode(sub) == '&') {
                        continue;
                    } else {
                        decoded += "" + t.getCharOfCode(sub);
                        t.insert(t.getCharOfCode(sub));
                        i += sub.length() - 1;
                        break;
                    }
                } 
            }
        }
        return decoded;
    }

    public static void main(String[] args) {
        System.out.println(encode("ABCCCAAAA"));
        System.out.println(decode(encode("ABCCCAAAA")));
    }
}
