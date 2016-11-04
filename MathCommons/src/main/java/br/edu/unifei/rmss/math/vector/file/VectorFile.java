/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.unifei.rmss.math.vector.file;

import br.edu.unifei.rmss.math.vector.Vector;
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
import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

/**
 *
 * @author rafael
 */
public class VectorFile extends Vector implements Closeable {

    private static final int MAPPING_SIZE = 1 << 30;
    private final RandomAccessFile raf;
    private final int rows;
    private final int columns;
    private final List<MappedByteBuffer> mappings = new ArrayList<MappedByteBuffer>();
    private String filename;

    public VectorFile(int i) throws IOException {

        this.filename = createName();

        this.raf = new RandomAccessFile(filename, "rw");
        try {
            this.rows = 1;
            this.columns = i;
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

    public VectorFile(Vector v) throws IOException {

        this.filename = createName();

        this.raf = new RandomAccessFile(filename, "rw");
        try {
            this.rows = 1;
            this.columns = v.size();
            long size = 8L * rows * columns;
            for (long offset = 0; offset < size; offset += MAPPING_SIZE) {
                long size2 = Math.min(size - offset, MAPPING_SIZE);
                mappings.add(raf.getChannel().map(FileChannel.MapMode.READ_WRITE, offset, size2));
            }
        } catch (IOException e) {
            raf.close();
            throw e;
        }

        for (int i = 0; i < v.size(); i++) {
            set(i, v.get(i));
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                File f = new File(filename);
                deleteFileOrDirectory(f);
            }
        });

    }

    public VectorFile(double[] v) throws IOException {

        this.filename = createName();

        this.raf = new RandomAccessFile(filename, "rw");
        try {
            this.rows = 1;
            this.columns = v.length;
            long size = 8L * rows * columns;
            for (long offset = 0; offset < size; offset += MAPPING_SIZE) {
                long size2 = Math.min(size - offset, MAPPING_SIZE);
                mappings.add(raf.getChannel().map(FileChannel.MapMode.READ_WRITE, offset, size2));
            }
        } catch (IOException e) {
            raf.close();
            throw e;
        }

        for (int i = 0; i < v.length; i++) {
            set(i, v[i]);
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
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        return "V" + dateFormat.format(date);
    }

    protected long position(int x, int y) {
        return (long) y * rows + x;
    }

    @Override
    public int size(){
        return this.columns;
    }
    
    @Override
    public double get(int i) {
        assert i >= 0 && i < columns;
        long p = position(0, i) * 8;
        int mapN = (int) (p / MAPPING_SIZE);
        int offN = (int) (p % MAPPING_SIZE);
        return mappings.get(mapN).getDouble(offN);
    }

    @Override
    public void set(int i, double d) {
        assert i >= 0 && i < columns;
        long p = position(0, i) * 8;
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

    @Override
    public double max() {
        double[] line = getArray();
        Arrays.sort(line);
        return line[columns-1];
    }

    @Override
    public double min() {
        double[] line = getArray();
        Arrays.sort(line);
        return line[0];
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
