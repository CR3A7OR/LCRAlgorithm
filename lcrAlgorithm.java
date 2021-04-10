import java.util.*;
import java.util.Scanner;

/* Class to generate a Double Linked list that can simulate and manage a network with LCR and HS */
class DoublyLinkedList {
    int messageCounter = 0;
    /* Class to manage each nodes local states and data */
    class Node{
        int data;                   //holds ID of individual node
        int inBuffer;               // receive id passed from counter clockwise neighbour
        int outBuffer;              //receive id passed from clockwise neighbour (LCR used to send ID from current node to next)
        int terminateBuffer = 0;    //flag to inform node if they should be terminating this round or next round 
        String status = "unknown";  //status of node 
        boolean  terminate = false; //flag to see whether node has terminated or not
        
        Node prev;  
        Node next; 
        
        /* assign id of node to .data local variable */        
        public Node(int data) {  
            this.data = data;  
        }
        
        /* LCR method to send id from current node to clockwise neighbour node */
        public void send(){
            if(!(this.next.inBuffer == this.outBuffer)){
                this.next.inBuffer = this.outBuffer;
                messageCounter += 1;
            }
        }
        
        /* LCR method that will perform the receive and update protocol for a node */
        public void receiveUpdate(){
            /*
            * if the receiving id is larger than the current nodes id it will prepare to send the receiving id in the next round
            * else if the received id is the same as the current nodes id it will declare leader change state and inform the next node
            * to terminate next round 
            */
            if (this.inBuffer > this.data) {
                 this.outBuffer = this.inBuffer;
            }              
            else if (this.inBuffer == this.data) {
                    this.status = "leader";
                    this.outBuffer = this.data;
                    //this.send();
                    this.terminateBuffer = 2;
                    this.next.terminateBuffer = 1;
                    messageCounter += 1;
                    System.out.println(this.data + " IS THE LEADER, BEGINNING TERMINATION");
                    System.out.println(this.data + " terminated");
                    this.terminate = true;
                    
                }
            else if (this.inBuffer < this.data) {
                    //do nothing and let next node continue
                }
        }
    }    
    
    /*
    * Method will add a new node to the network and conform it the design of a ring
    * by taking the tail.next pointer and assign to the head node, vice versa with the head node
    */
    Node head, tail = null; 
    public void addNode(int id) {   
        Node node = new Node(id);  
   
        /*
        * if this is the first node then, head and tail will be both be instantiated
        * else the next node will be appended to the end of the list and assigned as tail
        * its pointers will then be set as the previous node and the head of the network
        */ 
        if(head == null) {  
            head = tail = node;   
            head.prev = null;  
            tail.next = null;  
        }  
        else {  
            tail.next = node;  
            node.prev = tail;  
            tail = node;  
            tail.next = head;
            head.prev = tail;
            
        }  
    }
    
    /* LCR method that performs the LCR algorithm on the network to elect a leader from the ring */ 
    public void LCR(int size) {
        int round = 0;
        Node current = head;
        /* if there are no nodes in the ring then terminate the algorithm */
        if(head == null) {  
            System.out.println("Ring is empty");  
            return;  
        }  
        int test = size;
        boolean globaltermination = false;
        
        /* perform the LCR algorithm on network whilst there are still nodes that have not been terminated */
        while (globaltermination == false) {
            size = test;
            round += 1;
        /* iterate through the size of the network to simulate a symmetric network performance by having each node take turns*/
        while (size > 0) {
            /* if the round is 1 then all nodes can only send their id to their neighbours */
            if (round == 1) {
                current.outBuffer = current.data;
                current.send();
                current = current.next;
                
            }
            else {
                /* if the terminationBuffer is two then the node has been called to terminate this round*/
                if (current.terminateBuffer == 2 && current.terminate == false) {
                    /* the node will send the leader id and set the flag for the next node to terminate next round before terminating itself */
                    current.outBuffer = current.inBuffer;
                    //current.send();
                    messageCounter += 1;
                    current.terminate = true;
                    current.next.terminateBuffer = 1;
                    System.out.println(current.data + " terminated");
                    current = current.next;               
                }
                /* if the node has terminated then it does not perform an action */
                else if (current.terminate == true) {
                    current = current.next;
                }
                else {
                    /* otherwaise nodes should complete the send, receive, update protocol*/
                    
                        current.send();
                    /* if a node has the warning flag at one this round it will increment it so it can terminate next round */
                    if (current.terminateBuffer == 1) {
                        current.terminateBuffer = 2;
                    }
                        current.receiveUpdate();
                        current = current.next;
                }
               
            }
            size -= 1;
        }
        /* iterate through all nodes and check if they have terminated or not */
        for (int i = 0; i < test; i++) {
            /* if a single node is found with the termiante flag set as false then we continue with the LCR algorithm */
            if (current.terminate == true) {
                globaltermination = true;
                current = current.next;
            }
            else {
                globaltermination = false;
                break;
            }
        }
        //print the ring out to terminal
        printNodes(test);
        }
        //print out the final amount of rounds and the maximum node that has been elected
        System.out.println("Rounds: " + round + " Total Messages: " + messageCounter + " Umax: " + current.next.data);
    }
      
