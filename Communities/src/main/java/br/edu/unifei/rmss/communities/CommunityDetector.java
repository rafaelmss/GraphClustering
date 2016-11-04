/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.communities;

import br.edu.unifei.rmss.graph.Network;
import br.edu.unifei.rmss.graph.Vertex;
import br.edu.unifei.rmss.math.matrix.Matrix;
import br.edu.unifei.rmss.math.matrix.colt.MatrixColt;
import java.util.Iterator;


/**
 *
 * @author rafael
 */
public abstract class CommunityDetector {
    
    protected Network network;
    
    protected java.util.Random rng;
    
    protected String name;
    
    public CommunityDetector (Network network, String name) {
        this.network = network;
        this.rng = new java.util.Random();
        this.name = name;
    }
    
    public String getName(){
        return this.name;
    }
    
    protected void setName(String name){
        this.name = name;
    }
    
    public static double distance(Matrix NA, int element, Matrix CA, int centroid) {
        double dist = 0;
        for (int value = 0; value < NA.columns() && value < CA.columns(); ++value) {
            dist += (CA.get(centroid, value) - NA.get(element, value)) * (CA.get(centroid, value) - NA.get(element, value));
        }

        return Math.sqrt(dist);
    }
    
    public abstract void compute();
    
}
