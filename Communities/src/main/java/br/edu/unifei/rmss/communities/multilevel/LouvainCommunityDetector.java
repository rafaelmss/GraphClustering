/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.communities.multilevel;

import br.edu.unifei.rmss.graph.Edge;
import br.edu.unifei.rmss.graph.Network;
import br.edu.unifei.rmss.graph.Vertex;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rafael
 */
public class LouvainCommunityDetector extends MultilevelCommunityDetector{

    public LouvainCommunityDetector(Network network) {
        super(network,"Louvain");
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
                
                Vertex neighbor = nodeWithMoreModularityGain(vertex);
            
                
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

            Vertex vc1 = network.getCoarsenVertex(v1.getId(), v1.getLevel());
            Vertex vc2 = network.getCoarsenVertex(v2.getId(), v2.getLevel());

            
            if (vc1.getId() != vc2.getId()){
                
                Edge existEdge = network.getEdge(vc1.getId(), vc2.getId(), level);
                if (existEdge != null){
                    
                    existEdge.sumWeight(edge.getWeight());
                    network.updateEdge(existEdge);
                    
                    
                } else {
                    
                    network.createEdge(1, vc1.getId(), vc2.getId(), level);
                }
                
                

            } else {
                vc1.setWeight(vc1.getWeight()+1);
                network.updateVertex(vc1);
            }
            
        }   
        
        
        int numNodes = network.getNumberOfNodes(level);
        int numNodesOld = network.getNumberOfNodes(level-1);
        
        if (numNodes == 2){
            return false;
        }
        
        if (numNodes == numNodesOld){
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
    
    public double computeGainModularity(Vertex node1, Vertex node2) {

        int originalPartition = node1.getPartition();
        node1.setPartition(node2.getPartition());
        network.updateVertex(node1);
        double modularity = computeModularity(node1);
        node1.setPartition(originalPartition);
        network.updateVertex(node1);

        return modularity;

    }

    //Computa a modularidade do nó no grafo
    private double computeModularity(Vertex node) {

        int vertexPartition = node.getPartition();
        int vertexLevel = node.getLevel();
        
        double sum = 0;

        double m2 = (double) (2 * (network.getNumberOfEdges(vertexLevel)));

        double s_in = 0;    //soma dos pesos das arestas de dentro da particao
        double s_tot = 0;   //soma dos pesos das arestas que incidem na particao

        double k_i = 0;     //soma dos pesos das arestas que incidem no nó I
        double k_iin = 0;   //soma dos pesos das arestas que incidem no nó I originarios da mesma partição

        for (Vertex v : network.getPartitionVertex(vertexPartition,vertexLevel)) {
            
            for (Edge edge : network.getEdgesFromNode(node)) {

                Vertex otherNode = edge.getEndNode();

                //Se os dois estão na mesma partição
                if (v.getPartition() == otherNode.getPartition()) {
                    s_in = s_in + edge.getWeight();
                } else {
                    s_tot = s_tot + edge.getWeight();
                }

                //Se o nó analisado é o Node                    
                if (v.getId() == node.getId()) {
                    if (otherNode.getPartition() == vertexPartition) {
                        k_iin = k_iin + edge.getWeight();
                        k_i = k_i + edge.getWeight();
                    } else {
                        k_i = k_i + edge.getWeight();
                    }
                }

            }
        }
        
        s_in = s_in / 2; //na mesma particao é contato em dobro
                
        return ( ((s_in + k_iin) / m2 - Math.pow(((s_tot + k_i) / m2), 2)) - ((s_in / m2) - Math.pow((s_tot / m2), 2) - Math.pow((k_i / m2), 2)) );
    }   
    
    public Vertex nodeWithMoreModularityGain (Vertex v) {
        
        Vertex otherVertex = null;
        double gain = 0;
        
        for (Edge edge : network.getEdgesFromNode(v)) {

            Vertex analysis = edge.getEndNode();
            if (analysis.getId() == v.getId() ){
                analysis = edge.getStartNode();
            }
            
            if (network.getCoarsenVertex(analysis.getId(), analysis.getLevel()) == null){
            
                double new_gain = computeGainModularity(v, analysis);
            
                if (new_gain > gain) {
                    gain = new_gain;
                    otherVertex = analysis;
                }
            
            }

        }
        
        return otherVertex;
        
    }

}
