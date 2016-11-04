package br.edu.unifei.rmss.graphgenerator.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class ClusterGenerator extends BaseGenerator {

    public ClusterGenerator(int size) {
        super(size, 2);

        gen = new RandomGenerator(10);
        gen.addSink(graph);

    }

    @Override
    protected void process() {
        Random rand = new Random();
        
        List<List<Node>> connections = new ArrayList<>();
        graph.clear();

        for (int i = 0; i < getPartitions(); i++) {
        
            List<Node> part = new ArrayList<>();
            
            Graph gp = new SingleGraph("gp"+i);
            gp.setStrict(false);
            gp.setAutoCreate(true);
            Generator gengp= new RandomGenerator(10);
            gengp.addSink(gp);
            
            gengp.begin();  
            gp.clear();
            while (gp.getNodeCount()<(getSize()/getPartitions())) {
                gengp.nextEvents();            
            }                           
            gengp.end();
        
            for (Node node : gp.getNodeSet()) {
                Node n = graph.addNode(String.valueOf(i)+node.getId());
                part.add(n);
            }
            
            for (Edge edge : gp.getEdgeSet()) {
                String name = String.valueOf(i)+edge.getId();
                Node n1 = graph.getNode(String.valueOf(i)+edge.getNode0().getId());
                Node n2 = graph.getNode(String.valueOf(i)+edge.getNode1().getId());
                graph.addEdge(name , n1, n2);                
            }
            
            connections.add(part);
        }
        
        for (int i = 0; i < connections.size(); i++) {
            
            for (int j = i+1; j < connections.size(); j++) {
                
                List<Node> part1 = connections.get(i);
                List<Node> part2 = connections.get(j);                
                
                int inner = Math.abs( (getSize()/getPartitions()) / 50 );
                int num = 1 + rand.nextInt(inner);
                for (int k = 0; k < num; k++) {
                
                    Node n1 = part1.get(rand.nextInt(part1.size()));
                    Node n2 = part2.get(rand.nextInt(part2.size()));
                    String name = n1.getId()+n2.getId();
                    graph.addEdge(name, n1, n2);
                    
                }
                
            }
            
        }
        
        
    }

}
