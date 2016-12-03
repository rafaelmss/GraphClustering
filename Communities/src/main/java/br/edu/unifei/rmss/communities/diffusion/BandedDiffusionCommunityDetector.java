/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.communities.diffusion;

import br.edu.unifei.rmss.communities.CommunityDetector;
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
public class BandedDiffusionCommunityDetector extends CommunityDetector{

    private Vertex hook1;
    private Vertex hook2;
       
    private Map<Long,Double> old;
    
    private int IN_VALUE = 10;

    public BandedDiffusionCommunityDetector(Network network) {
        super(network, "BandedDiffusion");
    }

    @Override
    public void compute(){
        
        preProcess();           
        diffuse();
    }
    
    protected void diffuse() {
        
        List<Double> newValues = new ArrayList<Double>();
        for (int i = 0; i < network.getNumberOfNodes(); i++) {
            newValues.add(0.0);
        }
            
        while (network.getNumVertexWithoutPartition() > 0) {
                        
            old.replace(hook1.getId(), old.get(hook1.getId()) + IN_VALUE);
            old.replace(hook2.getId(), old.get(hook2.getId()) - IN_VALUE);
            
            
            boolean diffused = false;
            
            while (!diffused){
            
                diffused = true;
                
                for (Map.Entry<Long, Double> entry : old.entrySet()) {

                    long vertexId = entry.getKey();
                    double content = entry.getValue();
                    Vertex v = network.getVertex(vertexId);

                    if (Math.abs(content) > v.getWeight()){

                        diffused = false;
                        
                        if (content>0) {
                            content = content - v.getWeight();
                            entry.setValue((double)v.getWeight());
                        } else {
                            content = content + v.getWeight();
                            entry.setValue(-(double)v.getWeight());
                        }
                        
                        

                        int totalWeight = network.getWeightEdges(vertexId);

                        for (Edge edge : network.getEdgesFromNode(v)) {

                            double flow = (content * edge.getWeight() / totalWeight); 

                            long otherId = edge.getOtherNode(v).getId();                        
                            newValues.set((int)otherId, newValues.get((int)otherId)+flow);                        
                        }
                    }
                }
            }
            
            for (Map.Entry<Long, Double> entry : old.entrySet()) {
                
                long oldId = entry.getKey();
                double newValue = newValues.get((int)oldId);
                old.replace(oldId, newValue );
                
                Vertex vert = network.getVertex(oldId);
                if (newValue != 0){
                    if (newValue > 0){
                        vert.setPartition(1);
                        network.updateVertex(vert);
                    } else {
                        vert.setPartition(2);
                        network.updateVertex(vert);
                    }  
                }
                                
            }           
            
        }
    }

    protected void preProcess() {
        Random rand = new Random();        
        
        int numVertex = network.getNumberOfNodes();
        long id1 = Math.abs(rand.nextLong()%numVertex);
        long id2 = Math.abs(rand.nextLong()%numVertex);
        while (id1 == id2){
            id2 = Math.abs(rand.nextLong()%numVertex);
        }
        
        hook1 = network.getVertex(id1);
        hook2 = network.getVertex(id2);
               
        old = new HashMap<Long, Double>();
        for (Vertex v : network.getAllVertex()) {
            
            if (v.getId() == id1){
                hook1 = v;
            } else if (v.getId() == id2) {
                hook2 = v;
            }
            old.put(v.getId(), 0.0);
        }
    }
    
}
