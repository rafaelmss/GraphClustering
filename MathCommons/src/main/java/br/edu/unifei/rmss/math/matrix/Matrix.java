/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.math.matrix;

/**
 *
 * @author rafael
 */
public abstract class Matrix {
    
    public int size() {
        return rows() * columns();
    }

    public abstract int rows();

    public abstract int columns();

    public abstract double get(int i, int j);

    public abstract void set(int i, int j, double v);

    public double[][] getArray() {
        int m = rows();
        int n = columns();
        double[][] array = new double[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                array[i][j] = get(i, j);
            }
        }

        return array;
    }

    public abstract double[] getRow(int i);   

    public void set(double v[][]) {
        if (v.length == rows()) {

            for (int i = 0; i < v.length; i++) {
                setRow(i, v[i]);
            }
        }
    }

    public void setRow(int i, double v[]) {
        if (v.length == columns()) {

            for (int j = 0; j < v.length; j++) {
                Matrix.this.set(i, j, v[j]);
            }
        }
    }

    public void set(Matrix m) {
        if ((this.rows() == m.rows()) && (this.columns() == m.columns())) {
            for (int i = 0; i < m.rows(); i++) {
                for (int j = 0; j < m.columns(); j++) {
                    Matrix.this.set(i, j, m.get(i, j));
                }
            }
        }
    }

    public void swap(int i1, int j1, int i2, int j2){
        double value1 = get(i1, j1);
        double value2 = get(i2, j2);
        set(i1, j1, value2);
        set(i2, j2, value1);
    }
    
    public void zero() {
        int i, j;
        int filas = rows();
        int columnas = columns();

        for (i = 0; i < filas; i++) {
            for (j = 0; j < columnas; j++) {
                set(i, j, 0);
            }
        }
    }

    public boolean isSymmetric() {
        if (this.rows() != this.columns()) return false;
        
        int n = rows();

        for (int j = 0; (j < n); j++) {
            for (int i = j + 1; (i < n); i++) {
                if (get(i, j) != get(j, i)) return false;
            }
        }

        return true;
    }

    public abstract double getMedianValueAtLine(int i);
    
    public abstract double getMaxDistanceAtLine(int i);
    
    public abstract double max();
    
    public abstract double min();
    
}
