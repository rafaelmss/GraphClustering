package br.edu.unifei.rmss;

import br.edu.unifei.rmss.graph.Network;
import br.edu.unifei.rmss.graph.Vertex;
import br.edu.unifei.rmss.graph.neo4j.NetworkNeo4j;
import br.edu.unifei.rmss.utils.GraphStreamDisplay;
import java.util.Iterator;

/**
 *
 * @author rafael
 */
public class MainTest {

    public static void main(String[] args) {

        String fileName;
        
        //Random Generator
        int num_nodes = 10;
        fileName = String.valueOf(num_nodes);        
        Network graph = new NetworkNeo4j("test_"+fileName, true);
        graph.generateRandomGraph(num_nodes);
        
        
        //File Generator       
        //fileName = "v10";        
        //Network graph = new NetworkNeo4j("test_"+fileName);
        //graph.loadFromFile("v10.txt");
        //graph.generateRandomGraph(10);
        //graph.generateRandomConnectedGraph(10);        
        
        System.out.println("Grafo montado!");
        
        /*
        
        for (Vertex v : graph.getAllVertex()) {
            int partition = 1;
            if (v.getId() >= 2){
                partition = 2;
            }
            v.setPartition(partition);
            graph.updateVertex(v);
        }
        
                  
        for (Vertex v : graph.getAllVertex()) {
            System.err.println("O peso dos relacionamentos de "+v.getId()+" é: "+graph.getWeightEdges(v.getId()));
        }
        */
        
        //Network n = graph.getSubnetworkFrontier(1, 2, 1);
        
        GraphStreamDisplay gsd = new GraphStreamDisplay(graph);
        gsd.displayGraph();
        
        //printGraph(graph);
        //printGraph(n);
         
    }
    
    public static void printGraph(Network graph) {
        System.out.println("Number of nodes: "+graph.getNumberOfNodes()+"\n\n");
        
        for (Vertex v : graph.getAllVertex()) {
            System.out.println("Vertex "+v.getId()+":");
            System.out.println("    Weight: "+v.getWeight());
            System.out.println("    Degree: "+graph.getDegreeOfNode(v.getId()));
            if(v.hasPartition()){
            System.out.println("    Partition: "+v.getPartition());}
            System.out.print("    Neighbor: ");
            Iterator<Long> it = graph.getIdFromNeighbor(v);
            while (it.hasNext()){
                long next = it.next();
                System.out.print(next+" ");
            }
            System.out.println("\n");
            
        }

    }
    
    public static void printGraph(Network graph, int level) {
        System.out.println("Number of nodes of level "+level+": "+graph.getNumberOfNodes(level)+"\n\n");
        
        for (Vertex v : graph.getAllVertex(level)) {
            System.out.println("Vertex "+v.getId()+":");
            System.out.println("    Weight: "+v.getWeight());
            System.out.println("    Degree: "+graph.getDegreeOfNode(v.getId(),level));
            System.out.println("    Level: "+v.getLevel());
            if(v.hasPartition()){
            System.out.println("    Partition: "+v.getPartition());}
            System.out.print("    Neighbor: ");
            Iterator<Long> it = graph.getIdFromNeighbor(v);
            while (it.hasNext()){
                long next = it.next();
                System.out.print(next+" ");
            }
            System.out.println("\n");
            
        }

    }
    
}
