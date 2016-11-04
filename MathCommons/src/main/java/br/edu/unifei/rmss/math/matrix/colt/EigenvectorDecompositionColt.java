/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.math.matrix.colt;

import br.edu.unifei.rmss.math.matrix.EigenvectorDecomposition;
import br.edu.unifei.rmss.math.matrix.Matrix;


/**
 *
 * @author rafael
 */
public class EigenvectorDecompositionColt extends EigenvectorDecomposition{

    public EigenvectorDecompositionColt(Matrix matrix) {
        
        MatrixColt m = new MatrixColt(matrix);
        // Eigenvector matrix
        eigenvectors = new MatrixColt(m.getEigenvectors());
    }
}
