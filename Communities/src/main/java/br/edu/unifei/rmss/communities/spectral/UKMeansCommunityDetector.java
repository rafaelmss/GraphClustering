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
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rafael
 */
public class UKMeansCommunityDetector extends SpectralCommunityDetector {

    private int K;

    public UKMeansCommunityDetector(Network network) {
        this(network, (int) Math.sqrt(network.getNumberOfNodes()));
    }

    public UKMeansCommunityDetector(Network network, int clusters) {
        super(network, "UKMeans");
        K = clusters > 0 ? clusters : 2;
    }

    @Override
    public void compute() {

        int size = network.getNumberOfNodes();
        Matrix L;
        try {
            L = new MatrixFile(size, size);
            for (int i = 0; i < size; i++) {
                double value = network.getDegreeOfNode(i);
                L.set(i, i, value);
                Vertex v = network.getVertex(i);
                Iterator<Long> neighbor = network.getIdFromNeighbor(v);
                for (Iterator<Long> iterator = neighbor; iterator.hasNext();) {
                    long id = iterator.next();
                    L.set(i, (int) id, -1);
                }
            }

            EigenvectorDecomposition ed = new EigenvectorDecompositionFile(L);
            Matrix eigenvectors = ed.getEigenvectorMatrix();

            // K-means algorithm 
            double[] CL = kMeans(eigenvectors, K, 1e10);

            // Save results        
            for (int i = 0; i < CL.length; ++i) {
                Vertex v = network.getVertex(i);
                int partition = (int) Math.abs(CL[i]);
                v.setPartition(partition);
                network.updateVertex(v);
            }

        } catch (IOException ex) {
            Logger.getLogger(UKMeansCommunityDetector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
