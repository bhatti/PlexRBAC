package com.plexobject.rbac.eval.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.validator.GenericValidator;

public class Expression {
    private static final Pattern EXPRESSION_SPLIT_PATTERN = Pattern
            .compile("(\\s+and\\s+|\\s+or\\s+|\\s+\\&\\&\\s+|\\s\\|\\|\\s+|;|,)");
    private static final Pattern TOKEN_SPLIT_PATTERN = Pattern
            .compile("(\\s+|')");

    private final String name;
    private final Type type;
    private final Operator operator;
    private final String value; // time value must be in millisSince1970 or

    // HH:MM:ss

    public static Collection<Expression> parse(final String expression) {
        if (GenericValidator.isBlankOrNull(expression)) {
            throw new IllegalArgumentException("expression not specified");
        }
        final String[] rawExprs = EXPRESSION_SPLIT_PATTERN.split(expression);
        Collection<Expression> exprs = new ArrayList<Expression>();
        for (String rawExpr : rawExprs) {
            String[] tokens = TOKEN_SPLIT_PATTERN.split(rawExpr.trim());
            if (tokens.length < 3) {
                throw new IllegalArgumentException("illegal expression in "
                        + rawExpr + ", full " + expression);
            }
            String value = null;
            int start, end;
            if ((start = rawExpr.indexOf("'")) != -1
                    && (end = rawExpr.lastIndexOf("'")) != -1 && start < end) {
                value = rawExpr.substring(start + 1, end);
            } else if ((start = rawExpr.indexOf("\"")) != -1
                    && (end = rawExpr.lastIndexOf("\"")) != -1
                    && start < end) {
                value = rawExpr.substring(start + 1, end);
            } else {
                value = StringUtils.join(tokens, " ", 2, tokens.length)
                        .replaceAll("\\s+", " ");
            }
            value = value.trim();
            String name = tokens[0];
            Type type = Type.isNumber(tokens[2]) ? Type.NUMBER : value
                    .indexOf(":") != -1 ? Type.TIME : Type.STRING;
            Operator operator = Operator.bySymbol(tokens[1]);
            exprs.add(new Expression(name, type, operator, value));
        }
        return exprs;
    }

    public Expression(String name, Type type, Operator operator, String value) {
        if (GenericValidator.isBlankOrNull(name)) {
            throw new IllegalArgumentException("name not specified");
        }
        if (type == null) {
            throw new IllegalArgumentException("type not specified");
        }
        if (operator == null) {
            throw new IllegalArgumentException("operator not specified");
        }
        if (value == null) {
            throw new IllegalArgumentException("value not specified");
        }
        this.name = name;
        this.type = type;
        this.operator = operator;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Operator getOperator() {
        return operator;
    }

    public String getValue() {
        return value;
    }

    public boolean evaluate(final String value) {
        return operator.matches(this.value, value, this.type);
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Expression)) {
            return false;
        }
        Expression rhs = (Expression) object;
        return new EqualsBuilder().append(this.name, rhs.name).append(
                this.type, rhs.type).append(this.operator, rhs.operator)
                .append(this.value, rhs.value).isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(786529047, 1924536713).append(this.name)
                .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.name + ":" + this.type + " " + this.operator + " "
                + this.value;
    }
}
