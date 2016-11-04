/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.math.matrix.file;

import br.edu.unifei.rmss.math.matrix.Matrix;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

/**
 *
 * @author rafael
 */
public class MatrixFile extends Matrix implements Closeable {

    private static final int MAPPING_SIZE = 1 << 30;
    private final RandomAccessFile raf;
    private final int rows;
    private final int columns;
    private final List<MappedByteBuffer> mappings = new ArrayList<MappedByteBuffer>();
    private String filename;

    public MatrixFile(int rows, int columns) throws IOException {
       
        this.filename = createName();

        this.raf = new RandomAccessFile(filename, "rw");
        try {
            this.rows = rows;
            this.columns = columns;
            long size = 8L * rows * columns;
            for (long offset = 0; offset < size; offset += MAPPING_SIZE) {
                long size2 = Math.min(size - offset, MAPPING_SIZE);
                mappings.add(raf.getChannel().map(FileChannel.MapMode.READ_WRITE, offset, size2));
            }
        } catch (IOException e) {
            raf.close();
            throw e;
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                File f = new File(filename);
                deleteFileOrDirectory(f);
            }
        });
    }

    public MatrixFile(Matrix m) throws IOException {

        this.filename = createName();

        this.raf = new RandomAccessFile(filename, "rw");
        try {
            this.rows = m.rows();
            this.columns = m.columns();
            long size = 8L * rows * columns;
            for (long offset = 0; offset < size; offset += MAPPING_SIZE) {
                long size2 = Math.min(size - offset, MAPPING_SIZE);
                mappings.add(raf.getChannel().map(FileChannel.MapMode.READ_WRITE, offset, size2));
            }
        } catch (IOException e) {
            raf.close();
            throw e;
        }

        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.columns(); j++) {
                set(i, j, m.get(i, j));
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                File f = new File(filename);
                deleteFileOrDirectory(f);
            }
        });

    }

    public MatrixFile(double[][] m) throws IOException {

        this.filename = createName();

        this.raf = new RandomAccessFile(filename, "rw");
        try {
            this.rows = m.length;
            this.columns = m[0].length;
            long size = 8L * rows * columns;
            for (long offset = 0; offset < size; offset += MAPPING_SIZE) {
                long size2 = Math.min(size - offset, MAPPING_SIZE);
                mappings.add(raf.getChannel().map(FileChannel.MapMode.READ_WRITE, offset, size2));
            }
        } catch (IOException e) {
            raf.close();
            throw e;
        }

        for (int i = 0; i < m.length; i++) {
            double[] line = m[i];
            for (int j = 0; j < line.length; j++) {
                set(i, j, line[j]);
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                File f = new File(filename);
                deleteFileOrDirectory(f);
            }
        });

    }

    protected String createName() {
        Random rand = new Random();
        int val = rand.nextInt(100000);
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        String name = "M" + dateFormat.format(date) + "_" + String.valueOf(val) ;
;                
        File f = new File(name);
        deleteFileOrDirectory(f);
        return name;
    }

    protected long position(int x, int y) {
        return (long) y * rows + x;
    }

    public int rows() {
        return rows;
    }

    public int columns() {
        return columns;
    }

    public double get(int x, int y) {
        assert x >= 0 && x < rows;
        assert y >= 0 && y < columns;
        long p = position(x, y) * 8;
        int mapN = (int) (p / MAPPING_SIZE);
        int offN = (int) (p % MAPPING_SIZE);
        return mappings.get(mapN).getDouble(offN);
    }

    public void set(int x, int y, double d) {
        assert x >= 0 && x < rows;
        assert y >= 0 && y < columns;
        long p = position(x, y) * 8;
        int mapN = (int) (p / MAPPING_SIZE);
        int offN = (int) (p % MAPPING_SIZE);
        mappings.get(mapN).putDouble(offN, d);
    }

    public void close() throws IOException {
        for (MappedByteBuffer mapping : mappings) {
            clean(mapping);
        }
        raf.close();
        File f = new File(filename);
        deleteFileOrDirectory(f);
    }

    private void clean(MappedByteBuffer mapping) {
        if (mapping == null) {
            return;
        }
        Cleaner cleaner = ((DirectBuffer) mapping).cleaner();
        if (cleaner != null) {
            cleaner.clean();
        }
    }

    public double sum() {
        double sum = 0;
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                sum += get(i, j);
            }
        }
        return sum;
    }

    @Override
    public double[] getRow(int i) {
        double[] result = new double[columns];
        for (int j = 0; j < columns; j++) {
            result[j] = get(i, j);
        }
        return result;
    }

    @Override
    public double getMedianValueAtLine(int i) {
        double[] row = getRow(i);
        Arrays.sort(row);
        return row[Math.abs(row.length / 2)];
    }

    @Override
    public double getMaxDistanceAtLine(int i) {
        double[] line = getRow(i);
        Arrays.sort(line);
        double value = 0;
        int pos = -1;
        for (int j = 0; j < line.length - 1; j++) {
            if (Math.abs(line[j] - line[j + 1]) > value) {
                value = Math.abs(line[j] - line[j + 1]);
                pos = j + 1;
            }
        }
        if (pos != -1) {
            value = line[pos];
        }
        return value;
    }

    @Override
    public double max() {
        double max = 0;
        for (int i = 0; i < rows; i++) {
            double[] line = getRow(i);
            Arrays.sort(line);
            if (line[line.length - 1] > max) {
                max = line[line.length - 1];
            }
        }
        return max;
    }

    @Override
    public double min() {
        double min = 0;
        for (int i = 0; i < rows; i++) {
            double[] line = getRow(i);
            Arrays.sort(line);
            if (line[0] < min) {
                min = line[0];
            }
        }
        return min;
    }

    private void deleteFileOrDirectory(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File child : file.listFiles()) {
                    deleteFileOrDirectory(child);
                }
            }
            file.delete();
        }
    }
}