    /* Print each node out to form final network print */
    public void printNodes(int size) {  
        Node current = head;  
        System.out.println("Nodes list: ");  
        /* iterate through entire ring list and print the receiving node ID : nodes ID : outgoing node ID */
        while(size > 0) {  
            System.out.print(current.inBuffer + ":");
            System.out.print(current.data);
            System.out.print(":" + current.outBuffer + " ");
            current = current.next;
            size -= 1;
        }   
           System.out.print("\n");
           System.out.print("\n");
    }

}

 /* Main class that handles the user inputs and generating the initial network */
class Main {
  /* main method that performs all the method calls and collect the parameters for the network */
  public static void main(String[] args) {
      DoublyLinkedList nodes = new DoublyLinkedList();
      Scanner input = new Scanner(System.in); //use scanner to collect user input from the terminal
      
      /*collect how many nodes are within the ring network */
      System.out.println("Enter amount of nodes: "); 
      int size = input.nextInt();
      input.nextLine();
      
      /* find out what order the id assignment for the ring network should be */
      System.out.println("Enter ID assignment (Ascending A / Descending D / Random R): ");   
      String assignment = input.nextLine();
      
      if (assignment.equals("Ascending") || assignment.equals("A")){
         nodes = ASRing(size);
      }
      else if (assignment.equals("Descending") || assignment.equals("D")) {
          nodes = DERing(size);
      }
      else if (assignment.equals("Random") || assignment.equals("R")) {
        nodes = randomRing(size);
      }
      
      nodes.LCR(size);
      
  }
  
  /* Instantiation of network and ascending order of id assignment for each node in the ring */
  public static DoublyLinkedList ASRing (Integer size) {
    DoublyLinkedList nodes = new DoublyLinkedList();
    int count = 1;
    while (size > 0) {
        nodes.addNode(count);
        count += 1;
        size -= 1;
    }
    return nodes;
  }   
  
  /* Instantiation of network and descending order of id assignment for each node in the ring */
  public static DoublyLinkedList DERing (Integer size) {
    DoublyLinkedList nodes = new DoublyLinkedList();
    while (size > 0) {
        nodes.addNode(size);
        size -= 1;
    }
    return nodes;
  }      
  
  /* Instantiation of network and random order of id assignment for each node in the ring */  
  public static DoublyLinkedList randomRing(Integer size) {
    DoublyLinkedList nodes = new DoublyLinkedList();
    ArrayList<Integer> list = Random(size); //fill in a temporary array with ring size
     /* 
     * iterate through size of network and each time populate the DoublyLinkedList with a random node chosen from the ArrayList
     * then delete that node as an available choice to prevent duplicate ids
     */
    while (size > 0) {
        Random rand = new Random();
        int index = rand.nextInt(list.size());
        int id = list.get(index);
        nodes.addNode(id);
        list.remove(index);
        size -= 1;
    }
    return nodes;
  }
  
  /* populate and arraylist with integer values from 1 to the amount of nodes */
  public static ArrayList<Integer> Random(Integer range){
    ArrayList<Integer> list = new ArrayList<Integer>(range);
    for(int i = 1; i <= range; i++) {
        list.add(i);
    }
    return list;
  }  
    
}