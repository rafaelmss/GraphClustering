/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.communities.agglomerative;

import br.edu.unifei.rmss.graph.Network;
import javafx.util.Pair;

/**
 *
 * @author aluno
 */
public class CompleteLinkCommunityDetector extends AgglomerativeCommunityDetector{

    public CompleteLinkCommunityDetector(Network network) {
        super(network, "CompleteLink");
    }

    @Override
    protected Pair<Integer,Integer> findPartitionToMerge() {
        return network.getMaxDistPartitions();
    }
    
}
