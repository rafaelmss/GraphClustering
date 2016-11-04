package br.edu.unifei.rmss.graphgenerator;

import br.edu.unifei.rmss.graphgenerator.generators.BaseGenerator;
import br.edu.unifei.rmss.graphgenerator.generators.ClusterGenerator;
import br.edu.unifei.rmss.graphgenerator.generators.FlowerGenerator;
import br.edu.unifei.rmss.graphgenerator.generators.GridGenerator;
import br.edu.unifei.rmss.graphgenerator.generators.LobsterGenerator;
import java.util.ArrayList;
import java.util.List;

public class MainTest {

    public static void main(String[] args) {

        List<Integer> sizes = new ArrayList<Integer>();
        
        sizes.add(100);
        sizes.add(200);
        sizes.add(300);
        sizes.add(400);
        sizes.add(500);
        sizes.add(600);
        sizes.add(700);
        sizes.add(800);
        sizes.add(900);
        sizes.add(1000);
        
        for (int i=0; i<sizes.size(); i++){
            BaseGenerator graph = new ClusterGenerator(sizes.get(i));
            graph.generate();        
            graph.exportGraph("cluster_"+sizes.get(i));
        }
        
        /*
        for (int i=0; i<sizes.size(); i++){
            BaseGenerator graph = new FlowerGenerator(sizes.get(i));
            graph.generate();        
            graph.exportGraph("flower_"+sizes.get(i));
        }
        
        for (int i=0; i<sizes.size(); i++){
            BaseGenerator graph = new GridGenerator(sizes.get(i));
            graph.generate();        
            graph.exportGraph("grid_"+sizes.get(i));
        }
        
        for (int i=0; i<sizes.size(); i++){
            BaseGenerator graph = new LobsterGenerator(sizes.get(i));
            graph.generate();        
            graph.exportGraph("lobster_"+sizes.get(i));
        }
        */
        
    }

}
