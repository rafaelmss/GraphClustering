/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.communities.diffusion;

import br.edu.unifei.rmss.communities.CommunityDetector;
import br.edu.unifei.rmss.graph.Network;
import br.edu.unifei.rmss.graph.Vertex;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;

/**
 *
 * @author rafael
 */
public class DidicCommunityDetector extends CommunityDetector{

    
    protected LinkedHashMap<Long, ArrayList<Double>> w = new LinkedHashMap<Long, ArrayList<Double>>();
    protected LinkedHashMap<Long, ArrayList<Double>> l = new LinkedHashMap<Long, ArrayList<Double>>();

    protected Random rng = new Random();

    protected int benefitHigh = 10;
    protected int benefitLow = 1;
    protected int defClusterVal = 100;
    protected int numClusters = 2;
    
    protected int FOSBIterations = 11;
    protected int FOSTIterations = 11;
    protected int maxIterations = 150; 
    
    public DidicCommunityDetector(Network network) {
        super(network, "DiDiC");
    }    
    
    @Override
    public void compute() {
		
        initLoadVectorsAll();

        for (int timeStep = 0; timeStep < maxIterations; timeStep++) {
            
            // For Every "Partition System"
            for (int p = 0; p < numClusters; p++) {
                // For Every Node
                for (Vertex v : network.getAllVertex()) {
                    // FOS/T Primary Diffusion Algorithm
                    doFOST(v, p);
                }

            }

            updateClusterAllocationAll();
        }
    }
    
    
    
    

    private void doFOST(Vertex v, int p) {
        ArrayList<Double> lV = l.get(v.getId());
        ArrayList<Double> wV = w.get(v.getId());

        double wVC = wV.get(p);

        int vDeg = network.getDegreeOfNode(v.getId());

        for (int fostIter = 0; fostIter < FOSTIterations; fostIter++) {

            // FOS/B (Secondary/Drain) Diffusion Algorithm
            doFOSB(v, p);

            // FOS/T Primary Diffusion Algorithm
            for (Vertex vet : network.getNeighbor(v)) {
                int edgeWeight = (network.getEdge(v.getId(), vet.getId())).getWeight();
                ArrayList<Double> wU = w.get(vet.getId());

                double diff = alphaE(vet, vDeg) * edgeWeight * (wVC - wU.get(p));

                wVC = wVC - (diff / 2.0);
                wU.set(p, wU.get(p) + (diff / 2.0));
            }

            wVC = wVC + lV.get(p);
            wV.set(p, wVC);
        }

    }

    private void doFOSB(Vertex v, int p) {
        ArrayList<Double> lV = l.get(v.getId());

        double lVC = lV.get(p);
        int vDeg = network.getDegreeOfNode(v.getId());

        double bV = benefit(v, p);

        for (int fosbIter = 0; fosbIter < FOSBIterations; fosbIter++) {
            // FOS/B Diffusion Algorithm
            for (Vertex u : network.getNeighbor(v)) {
                ArrayList<Double> lU = l.get(u.getId());
                int edgeWeight = (network.getEdge(v.getId(), u.getId())).getWeight();

                double diff = alphaE(u, vDeg) * edgeWeight * ((lVC / bV) - (lU.get(p) / benefit(u, p)));

                lVC = lVC - (diff / 2.0);
                lU.set(p, lU.get(p) + (diff / 2.0));
            }
        }
        lV.set(p, lVC);
    }


    
    
    
    
    
    
    
    
    
    
    
    // NOTE Currently load is diffused EQUALLY (ignores edge weights)
    protected void diffuseLoadToNeighbours(Vertex node) {

        int neighbourCount = network.getDegreeOfNode(node.getId());
        if (neighbourCount != 0){

            ArrayList<Double> lV = l.get(node.getId());
            ArrayList<Double> wV = w.get(node.getId());

            for (int i = 0; i < numClusters; i++) {
                double lVPerNeighbour = lV.get(i) / neighbourCount;
                double wVPerNeighbour = wV.get(i) / neighbourCount;

                for (Vertex otherNode : network.getNeighbor(node)) {

                    ArrayList<Double> lVOther = l.get(otherNode.getId());
                    ArrayList<Double> wVOther = w.get(otherNode.getId());

                    lVOther.set(i, lVOther.get(i) + lVPerNeighbour);
                    wVOther.set(i, wVOther.get(i) + wVPerNeighbour);
                }
            }
        }
    }

    protected void initLoadVectorsAll() {
        for (Vertex v : network.getAllVertex()) {
            int vPart = -1;
            if (v.hasPartition()){
                vPart = v.getPartition();
            }

            ArrayList<Double> wV = new ArrayList<Double>();
            ArrayList<Double> lV = new ArrayList<Double>();

            for (int i = 0; i < numClusters; i++) {

                if (vPart == i) {
                    wV.add(new Double(defClusterVal));
                    lV.add(new Double(defClusterVal));
                    continue;
                }

                wV.add(new Double(0));
                lV.add(new Double(0));
            }

            w.put(v.getId(), wV);
            l.put(v.getId(), lV);
        }
    }

    protected void updateClusterAllocationAll() {

        int lastVisitedIndex = -1;
        int keySetSize = w.keySet().size();

        while (lastVisitedIndex < keySetSize - 1) {

            int currentIndex = -1;

            for (Long wCkey : w.keySet()) {

                currentIndex++;

                if (currentIndex < lastVisitedIndex) continue;

                lastVisitedIndex = currentIndex;

                updateClusterAllocation(wCkey);

            }
        }

    }

    protected void updateClusterAllocation(long nodeId) {

            Vertex vert = network.getVertex(nodeId);

            ArrayList<Double> lV = l.get(nodeId);
            ArrayList<Double> wV = w.get(nodeId);

            int vNewPart = -1;
            if (vert.hasPartition()){
                vNewPart = vert.getPartition();
            }

            vNewPart = allocateClusterBasic(wV, lV);
            vert.setPartition(vNewPart);
            network.updateVertex(vert);

    }

    // Optimized version. Find vDeg once only
    protected double alphaE(Vertex u, int vDeg) {
            double uDeg = network.getDegreeOfNode(u.getId());
            return 1.0 / Math.max(uDeg, vDeg);
    }

    protected double benefit(Vertex v, int p) {

        int vPart = -1;
        if (v.hasPartition()){
            vPart = v.getPartition();
        }

        if (vPart == p)
            return benefitHigh;
        else
            return benefitLow;
    }

    // Assign to cluster:
    // * Associated with highest load value
    protected int allocateClusterBasic(ArrayList<Double> wC, ArrayList<Double> lC) {
        int maxC = 0;
        double maxW = 0.0;

        for (int c = 0; c < wC.size(); c++) {

            double loadTotal = wC.get(c);

            if (loadTotal > maxW) {
                maxW = loadTotal;
                maxC = c;
            }
        }

        return maxC;
    }

    // v possui no mínimo 1 vizinho na partição p
    protected boolean intDegNotZero(Vertex v, int p) {
        for (Vertex n: network.getNeighbor(v)) {
            if (n.hasPartition()){
                if ( p == n.getPartition()){
                    return true;
                }
            }
        }
        return false;
    }
    
}
