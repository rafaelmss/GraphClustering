/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.math.vector;

/**
 *
 * @author rafael
 */
public abstract class Vector {
    
    public abstract int size();

    public abstract double get(int i);

    public abstract void set(int i, double v);

    public double[] getArray() {
        int m = size();
        double[] array = new double[m];

        for (int i = 0; i < m; i++) {
            array[i] = get(i);
        }

        return array;
    }

    public void set(double v[]) {
        if (v.length == size()) {
            for (int i = 0; i < v.length; i++) {
                set(i, v[i]);
            }
        }
    }

    public void set(Vector v) {
        if ((this.size() == v.size())) {
            for (int i = 0; i < v.size(); i++) {
                this.set(i, v.get(i));
            }
        }
    }

    public void swap(int i, int j){
        double value1 = get(i);
        double value2 = get(j);
        set(i, value2);
        set(j, value1);
    }
    
    public void zero() {
        int s = size();
        for (int i = 0; i < s; i++) {
            set(i, 0);
        }
    }

    public abstract double max();
    
    public abstract double min();
    
}
