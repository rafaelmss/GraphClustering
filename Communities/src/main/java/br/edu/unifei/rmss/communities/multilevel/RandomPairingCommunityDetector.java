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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author rafael
 */
public class RandomPairingCommunityDetector extends MultilevelCommunityDetector{

    public RandomPairingCommunityDetector(Network network) {
        super(network,"RandomPairing");
    }

    @Override
    protected boolean coarseningInteration() {
        
        Map<Integer,Boolean> vertexIdList = new HashMap<Integer,Boolean>();
        int id = 0;
        
        for (Vertex v : network.getAllVertex(level-1)) {
            int key = (int) v.getId();
            vertexIdList.put(key, true);            
        }
        
        //Coarsen Vertex
        for (int i = 0; i < vertexIdList.size(); i++) {
            
            if (vertexIdList.get(i)){
                
                Vertex vertex = network.getVertex(i, level-1);
                
                Vertex neighbor = selectedNode(vertex);
                
                if (neighbor != null) {
                        
                    boolean created = false;

                    Vertex coarsen = network.getCoarsenVertex(neighbor.getId(), level-1);
                    if (coarsen == null){
                        coarsen = network.createVertex(id, 1, level, neighbor.getId());
                        created = true;
                    }                        
                    coarsen.setPartition(vertex.getPartition());
                    network.updateVertex(coarsen);

                    network.createCoarsenEdge(vertex.getId(), coarsen.getId(), level);

                    if (created) {
                        id++;
                    }
                    //Lock v1 e v2
                    vertexIdList.replace((int)vertex.getId(), false);
                    vertexIdList.replace((int)neighbor.getId(),false);

                }
            }
        }        
        
        //Transpose remaining nodes
        for (int i = 0; i < vertexIdList.size(); i++) {
            
            if (vertexIdList.get(i)) {

                Vertex vertex = network.getVertex(i, level-1);
                    
                Vertex c = network.createVertex(id, vertex.getWeight(), level, vertex.getId());
                c.setPartition(vertex.getPartition());
                network.updateVertex(c);
                id++;
            }               
        }
        
        //Transpose edges
        for (Edge edge : network.getAllEdges(level-1)) {
                
            Vertex v1 = edge.getStartNode();
            Vertex v2 = edge.getEndNode();

            Vertex vc1 = network.getCoarsenVertex(v1.getId(), level-1);
            Vertex vc2 = network.getCoarsenVertex(v2.getId(), level-1);
            
            long id1 = vc1.getId();
            long id2 = vc2.getId();

            if (id1 != id2){

                Edge existEdge = network.getEdge(id1, id2, level);
                if (existEdge != null){
                    existEdge.sumWeight(edge.getWeight());
                    network.updateEdge(existEdge);
                } else {
                    network.createEdge(1, id1, id2, level);
                }

            } else {
                vc1.setWeight(vc1.getWeight()+1);
                network.updateVertex(vc1);
            }
        }   
        
        
        int numNodes = network.getNumberOfNodes(level);
        int numNodesOld = network.getNumberOfNodes(level-1);
        
        if ((numNodes == numNodesOld) || (network.getNumberOfNodes(level) == 2)){
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void preprocess() {
        int partition = 1;
        for (Vertex vertex : network.getAllVertex()) {
            vertex.setPartition(partition);
            network.updateVertex(vertex);
            partition++;
        }
    }

    @Override
    protected void setPartitions() {
        int partition = 1;
        if (level == 0){
            for (Vertex vertex : network.getAllVertex()) {
                vertex.setPartition(partition);
                network.updateVertex(vertex);
                partition++;
            }
        } else {
            for (Vertex vertex : network.getAllVertex(this.level)) {
                vertex.setPartition(partition);
                network.updateVertex(vertex);
                partition++;
            }
        }
    }
    
    public Vertex selectedNode (Vertex v) {
        
        Vertex otherVertex = null;
        
        List<Long> neighborIdList = new ArrayList<Long>();
        for (Iterator it = network.getIdFromNeighbor(v); it.hasNext();) {
            long next = (long)it.next();
            if (network.getCoarsenVertex(next, level-1) == null){
                neighborIdList.add(next);
            }
        }
        
        int size = neighborIdList.size();
        
        if (size == 0){
            return null;
        } else {
            Random rand = new Random();
            long selectedId = neighborIdList.get(rand.nextInt(size));
            return network.getVertex(selectedId, level-1);
        }
        
    }
   
}
