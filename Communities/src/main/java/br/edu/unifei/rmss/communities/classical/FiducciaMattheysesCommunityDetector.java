/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.communities.classical;

import br.edu.unifei.rmss.communities.CommunityDetector;
import br.edu.unifei.rmss.graph.Network;
import br.edu.unifei.rmss.graph.Vertex;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.util.Pair;

/**
 *
 * @author aluno
 */
public class FiducciaMattheysesCommunityDetector extends CommunityDetector{

    /**
     * Number of iteration retries if there was no exchange in the iteration (G <= 0) 
     */
    private static int MAX_NUMBER_OF_RETIES = 5;

    private static int MAX_NUMBER_OF_ITERATIONS = 20;
	
    // Ratio between partitions to be used as the balance condition.
    private double ratio;

    private double F1;
    private double F2;

    //Weight of the largest cell in the network
    private int SMAX = 1;
    
    public FiducciaMattheysesCommunityDetector(Network network) {
        super(network, "FM");
		
        this.ratio = -1D;;
		
        int sizeNodes = network.getNumberOfNodes();
	SMAX = (int) (sizeNodes * 0.05);//5 porcento do tamanho total de vértices
	F1 = ratio * sizeNodes / 2 - SMAX;
	F2 = ratio * sizeNodes / 2 + SMAX;
    }

    @Override
    public void compute() {

        // Iniciar o particionamento
        startWithArbitraryPartition();
        
        int bestCut = network.getCutWeight();
        
        int it = 0;
        int cut = 0;
        int bestIt = 0;
        int retries = 0;

        while(retries < MAX_NUMBER_OF_RETIES && it <= MAX_NUMBER_OF_ITERATIONS) {
            
            it++;

            cut = fmIteration(it);

            // se não houve trocas
            if (cut == -1) {
                retries++;
                if(retries < MAX_NUMBER_OF_RETIES) {
                    // Reiniciar o particionamento
                    startWithArbitraryPartition();
                }
            } else {
                // usa a mesma partição atual, como sendo a partição inicial do passo seguinte
                if (cut < bestCut) {
                    // armazena o melhor resultado
                    bestIt = it;
                    bestCut = cut;
                }
            }
        }
        
    }
        
    private int fmIteration(int itNumber) {
		
        /**
         * 1. Gather input data and store into the relevant structures. <br>
         * 2. Compute gains of all cells <br>
         * 3. i = 1, Select base cell ci that has <br>
         * (i) max. gain, <br>
         * (ii) satisfies balance criterion. If tie, Then use size criterion or internal connections. If no
         * base cell, then EXIT; <br>
         * 4. Lock ci; update gains of cells of affected critical nets <br>
         * 5. If free cell is not-empty, Then i = i+1; select next base cell and go to step 3. <br>
         * 6. Select best sequence of moves c1, c2, ..., ck; (1<=k <= i) such that G(k) = is maximum. If tie,
         * then choose subset that achieves a superior performance. If G <= 0, Then EXIT <br>
         * 7. Make all i moves permanent; Free all cells; Goto step 1.
         */

        // Brauer's steps 1, 2 and 3 are not necessary as we are not using hypergraphs

        // Flag to determine if the partitioning is finished
        // boolean stop = false;

        // Brauer's Step 4
        // Initially divide the graph into two partitions to be refined later.
        // setInitialPartitioning();

        // Begin FM algorithm

        // Brauer's Step 5
        // Calculate the initial gains of the nodes.

        
        
    
        List<Pair> gainMap = new ArrayList<Pair>();
        List<Pair> moveMap = new ArrayList<Pair>();
        
        
        //Calculate Inicial gains
        gainMap = network.gainOfAllVertex();

        while(!gainMap.isEmpty()) {
            // Brauer's Step 6
            // Brauer's Step 7


            //Select best vertex to move
            Vertex v = null;
                
            int gain = 0;
            long id = 0; 

            for (int i = 0; i<gainMap.size(); i++) {
                
                gain = (int) gainMap.get(i).getValue();
                id = (long)gainMap.get(i).getKey();

                //Verifica o valor do ganho
                if(gain == 0) {
                    break;
                }
                
                //Verifica a condição de balanceamento
                v = network.getVertex(id);
                Iterable<Vertex> elements = network.getPartitionVertex(v.getPartition());
                int partitionSize = 0;
                for (Vertex vert : elements) {
                    partitionSize += vert.getWeight();                
                }

                if (F1 <= partitionSize && partitionSize <= F2) {
                    gainMap.remove(i);
                    break;
                } else {
                    v = null;
                }
                
            }

            //V é o vértice que atende as exigências

            if (v != null){
                
                Pair p = new Pair(id, gain);
                moveMap.add(p);

                // Brauer's Step 8
                updateGainOfAffectedNodes(v.getId(),gainMap);

                // Brauer's Step 9
            } else {
                break;
            }
        }
        // Brauer's Step 10
        // Brauer's Step 11
        // Brauer's Step 12

        
        //Realiza as movimentações
        int partialGain = 0;
        int maxGain = 0;
        int maxMove = -1;
        int k = 0;

        for (Pair move : moveMap) {
            k++;
            partialGain += (int)move.getValue();
            if (partialGain > maxGain) {
                maxGain = partialGain;
                maxMove = k;
            }
        }

        if (maxMove > 0) {
                // Brauer's Step 11

            for (Pair move : moveMap) {
                Vertex v = network.getVertex((long)move.getKey());
                int newPartition = (v.getPartition() == 1) ? 0 : 1;
                v.setPartition(newPartition);
                network.updateVertex(v);
            }
        }
        
        
        int cutWeight = network.getCutWeight();

        // Brauer's Step 12	
        return cutWeight;
    }
    
    private void startWithArbitraryPartition(){
        Random rand = new Random();
        for (Vertex v : network.getAllVertex()) {
            if (rand.nextBoolean()){
                v.setPartition(1);
                network.updateVertex(v);
            } else {
                v.setPartition(2);
                network.updateVertex(v);
            }
            
        }
    }
    
    private void updateGainOfAffectedNodes(long nodeId, List<Pair> gainMap) { 
               
        // Criou-se uma lista com ID/(GANHO,INDICE_GAINMAP)
        Map<Long,Pair> gainList = new HashMap<>();        
        for (int i = 0; i< gainMap.size() ; i++) {
            Pair original = gainMap.get(i);            
            Pair p = new Pair((int)original.getValue(), i);
            gainList.put((long)p.getKey(), p);
        }
        
        //Busca o próprio nó
        Vertex v = network.getVertex(nodeId);        
        Iterator<Long> allNeighbors = network.getIdFromNeighbor(v);
        
        //Atualiza seus vizinhos
        for (Iterator it = allNeighbors; it.hasNext();) {
            
            long neighborId = (long)it.next();
            
            //Se o vizinho está na lista corrente
            if (gainList.containsKey(neighborId)){
                
                Pair p = gainList.get(neighborId);                
                int neighborGain = (int)p.getKey();   
                int neighborIndice = (int)p.getValue();                
                int weight = network.getEdge(neighborId, nodeId).getWeight();
                int neighborPartition = network.getVertex(neighborId).getPartition();                
                
                int newPartition = (v.getPartition() == 1) ? 0 : 1;
                int newNeighborGain = 0;
                if (newPartition == neighborPartition) {
                    newNeighborGain = neighborGain - 2 * weight; 
                } else { 
                    newNeighborGain = neighborGain + 2 * weight;
                }
                
                gainMap.set(neighborIndice, new Pair(neighborId, newNeighborGain));
                
            }
        }
    }
	
	
}
