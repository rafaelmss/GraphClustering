/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.communities.spectral;

import br.edu.unifei.rmss.communities.CommunityDetector;
import br.edu.unifei.rmss.graph.Network;
import br.edu.unifei.rmss.graph.Vertex;
import br.edu.unifei.rmss.math.matrix.EigenvectorDecomposition;
import br.edu.unifei.rmss.math.matrix.Matrix;
import br.edu.unifei.rmss.math.matrix.file.EigenvectorDecompositionFile;
import br.edu.unifei.rmss.math.matrix.file.MatrixFile;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rafael
 */
public class EigenCommunityDetector extends SpectralCommunityDetector{
    
    public enum ThresholdType {
        // 0.0
        ZERO, 
        // Fiedler Eigenvector average
        AVG, 
        // Fiedler Eigenvector median
        MEDIAN,
        // Max distance of elements in the Fiedler Eigenvector
        GAP
    };
    
    ThresholdType threshold;
    
    public EigenCommunityDetector(Network network) {
        super(network, "EigenZero");
        threshold = ThresholdType.ZERO;
    }

    public EigenCommunityDetector(Network network, ThresholdType type) {
        super(network, "Eigen");
        threshold = type;
        setName(composeName(type));
    }
   
    private String composeName(ThresholdType type){
        String name = "Eigen";
        
        switch (type) {
            // Average value
            case AVG:
                name = name + "AVG";
                break;
            // Median value
            case MEDIAN:
                name = name + "MEDIAN";
                break;
            // Max gap value
            case GAP:
                name = name + "GAP";
                break;
            default:
                name = name + "ZERO";
        }
        
        return name;
    }
    
    @Override
    public void compute() {
        
        try {
            
            int size = network.getNumberOfNodes();
            Matrix L = new MatrixFile(size, size);
            
            for (int i=0; i<size; i++) {
                double value = network.getDegreeOfNode(i);
                L.set(i, i, value);
                Vertex v = network.getVertex(i);
                Iterator<Long> neighbor = network.getIdFromNeighbor(v);
                for (Iterator<Long> iterator = neighbor; iterator.hasNext();) {
                    long id = iterator.next();
                    L.set(i, (int)id, -1);
                }
            }
            
            EigenvectorDecomposition ed = new EigenvectorDecompositionFile(L);
            double[] fiedler = ed.getFiedlerVector();
            
            double th = threshold(fiedler, threshold);
            partitioning(fiedler, 1, 2, th);
        } catch (IOException ex) {
            Logger.getLogger(EigenCommunityDetector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private void partitioning (double[] eigenvector, int cluster1, int cluster2, double th) {
        int size = eigenvector.length;
        // If value is less than threshold, asign cluster 1, else asign cluster 2
        for (int i = 0; i < size; ++i) {
            if (eigenvector[i] <= th) {
                Vertex v = network.getVertex(i);
                v.setPartition(cluster1);
                network.updateVertex(v);
            } else {
                Vertex v = network.getVertex(i);
                v.setPartition(cluster2);
                network.updateVertex(v);
            }
        }
    }
    
    private double threshold (double[] E, ThresholdType th) {
        double value = 0.0;

        // Type
        switch (th) {
            // Average value
            case AVG:
                value = average(E);
                break;
            // Median value
            case MEDIAN:
                value = median(E);
                break;
            // Max gap value
            case GAP:
                value = maxDistance(E);
                break;
            default:
                value = 0.0;
        }

        return value;
    }
    
    private double average(double[] v){
        
        int size = v.length;
        int sum = 0;
        for (int i = 0; i < size; i++) {
            sum += v[i];
        }
	return sum/size;
        
    }
        
    private double median(double[] v){
        
        double[] list = v;        
        Arrays.sort(list);
        double value = list[Math.round(list.length/2)];
        return value;
        
    }
        
    private double maxDistance(double[] v){
        
        double value = 0;
        double[] list = v;        
        Arrays.sort(list);
        int pos = -1;
        for (int i = 0; i < list.length - 1; ++i) {
            if (Math.abs(list[i] - list[i+1]) > value) {
                value = Math.abs(list[i] - list[i+1]);
                pos = i + 1;
            }
        }
        if (pos != -1) {
            value = list[pos];
        }
        return value;
        
    }
    
    
}
