/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.math.matrix.colt;

import br.edu.unifei.rmss.math.matrix.Matrix;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import java.util.Arrays;


/**
 *
 * @author rafael
 */
public class MatrixColt extends Matrix{
    
    private int row;
    
    private int column;
    
    private DoubleMatrix2D matrix;

    public MatrixColt(Matrix m){
        this.row = m.rows();
        this.column = m.columns();
        initDB(m);
    }

    public MatrixColt(double[][] m){
        this.row = m.length;
        double[] first_row = m[0];
        this.column = first_row.length;
        initDB(m);
    }
    
    public MatrixColt(int row, int column) {
        this.row = row;
        this.column = column;
        initDB();
    }   

    @Override
    public int rows() {
        return row;
    }

    @Override
    public int columns() {
        return column;
    }

    @Override
    public double get(int i, int j) {
        double result = getCoefficient(i,j); 
        return result;
    }

    @Override
    public void set(int i, int j, double v) {
        if (!setCoefficient(i,j,v)){
            throw new Error("Coefficient "+v+" in position ("+i+","+j+") was not seted."); 
        }
    }  
        
    //**************************************************************************
    //Métodos para operações de banco de dados
    //**************************************************************************
    //cria uma instância do banco em disco
    private boolean initDB() {		
	createDefaltCoefficients();
        return true;        
    }
    
    private boolean initDB(Matrix m) {		
	copyCoefficients(m);
        return true; 
    }
    
    private boolean initDB(double[][] m) {		
	copyCoefficients(m);
        return true; 
    }
    
    //cria registro iniciais com o valor 0 no banco de dados
    private void createDefaltCoefficients(){
        double[][] coefficients = new double[row][column];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                coefficients[i][j] = 0.0;
            }
        }
        matrix =  new DenseDoubleMatrix2D(coefficients);
    }
    
    //cria registro iniciais copiados de outra matrix
    private void copyCoefficients(Matrix m) {
        if ((m.rows() == row) && (m.columns() == column)) {
            double[][] coefficients = m.getArray();
            matrix =  new DenseDoubleMatrix2D(coefficients);
        }
    }
    
    //cria registro iniciais copiados de outra matrix
    private void copyCoefficients(double[][] m) {
        matrix =  new DenseDoubleMatrix2D(m);
    }
        
    //atualiza registro no banco de dados
    public boolean setCoefficient(int row, int column, double value){
        
        if ((row < this.row) && (column < this.column)){
            this.matrix.set(row, column, value);
            return true;
        } else {
            return false;
        }
                
    }
        
    //bucar registro no banco de dados
    public double getCoefficient(int row, int column){
        
        if ((row < this.row) && (column < this.column)){
            return this.matrix.get(row, column);
        } else {
            return 0;
        }
        
    }  
    
    public double[][] getEigenvectors(){ 
        EigenvalueDecomposition eigenDecomp = new EigenvalueDecomposition(matrix);
	DoubleMatrix2D eigenVectorMatrix = eigenDecomp.getV();
        return eigenVectorMatrix.toArray();
        
    }

    @Override
    public double getMedianValueAtLine(int i) {
        double[][] general = matrix.toArray();
        double[] line = general[i];
        Arrays.sort(line);
        return line[Math.round(line.length/2)];        
    }

    @Override
    public double getMaxDistanceAtLine(int i) {
        double[][] general = matrix.toArray();
        double[] line = general[i];
        Arrays.sort(line);
        double value = 0;
        int pos = -1;
        for (int j = 0; j < line.length-1; j++) {
            if (Math.abs(line[j] - line[j+1]) > value){
                value = Math.abs(line[j] - line[j+1]);
                pos = j+1;
            }
        }
        if (pos != -1){
            value = line[pos];
        }
        return value;
    }

    @Override
    public double max() {
        double max = 0;
        double[][] general = matrix.toArray();
        for (int i = 0; i < general.length; i++) {
            double[] line = general[i];
            Arrays.sort(line);
            if (line[line.length-1]>max){
                max = line[line.length-1];
            }
        }
        return max;
    }

    @Override
    public double min() {
        double min = 0;
        double[][] general = matrix.toArray();
        for (int i = 0; i < general.length; i++) {
            double[] line = general[i];
            Arrays.sort(line);
            if (line[0]<min){
                min = line[0];
            }
        }
        return min;
    }

    @Override
    public double[] getRow(int i) {
        double[][] general = matrix.toArray();
        double[] line = general[i];
        return line;
    }
    
}
