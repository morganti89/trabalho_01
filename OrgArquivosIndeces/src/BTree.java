class BTree {
    /**
     * The constant t determines the maximum number of data items that
     * can be stored at each node.
     */
    public static final int t = 3;

    /**
     * Indicates how many data items are stored in this node.  For all
     * nodes except the root of the tree, this will be somewhere between
     * (t-1) and (2t-1).
     */
    private int n;

    /**
     * An array of data items.  Each such array will be allocated (2t-1)
     * slots.
     */
    private Dados[] keys;

    /**
     * An array of pointers to child nodes.  Each such array will be
     * allocated 2t slots.
     */
    private BTree[] children;

    /**
     * Construct an empty B-tree node; we mark this constructor as
     * private because the values in the constructed node will not
     * be valid until further initialization has been carried out.
     */
    private BTree() {
        this.n = 0;
        this.keys = new Dados[2 * t - 1];
        this.children = new BTree[2 * t];
    }

    /**
     * Construct a B-tree node containing just a single value.
     */
    private BTree(Dados value) {
        this();
        this.n = 1;
        this.keys[0] = value;
    }

    /**
     * Determine whether the root node of a (non-null) B-tree is full.
     */
    public boolean full() {
        return (n == 2 * t - 1);
    }

    /**
     * Determine whether a particular value occurs in this tree.
     */
//    public static boolean contains(int value, BTree btree) {
//        while (btree != null) {
//            int i = 0;
//            for (; i < btree.n; i++) {
//                if (value == btree.keys[i]) {
//                    return true;
//                } else if (value <= btree.keys[i]) {
//                    break;
//                }
//            }
//            btree = btree.children[i];
//        }
//        return false;
//    }

    /**
     * Insert a value into a B-tree node.
     */
    public static BTree insert(Dados dados, BTree btree) {
        if (btree == null) {
            return new BTree(dados);
        } else if (btree.full()) {
            BTree root = new BTree(btree.keys[t - 1]);
            root.children[0] = btree;
            root.children[1] = btree.split();
            btree = root;
        }
        // At this point, we can guarantee that btree
        // is a non-null and non-full BTree node.
        insertNonFull(dados, btree);
        return btree;
    }

    /**
     * Insert a value into a non-full (and non-null) B-tree node.
     */
    private static void insertNonFull(Dados value, BTree btree) {
        int i = 0;
        while (i < btree.n && (value.name.compareTo(btree.keys[i].getName()) > 0)) {
            i++;
        }
        if (btree.children[i] == null) {            // Leaf node
            btree.shiftUp(i, value);
            btree.children[i + 1] = null;
        } else if (btree.children[i].full()) {        // Full child
            Dados pivot = btree.children[i].keys[t - 1];
            btree.shiftUp(i, pivot);
            btree.children[i + 1] = btree.children[i].split();
            insertNonFull(value, btree.children[(value.name.compareTo(pivot.getName()) > 0) ? (i + 1) : i]);
        } else {                    // Non-full child
            insertNonFull(value, btree.children[i]);
        }
    }

    /**
     * Shift up the keys and children from a specified position onwards
     * to make room for a new entry in a non-full BTree node.  After
     * the shift, the caller will need to set children[i+1] to the
     * appropriate values.
     */
    private void shiftUp(int i, Dados key) {
        for (int j = n; j > i; j--) {
            keys[j] = keys[j - 1];
            children[j + 1] = children[j];
        }
        keys[i] = key;
        n++;
    }

    /**
     * Split a full B-tree node, modifying the receiver (the left half)
     * and returning the new node (the right half).  We assume that this
     * method is invoked only on full tree nodes, meaning that btree.n
     * will be 2*t-1.
     */
    private BTree split() {
        BTree right = new BTree();
        for (int i = 0; i < t - 1; i++) {
            right.keys[i] = this.keys[t + i];
            right.children[i] = this.children[t + i];
            this.children[t + i] = null;
        }
        right.children[t - 1] = this.children[2 * t - 1];
        this.children[2 * t - 1] = null;
        this.n = (t - 1);
        right.n = (t - 1);
        return right;
    }

    public void print(BTree btree) {
        if (btree==null) {
            System.out.print("-");
        } else {
            System.out.print("(");
            int i = 0;
            for (; i<btree.n; i++) {
                print(btree.children[i]);
                System.out.print(" " + btree.keys[i].getId() + " ");
            }
            print(btree.children[i]);
            System.out.print(")");
        }
    }
}