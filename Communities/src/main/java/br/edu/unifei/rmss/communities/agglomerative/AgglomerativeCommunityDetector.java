/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.communities.agglomerative;

import br.edu.unifei.rmss.communities.CommunityDetector;
import br.edu.unifei.rmss.graph.Network;
import br.edu.unifei.rmss.graph.Vertex;
import java.util.Iterator;
import java.util.List;
import javafx.util.Pair;
import org.apache.commons.collections.IteratorUtils;

/**
 *
 * @author aluno
 */
public abstract class AgglomerativeCommunityDetector extends CommunityDetector {

    /**
     * Constructor.
     */
    public AgglomerativeCommunityDetector(Network network, String name) {
        super(network, name);
    }

    @Override
    public void compute() {
        
        // Inicializate
        // Add only one node eatch partition
        int part = 1;
        for (Vertex v : network.getAllVertex()) {
            v.setPartition(part);
            network.updateVertex(v);
            part++;
        }

        // Iterate
        while (true) {
                                    
            // If num of partitions is 2, go to end
            if (network.getNumberOfPartitions() == 2){
                break;
            }
            
            // Find partitions to marge
            Pair<Integer, Integer> mPart = findPartitionToMerge();
            int part1 = mPart.getKey();
            int part2 = mPart.getValue();
            
            //Merge partition Part1 and Part2
            if ((part1 != -1) && (part2 != -1)){
                for (Vertex v : network.getPartitionVertex(part2)) {
                    v.setPartition(part1);      
                    network.updateVertex(v);
                }
            }
        }
    }

    protected abstract Pair<Integer, Integer> findPartitionToMerge();
    
}