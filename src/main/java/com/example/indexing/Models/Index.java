package com.example.indexing.Models;
import java.util.List;
import java.util.Objects;

/**
 * Created by Enzo Cotter on 3/1/20.
 */
public class Index {

    private String term;
    private List<int[]> matrix;

    public Index(String term, List<int[]> matrix) {
        this.term = term;
        this.matrix = matrix;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public List<int[]> getMatrix() {
        return matrix;
    }

    public void setMatrix(List<int[]> matrix) {
        this.matrix = matrix;
    }

    @Override
    public String toString() {
        String ss=term+":"+" ";
        for(int[] l:matrix){
            ss=ss+l[0]+"("+l[1]+")"+" ";
        }
        return ss;
    }
}
