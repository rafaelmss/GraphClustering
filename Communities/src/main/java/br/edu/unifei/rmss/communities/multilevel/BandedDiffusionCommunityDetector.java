/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.communities.multilevel;

import br.edu.unifei.rmss.graph.Edge;
import br.edu.unifei.rmss.graph.Network;
import br.edu.unifei.rmss.graph.Vertex;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author aluno
 */
public class BandedDiffusionCommunityDetector extends MultilevelCommunityDetector{

    private Vertex hook1;
    private Vertex hook2;
       
    private Map<Long,Integer> old;
    
    private int IN_VALUE = 10;

    public BandedDiffusionCommunityDetector(Network network) {
        super(network, "BandedDiffusion");
    }

    @Override
    protected boolean coarseningInteration() {
        //Aplica o particionamento
        
        List<Integer> newValues = new ArrayList<Integer>();
        for (int i = 0; i < network.getNumberOfNodes(); i++) {
            newValues.add(0);
        }
                
        while (network.getNumVertexWithoutPartition() > 0) {
            
            old.replace(hook1.getId(), old.get(hook1.getId()) + IN_VALUE);
            old.replace(hook2.getId(), old.get(hook2.getId()) - IN_VALUE);
            
            for (Map.Entry<Long, Integer> entry : old.entrySet()) {
                
                long vertexId = entry.getKey();
                int content = entry.getValue();
                Vertex v = network.getVertex(vertexId);
                
                if (Math.abs(content) > v.getWeight()){
                    
                    if (content>0) {
                        content = content - v.getWeight();
                    } else {
                        content = content + v.getWeight();
                    }
                    
                    int totalWeight = network.getWeightEdges(vertexId);
                    
                    for (Edge edge : network.getEdgesFromNode(v)) {
                        
                        int flow = (content * edge.getWeight() / totalWeight); 
                        
                        long otherId = edge.getOtherNode(v).getId();                        
                        
                        newValues.set((int)otherId, newValues.get((int)otherId)+flow);                        

                    }
               
                }
                                
            }
            
            for (Map.Entry<Long, Integer> entry : old.entrySet()) {
                
                long oldId = entry.getKey();
                int newValue = newValues.get((int)oldId);
                old.replace(oldId, newValue );
                
                Vertex vert = network.getVertex(oldId);
                if (newValue > 0){
                    vert.setPartition(1);
                    network.updateVertex(vert);
                } else {
                    vert.setPartition(2);
                    network.updateVertex(vert);
                }  
                                
            }           
            
        }
        
        return false;
    }

    @Override
    protected void setPartitions() {
        
        for (Map.Entry<Long, Integer> entry : old.entrySet()) {
            Vertex key = network.getVertex(entry.getKey());
            Integer value = entry.getValue();
            
            if (value > 0){
                key.setPartition(1);
                network.updateVertex(key);
            } else {
                key.setPartition(2);
                network.updateVertex(key);
            }            
        }
        
    }

    @Override
    protected void preprocess() {
        Random rand = new Random();        
        
        int numVertex = network.getNumberOfNodes();
        long id1 = Math.abs(rand.nextLong()%numVertex);
        long id2 = Math.abs(rand.nextLong()%numVertex);
        while (id1 == id2){
            id2 = Math.abs(rand.nextLong()%numVertex);
        }
        
        hook1 = network.getVertex(id1);
        hook2 = network.getVertex(id2);
               
        old = new HashMap<Long, Integer>();
        for (Vertex v : network.getAllVertex()) {
            
            if (v.getId() == id1){
                hook1 = v;
            } else if (v.getId() == id2) {
                hook2 = v;
            }
            old.put(v.getId(), 0);
        }
    }
    
}
