package br.edu.unifei.rmss.graphgenerator.generators;


public class LobsterGenerator extends BaseGenerator{

    public LobsterGenerator(int size) {
        super(size, 2);
        gen = new org.graphstream.algorithm.generator.LobsterGenerator();
        gen.addSink(graph);
    }

    @Override
    protected void process() {
        
        while (graph.getNodeCount() <= getSize()) {
            gen.nextEvents();            
        }
    }
    
    
}
