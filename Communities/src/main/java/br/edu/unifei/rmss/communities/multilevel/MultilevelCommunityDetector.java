/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.communities.multilevel;

import br.edu.unifei.rmss.communities.CommunityDetector;
import br.edu.unifei.rmss.graph.Network;
import br.edu.unifei.rmss.graph.Vertex;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

/**
 *
 * @author aluno
 */
public abstract class MultilevelCommunityDetector extends CommunityDetector{
    
    protected int level = 1;
    protected int frontierSize = 1;

    public MultilevelCommunityDetector(Network network, String name) {
        super(network, name);
    }
    
    @Override
    public void compute(){
        
        preprocess();                
        while (coarseningInteration()){
            level++;
        };
        setPartitions();
        network.uncoarsen();
    }

    protected abstract boolean coarseningInteration();
    
    protected abstract void setPartitions();
    
    protected abstract void preprocess();
   
}
