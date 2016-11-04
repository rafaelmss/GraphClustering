package br.edu.unifei.rmss.graphgenerator.generators;

import org.graphstream.graph.Node;


public class GridGenerator extends BaseGenerator{

    public GridGenerator(int size) {
        super(size, 2);

        gen = new org.graphstream.algorithm.generator.GridGenerator();
        gen.addSink(graph);
    }

    @Override
    protected void process() {
        while (graph.getNodeCount() <= getSize()) {
            gen.nextEvents();            
        }
        
        boolean change = false;
        for (int i=0; i<(graph.getNodeCount()/2); i++) {
            if (change){
                Node n = graph.getNode(i+"_"+i);
                if (n == null){
                    break;
                }
                graph.removeNode(n.getId());
            }
            change = !change;
        }
        //gerar as partições
    }
    
}
