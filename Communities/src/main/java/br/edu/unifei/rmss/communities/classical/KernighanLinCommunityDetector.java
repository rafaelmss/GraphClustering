/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.communities.classical;

import br.edu.unifei.rmss.communities.CommunityDetector;
import br.edu.unifei.rmss.graph.Network;
import br.edu.unifei.rmss.graph.Vertex;
import br.edu.unifei.rmss.math.matrix.Matrix;
import br.edu.unifei.rmss.math.matrix.colt.MatrixColt;
import br.edu.unifei.rmss.math.vector.Vector;
import br.edu.unifei.rmss.math.vector.colt.VectorColt;
import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;

/**
 *
 * @author aluno
 */
public class KernighanLinCommunityDetector extends CommunityDetector  {
    
    public KernighanLinCommunityDetector(Network network) {
        super(network, "KL");
    }

    @Override
    public void compute() {
        // --- Initialization ---
        // 2 Subgraphs with n/2 nodes
        List<Integer> A = new ArrayList<Integer>();
        List<Integer> B = new ArrayList<Integer>();
        // Backup subgraphs
        List<Integer> AA = new ArrayList<Integer>();
        List<Integer> BB = new ArrayList<Integer>();
        // Create Subgraphs
        for (int node = 0; node < network.getNumberOfNodes(); ++node) {
            if (node % 2 == 0) {
                A.add(node);
                AA.add(node);
            } else {
                B.add(node);
                BB.add(node);
            }
        }

        // Cost reduction for moving node i
        Vector D = new VectorColt(network.getNumberOfNodes());
        // Node i is locked
        List<Boolean> L = new ArrayList<Boolean>();
        // Reset other parameter
        for (int node = 0; node < network.getNumberOfNodes(); ++node) {
            D.set(node,0.0);
            L.add(false);
        }

        // Cost matrix (adjacency matrix)
        Matrix C = adjacencyMatrix(network);

        // Max Gain list
        List<Pair<Pair<Integer, Integer>, Double>> G = new ArrayList<Pair<Pair<Integer, Integer>, Double>>();
        double gmax;

        // --- Algorithm ---
        do {

            // --- Calculate D for all nodes ---
            // A nodes
            computeD(A, B, L, C, D);
            // B nodes
            computeD(B, A, L, C, D);

            // --- Iterate ---
            int locks = 0;
            do {

                // --- Find maximum gain ---
                int namg = -1, nbmg = -1;
                double mg = Double.NEGATIVE_INFINITY;
                // For all A nodes
                for (int i = 0; i < A.size(); ++i) {
                    // A node
                    int na = A.get(i);
                    // If no lock
                    if (!L.get(na)) {
                        // For all B nodes
                        for (int j = 0; j < B.size(); ++j) {
                            // B node
                            int nb = B.get(j);
                            // If no lock
                            if (!L.get(nb)) {
                                // Gain exchange na and nb
                                double g = D.get(na) + D.get(nb) - 2
                                        * C.get(na,nb);
                                if (g > mg) {
                                    mg = g;
                                    namg = i;
                                    nbmg = j;
                                }
                            }
                        }
                    }
                }

                // --- Exchange nodes with maximum gain ---
                int temp = A.get(namg);
                A.set(namg, B.get(nbmg));
                B.set(nbmg, temp);
                // Lock nodes
                L.set(A.get(namg), true);
                L.set(B.get(nbmg), true);
                locks += 2;
                // Save gain
                Pair<Integer, Integer> p = new Pair(B.get(nbmg), A.get(namg));
                G.add(new Pair(p, mg));

                // --- Update D values ---
                // A nodes
                updateD(A, B.get(nbmg), A.get(namg), L, C, D);
                // B nodes
                updateD(B, A.get(namg), B.get(nbmg), L, C, D);

            } while (locks < network.getNumberOfNodes() - 1);

            // --- Select k pair with gain g1+...+gk is maximized ---
            int k = 0;
            double g = 0;
            gmax = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < G.size(); ++i) {
                g += G.get(i).getValue();
                if (g > gmax) {
                    k = i + 1;
                    gmax = g;
                }
            }

            // --- If gain is positive swap k nodes  ---
            if (gmax > 0) {

                // Swap k first pairs of nodes
                Pair<Integer, Integer> p;
                for (int i = 0; i < k; ++i) {
                    // Get pair
                    p = G.get(i).getKey();
                    int pos1 = AA.indexOf(p.getKey());
                    int pos2 = BB.indexOf(p.getValue());
                    // Swap
                    int temp = AA.get(pos1);
                    AA.set(pos1, BB.get(pos2));
                    BB.set(pos2, temp);
                }

                // Restore backup
                A.clear();
                for (int i = 0; i < AA.size(); ++i) {
                    A.add(AA.get(i));
                }
                B.clear();
                for (int i = 0; i < BB.size(); ++i) {
                    B.add(BB.get(i));
                }

            }

            // Unlock nodes and reset D and G
            for (int i = 0; i < L.size(); ++i) {
                L.set(i, false);
                D.set(i, 0.0);
            }
            G.clear();

        } while (gmax > 0);

        // A nodes
        for (int i = 0; i < AA.size(); ++i) {
            Vertex v = network.getVertex(AA.get(i));
            v.setPartition(1);
            network.updateVertex(v);
        }
        // B nodes
        for (int i = 0; i < BB.size(); ++i) {
            Vertex v = network.getVertex(BB.get(i));
            v.setPartition(2);
            network.updateVertex(v);
        }
    }

    /**
     * Compute cost reduction (minimal cut) for moving each node of IG to EG 
     * if isn't locked
     * 
     * @pre IG union EG = V, IG intersec EG = 0
     * @param IG Group of nodes 
     * @param EG Group of nodes
     * @param L L[i] = true if node i is lock
     * @param C cost associate to the links
     * @param D Cost to compute
     *
     */
    protected void computeD(List<Integer> IG, List<Integer> EG, List<Boolean> L, Matrix C, Vector D) {
        for (int i = 0; i < IG.size(); ++i) {
            double ic = 0.0, ec = 0.0;
            // Node orig
            int no = IG.get(i);

            // If no lock
            if (!L.get(no)) {
                // Internal cost
                for (int j = 0; j < IG.size(); ++j) {
                    ic += C.get(no,IG.get(j));
                }
                // External cost
                for (int j = 0; j < EG.size(); ++j) {
                    ec += C.get(no,EG.get(j));
                }
                // Cost reduction for moving node 'no'
                D.set(no, ec - ic);
            }
        }
    }


    /**
     * Update cost reduction (minimal cut) for removing node 'in' from G
     * and add node 'en' to G if isn't locked
     * 
     * @param G Group of nodes 
     * @param in node of G to move to other group
     * @param en node of other group to move to G
     * @param L L[i] = true if node i is lock
     * @param C cost associate to the links
     * @param D Cost to update
     *
     */
    protected void updateD(List<Integer> G, int in, int en, List<Boolean> L, Matrix C, Vector D) {
        for (int i = 0; i < G.size(); ++i) {
            int node = G.get(i);
            // If no lock
            if (!L.get(node)) {
                double d = D.get(node);
                // Update Di value
                d += 2 * C.get(node,in) - 2 * C.get(node,en);
                // Cost reduction for moving node 'node'
                D.set(node, d);
            }
        }
    }
    
    public static Matrix adjacencyMatrix (Network net) {
        double value = 1;
        int size = net.getNumberOfNodes();
        Matrix W = new MatrixColt(size, size);

        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                if (net.edgeExist(i, j)) {// If link exists
                    W.set(i, j, value);
                }
            }
        }
        
        return W;
    }
}
