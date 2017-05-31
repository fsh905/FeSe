package xyz.fefine.handler;

import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * 二叉搜索树
 * 现已升级为AVL
 * Created by feng_sh on 17-5-31.
 */
public class RequestSearchTree<T extends RequestHandler> implements Comparator<T> {



    private Node<T> root;
    private int size;

    public RequestSearchTree(){

        size = 0;

    }

    public void insert(T t) {
        root = insertNode(root,t);
        size ++;
    }


    /**
     * AVL平衡树练习
     * @param N
     * @param t
     * @return
     */
    private Node<T> insertNode(Node<T> N,T t){



        if(N == null)
            N = new Node<>(t);
        else
            //big is in the right side
            if(N.data.compareTo(t) > 0){
                N.left = insertNode(N.left,t);
                //n & n-> right
                if(height(N) - height(N.right) == 2){
                    if(N.left.data.compareTo(t) > 0)
                        // LL
                        N = lLRotation(N);
                    else
                        //LR
                        N = lRRotation(N);
                }
            }else {
                N.right = insertNode(N.right,t);

                // n - n->right = 2
                if (height(N) - height(N.left) == 2) {
                    // this is <
                    if(N.right.data.compareTo(t) < 0)
                        //RR
                        N = rRRotation(N);
                    else
                        //RL
                        N = rLRotation(N);
                }
            }
        N.height = height(maxHeight(N.left,N.right)) + 1;
        return N;
    }

    //传进来一个url
    public T find(String url){

        //感觉这里很不合理
        RequestHandler t = new RequestHandler();
        t.setUrl(url);
        return findNode(root,(T)t).data;
    }

    private Node<T> findNode(Node<T> node,T t){

        if(node == null)
            return node;
        int r = compare(node.data,t);
        if(r == 0)
            return node;
        if(r > 0)
            return findNode(node.left,t);
        else
            return findNode(node.right,t);


    }


    public boolean empty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    private int height(Node<T> node){
        if(node == null) return -1;
        return node.height;
    }

    private Node maxHeight(Node a,Node b){

        return height(a) > height(b) ? a:b;

    }


    /**
     * 左轮转 对应LL的不平衡树
     * @param node
     * @return
     */
    private Node<T> lLRotation(Node<T> node){

        Node a = node.left;
        Node temp = a.right;
        node.left = temp;
        node.height = height(maxHeight(node.left,node.right))+1;
        a.right = node;
        a.height = height(maxHeight(a.left,a.right))+1;
        return a;
    }

    /**
     * 右轮转 对应RR型的不平衡
     * @param node
     * @return
     */
    private Node<T> rRRotation(Node<T> node){

        Node a = node.right;
        Node temp = a.left;
        node.right = temp;
        node.height = height(maxHeight(node.left,node.right))+1;
        a.left = node;
        a.height = height(maxHeight(a.left,a.right))+1;

        return a;
    }

    /**
     * 左右轮转 对应LR,先右转后左转
     * @param node
     * @return
     */
    private Node<T> lRRotation(Node<T> node){
        node.left = rRRotation(node.left);
        return lLRotation(node);

    }

    /**
     * 右左轮转 对应RL型 先左转,后右转
     * @param node
     * @return
     */
    private Node<T> rLRotation(Node<T> node){

        node.right = lLRotation(node.right);
        return rRRotation(node);

    }

    /**
     * 删除节点
     * @return
     */
    public T remove() {
        T t = root.data;
        root = deleteNode(root,t);
        return t;
    }

    /**
     * 删除指定的节点
     * @param t
     * @return
     */
    public Node<T> remove(T t){

        if(findNode(root,t) != null)
            return root = deleteNode(root,t);
        else
            return root;
    }


