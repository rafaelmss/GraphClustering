/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.communities.greedy;

import br.edu.unifei.rmss.communities.CommunityDetector;
import br.edu.unifei.rmss.graph.Network;
import br.edu.unifei.rmss.graph.Vertex;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author aluno
 */
public abstract class GreedyCommunityDetector extends CommunityDetector{

    private Selector partitionSelector;
        
    protected List<Integer> vertexIdList = new ArrayList<Integer>();
    
    public GreedyCommunityDetector(Network network, String name) {
        super(network, name);
        partitionSelector = new Selector(2);
    }
    
    public GreedyCommunityDetector(Network network, String name, int numPartitions) {
        super(network, name);
        partitionSelector = new Selector(numPartitions);
    }

    @Override
    public void compute() {
        
        for (Vertex v : network.getAllVertex()) {
            int key = (int) v.getId();
            vertexIdList.add(key);
        }
        
        Random rand = new Random();
        
        for (int i = 0; i < partitionSelector.size(); i++) {
            long index = rand.nextInt(network.getNumberOfNodes());
            Vertex vertex = network.getVertex(index);
            vertex.setPartition(partitionSelector.next());
            network.updateVertex(vertex);
        }
        
        while (!vertexIdList.isEmpty()) {
        
            int part = partitionSelector.next();
            
            Vertex nextVertex = selectVertex(part);
            nextVertex.setPartition(part);
            network.updateVertex(nextVertex);
                       
        }
        
    }
    
    protected abstract Vertex selectVertex(int part);
    
    private class Selector{
        
        private int selected;
        private List<Integer> list;
        
        public Selector(int numElements){
            list = new ArrayList<>();
            for (int i = 0; i < numElements; i++) {
                list.add(i+1);
            }
        }
        
        public int next(){
            selected++;
            if (selected>=list.size()){ 
                selected = 0;
            }
            return list.get(selected);
        }
        
        public int atual(){
            return list.get(selected);
        }
        
        public int size(){
            return list.size();
        }
    }

}
