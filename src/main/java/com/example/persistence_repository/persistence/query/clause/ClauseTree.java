package com.example.persistence_repository.persistence.query.clause;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClauseTree {
    String value;
    ClauseTree leftClause;
    ClauseTree rightClause;
    List<Object> parameters;

    public ClauseTree() {
        parameters = new ArrayList<>();
    }

    public ClauseTree(ClauseTree leftClause, ClauseTree rightClause, String value) {
        parameters = new ArrayList<>();
        this.value = value;
        this.leftClause = leftClause;
        this.rightClause = rightClause;
    }

    public ClauseTree(String value, List<Object> parameters) {
        parameters = new ArrayList<>();
        this.value = value;
        this.parameters = parameters;
    }

    public boolean isLeaf() {
        return leftClause == null && rightClause == null;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ClauseTree getLeftClause() {
        return leftClause;
    }

    public void setLeftClause(ClauseTree leftClause) {
        this.leftClause = leftClause;
    }

    public ClauseTree getRightClause() {
        return rightClause;
    }

    public void setRightClause(ClauseTree rightClause) {
        this.rightClause = rightClause;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }

    public static ClauseTree and(ClauseTree leftClause, ClauseTree rightClause) {
        return new ClauseTree(leftClause, rightClause, " AND ");
    }

    public static ClauseTree or(ClauseTree leftClause, ClauseTree rightClause) {
        return new ClauseTree(leftClause, rightClause, " OR ");
    }

    public static ClauseTree create(String value, Object... objects) {
        return new ClauseTree(value, Arrays.asList(objects));
    }

    public String build() {
        StringBuilder builder = new StringBuilder();
        traversal(this, builder, parameters);

        return builder.toString();
    }

    private void traversal(ClauseTree clause, StringBuilder builder, List<Object> params) {
        if (clause == null) {
            return;
        }

        traversal(clause.leftClause, builder, params);

        System.out.println(clause.value);
        if (clause.isLeaf()) {
            builder.append(" ( " + clause.value + " ) ");
        } else {
            builder.append(clause.value);
        }

        params.addAll(clause.getParameters());

        traversal(clause.rightClause, builder, params);
    }

}
