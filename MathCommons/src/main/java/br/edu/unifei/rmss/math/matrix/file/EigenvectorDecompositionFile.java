/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.math.matrix.file;

import br.edu.unifei.rmss.math.matrix.Matrix;
import br.edu.unifei.rmss.math.matrix.EigenvectorDecomposition;
import java.io.IOException;

/**
 *
 * @author rafael
 */
public class EigenvectorDecompositionFile extends EigenvectorDecomposition{

    public EigenvectorDecompositionFile(Matrix matrix) throws IOException {
        
        n = matrix.rows();
        
        d = new double[n];
	e = new double[n];
        
        //verifica a simetria da matriz
        if (matrix.isSymmetric()) {
            
            V = new MatrixFile(matrix);

            // Tridiagonalize
            tred2();

            // Diagonalize
            tql2();

            // Sort eigenvalues/eigenvectors
            sort();

        } else {
            
            V = new MatrixFile(n, n);

            H = new MatrixFile(matrix);

            ort = new double[n];

            // Reduce to Hessenberg form.
            orthes();

            // Reduce Hessenberg to real Schur form.
            hqr2();

            // Normalize eigenvectors
            normalize();
        }

        // Eigenvector matrix
        eigenvectors = new MatrixFile(V);
    }
    
}
