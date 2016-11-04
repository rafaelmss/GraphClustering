/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.communities.greedy;

import br.edu.unifei.rmss.graph.Network;
import br.edu.unifei.rmss.graph.Vertex;
import java.util.Random;

/**
 *
 * @author rafael
 */
public class StandardGreedyCommunityDetector extends GreedyCommunityDetector{

    public StandardGreedyCommunityDetector(Network network) {
        super(network, "StandardGreedy");
    }

    @Override
    protected Vertex selectVertex(int part) {
        
        Vertex vertex = null;
        int pos = -1;
        
        for (Vertex v : network.getPartitioFrontier(part)) {
            vertex = v;
            pos = vertexIdList.indexOf((int)vertex.getId());            
            break;
        }
        
        if (vertex == null){
            Random rand = new Random();
            pos = rand.nextInt(vertexIdList.size());
            long vertexId = vertexIdList.get(pos);
            vertex = network.getVertex(vertexId);
        }
        
        vertexIdList.remove(pos);
        
        return vertex;        
    }
    
}
