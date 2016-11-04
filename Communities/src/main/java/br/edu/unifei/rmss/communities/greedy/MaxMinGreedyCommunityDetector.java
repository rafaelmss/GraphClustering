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
public class MaxMinGreedyCommunityDetector extends GreedyCommunityDetector{

    public MaxMinGreedyCommunityDetector(Network network) {
        super(network, "MaxMinGreedy");
    }

    @Override
    protected Vertex selectVertex(int part) {
        
        int otherPart = 2;
        if (part == 2) otherPart = 1;
        
        Vertex vertex = null;
        int pos = -1;        
        
        int minMax = Integer.MIN_VALUE;
        
        for (Vertex v : network.getPartitioFrontierSortedByWight(part)) {
            
            int part1 = network.getVertexFrontierWeight(v.getId(), part);
            int part2 = network.getVertexFrontierWeight(v.getId(), otherPart);
            int diff = part1-part2;
            
            if (diff >= minMax){
                vertex = v;
                minMax = diff; 
            }
        }
        
        if (vertex == null){
            Random rand = new Random();
            pos = rand.nextInt(vertexIdList.size());
            long vertexId = vertexIdList.get(pos);
            vertex = network.getVertex(vertexId);
        } else {
            pos = vertexIdList.indexOf((int)vertex.getId()); 
        }
        
        vertexIdList.remove(pos);
        
        return vertex;        
    }
    
}
