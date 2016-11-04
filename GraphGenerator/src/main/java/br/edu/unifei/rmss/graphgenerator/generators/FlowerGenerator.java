package br.edu.unifei.rmss.graphgenerator.generators;

import java.util.Random;
import org.graphstream.algorithm.generator.FlowerSnarkGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;


public class FlowerGenerator extends BaseGenerator{

    public FlowerGenerator(int size) {
        super(size, 2);

        gen = new FlowerSnarkGenerator();
        gen.addSink(graph);
    }

    @Override
    protected void process() {
        while (graph.getNodeCount() <= getSize()) {
            gen.nextEvents();            
        }
        
        //gerar as partições
        Random rand = new Random();
        Node n1 = graph.getNode(rand.nextInt(graph.getNodeCount()));
        Node n2 = null;
        for (Edge edge : n1.getEachEdge()) {
            n2 = edge.getOpposite(n1);
        }
        
        graph.removeNode(n1);
        graph.removeNode(n2);
    }
    
}
