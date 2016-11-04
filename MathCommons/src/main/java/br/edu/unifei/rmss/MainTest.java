/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss;

import br.edu.unifei.rmss.math.matrix.Matrix;
import br.edu.unifei.rmss.math.matrix.colt.MatrixColt;
import br.edu.unifei.rmss.math.matrix.file.MatrixFile;
import br.edu.unifei.rmss.math.vector.Vector;
import br.edu.unifei.rmss.math.vector.colt.VectorColt;
import br.edu.unifei.rmss.math.vector.file.VectorFile;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author rafael
 */
public class MainTest {

    public static void main(String[] args) throws IOException {
        
        double[] v = {1,2,4,5,6,7,8,9,10};
        
        Vector v1 = new VectorColt(v);
        Vector v2 = new VectorFile(v);
        
        printVector(v1);
        printVector(v2);
        
        double[][] m = {{1, 2, 3},
                        {4, 5, 6},
                        {7, 8, 9}};
        
        Matrix m1 =  new MatrixColt(m);
        Matrix m2 =  new MatrixFile(m);
        
        printMatrix(m1);
        printMatrix(m2);
         
    }
    
    public static void printVector(Vector v) {
        System.out.println("\nVector values: ");
        for (int i = 0; i < v.size(); i++) {
            System.out.print(" "+v.get(i));
        }
        System.out.println("");
    }
    
    public static void printMatrix(Matrix m) {
        System.out.println("\nMatrix values: ");
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.columns(); j++) {
                System.out.print(" "+m.get(i, j));
            }
            System.out.println("");
        }
        System.out.println("");
    }
    
}