    private Node<T> deleteNode(Node<T> node,T t){

        if(node == null)
            return null;

        if(compare(node.data,t) > 0){

            node.left = deleteNode(node.left,t);
            if (height(node.right) - height(node.left) == 2) {
                // this is <
                Node<T> N = node.right;
                if(height(N.right) > height(N.left))
                    //RR
                    node = rRRotation(node);
                else
                    //RL
                    node = rLRotation(node);
            }

        }else if(compare(node.data,t) < 0){
            node.right = deleteNode(node.right,t);
            if(height(node.left) - height(node.right) == 2){

                Node<T> N = node.left;
                if(height(N.left) > height(N.right))
                    // LL
                    node = lLRotation(node);
                else
                    //LR
                    node = lRRotation(node);
            }
        }else{
            //  equal
            if(height(node.left) > height(node.right)){

                //当左树>又树,取出左树最大的,放到node中,然后删除那个最大的
                Node<T> tNode = maxNode(node.left);
                node.data = tNode.data;
                node.left = deleteNode(node.left,node.data);
            }else{
                Node<T> tNode = minNode(node.right);
                //当tnode为null,则说明找到了最小的,直接删除
                if (tNode == null)
                    return null;
                node.data = tNode.data;
                node.right = deleteNode(node.right,node.data);
            }

        }
        node.height = height(maxHeight(node.left,node.right)) + 1;

        return node;
    }

    /**
     *  找到最大的node
     * @param node
     * @return
     */
    private Node<T> maxNode(Node<T> node){

        if(node == null)
            return node;
        return maxNode(node.right);

    }

    /**
     *  找到最小的node
     * @param node
     * @return
     */
    private Node<T> minNode(Node<T> node){

        if(node == null)
            return node;
        return minNode(node.left);

    }

    /**
     * 这里进行requestHandler的比较
     * 先进行匹配，再进行比较，因此相当进行两次比较，搜索时间为2log(n)
     * @param o1
     * @param o2
     * @return
     */
    @Override
    public int compare(T o1, T o2) {
        if(o1.matcher(o2.getUrl()))
            return 0;
        //compareTo进行比较的是urlpattern
        //o2并没有urlpattern
        //return o1.compareTo(o2);

        //这里使用url会出现bug
//        return o1.getUrl().compareTo(o2.getUrl());

        return o1.compareTo(o2);
    }

    /**
     * 测试
     */
    void test() {

        RequestSearchTree<RequestHandler> searchBinTree = new RequestSearchTree<>();

        RequestHandler rh = new RequestHandler();
        rh.setUrl("/main/a");
        rh.setUrlPattern(Pattern.compile("/main/a$"));
        searchBinTree.insert(rh);

        RequestHandler rh1 = new RequestHandler();
        rh1.setUrl("/main/b");
        rh1.setUrlPattern(Pattern.compile("/main/b$"));
        searchBinTree.insert(rh1);

        RequestHandler rh2 = new RequestHandler();
        rh2.setUrl("/se/a");
        rh2.setUrlPattern(Pattern.compile("/se/a$"));
        searchBinTree.insert(rh2);

        RequestHandler rh3 = new RequestHandler();
        rh3.setUrl("/na/a");
        rh3.setUrlPattern(Pattern.compile("/na/a$"));
        searchBinTree.insert(rh3);

        RequestHandler rh4 = new RequestHandler();
        rh4.setUrl("/t/a");
        rh4.setUrlPattern(Pattern.compile("/t/a$"));
        searchBinTree.insert(rh4);

        RequestHandler rh5 = new RequestHandler();
        rh5.setUrl("/a/a/{b}");
        rh5.setUrlPattern(Pattern.compile("/a/a/[^/]+$"));
        searchBinTree.insert(rh5);

        RequestHandler b = searchBinTree.find("/a/a/c");

        System.out.println(b.getUrl()+"--");
        System.out.println(b.getUrlPattern());
    }

}


/**
 * 链式结构的节点
 * Created by feng_ on 2016/5/9.
 */
class Node<T>{

    T data;

    //left and right
    Node<T> left;
    Node<T> right;
    //树的高度,AVL树使用
    int height;


    public Node(T data) {
        this.data = data;
        this.height = 0;
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
