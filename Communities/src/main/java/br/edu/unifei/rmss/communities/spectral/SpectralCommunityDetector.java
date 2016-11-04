/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.communities.spectral;

import br.edu.unifei.rmss.communities.CommunityDetector;
import static br.edu.unifei.rmss.communities.CommunityDetector.distance;
import br.edu.unifei.rmss.graph.Network;
import br.edu.unifei.rmss.math.matrix.Matrix;
import br.edu.unifei.rmss.math.matrix.file.MatrixFile;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rafael
 */
public abstract class SpectralCommunityDetector extends CommunityDetector{
    
    public SpectralCommunityDetector(Network network, String name) {
        super(network, name);
    }
        
    protected double[] kMeans(Matrix E, int K, double msse) {

        try {
            int elements = E.rows();
            int attrs = E.columns();
            double max = E.max();
            double min = E.min();

            // Init centroids
            Matrix CE = new MatrixFile(K, attrs);
            Matrix NCE = new MatrixFile(K, attrs);
            for (int i = 0; i < CE.rows(); ++i) {
                for (int j = 0; j < CE.columns(); ++j) {
                    // Random initialization
                    CE.set(i, j, min + rng.nextDouble() * (max - min));
                }
            }

            // Assign elements to cluster
            double[] CL = new double[elements];
            double[] NCL = new double[elements];
            // No elements in clusters
            double[] NIC = new double[K];
            // Stop criteria
            Double minSSE = Double.MAX_VALUE;
            Boolean salir = false;
            while (!salir) {
                // Init vars for recalculate centroides
                for (int i = 0; i < NCE.rows(); ++i) {
                    for (int j = 0; j < NCE.columns(); ++j) {
                        NCE.set(i, j, 0);
                    }
                    NIC[i] = 0;
                }

                int centroideAsigned = -1;
                double SSE = 0;
                // Foreach element, asign to the cluster centroide which is nearest
                for (int element = 0; element < elements; ++element) {
                    Double distMin = Double.MAX_VALUE;
                    for (int centroid = 0; centroid < CE.rows(); ++centroid) {
                        // Distance between element and centroid c
                        double dist = distance(E, element, CE, centroid);
                        if (dist < distMin) {
                            // Keep minimun distance and asign the element to cluster
                            distMin = dist;
                            NCL[element] = centroid;
                            centroideAsigned = centroid;
                        }
                    }
                    // For recalculate centroides
                    for (int i = 0; i < NCE.columns(); ++i) {
                        NCE.set(centroideAsigned, i, NCE.get(centroideAsigned, i) + E.get(centroideAsigned, i));
                    }
                    NIC[centroideAsigned] = NIC[centroideAsigned] + 1;
                    // Calculating SSE
                    SSE += distMin * distMin;
                }
                // Recalculate centroides
                for (int i = 0; i < CE.rows(); ++i) {
                    for (int j = 0; j < CE.columns(); ++j) {
                        if (NIC[i] != 0) {
                            CE.set(i, j, NCE.get(i, j) / NIC[i]);
                        } else {
                            CE.set(i, j, min + rng.nextDouble() * (max - min));
                        }
                    }
                }

                // Save min SSE and check stop criteria
                if (minSSE - msse > SSE) {
                    minSSE = SSE;
                    // Backup last asign
                    CL = NCL;
                } else {
                    if (minSSE > SSE) // Backup last asign
                    {
                        CL = NCL;
                    }
                    salir = true;
                }

            }

            return CL;
        } catch (IOException ex) {
            Logger.getLogger(UKMeansCommunityDetector.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
}
