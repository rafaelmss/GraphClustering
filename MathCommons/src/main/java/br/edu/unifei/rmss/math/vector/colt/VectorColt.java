/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.math.vector.colt;

import br.edu.unifei.rmss.math.vector.Vector;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import java.util.Arrays;


/**
 *
 * @author rafael
 */
public class VectorColt extends Vector{
    
    private int size;
    
    private DoubleMatrix1D vector;

    public VectorColt(Vector v){
        this.size = v.size();
        vector = new DenseDoubleMatrix1D(v.getArray());
    }

    public VectorColt(double[] v){
        this.size = v.length;
        vector = new DenseDoubleMatrix1D(v);
    }
    
    public VectorColt(int size) {
        this.size = size;
        vector = new DenseDoubleMatrix1D(size);
        zero();
    }   

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public double get(int i) {
        return vector.get(i);
    }

    @Override
    public void set(int i, double v) {
        vector.set(i, v);
    }

    @Override
    public double max() {
        double[] values = vector.toArray();
        Arrays.sort(values);
        return values[size-1];
    }

    @Override
    public double min() {
        double[] values = vector.toArray();
        Arrays.sort(values);
        return values[0];
    }

    @Override
    public double[] getArray() {
        return vector.toArray();
    }
    
    @Override
    public void set(double v[]) {
        this.size = v.length;
        vector = new DenseDoubleMatrix1D(v);
    }

    @Override
    public void set(Vector v) {
        double[] values = v.getArray();
        this.size = values.length;
        vector = new DenseDoubleMatrix1D(values);
    }
}
