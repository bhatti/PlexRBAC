package com.plexobject.rbac.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;

import com.sleepycat.persist.model.Persistent;

@Persistent
public class ContextProperty implements Validatable {
    private static final Logger LOGGER = Logger
            .getLogger(ContextProperty.class);

    private static final Pattern NUMBER_PATTERN = Pattern.compile("[\\d\\.]+");
    private static final ThreadLocal<SimpleDateFormat> TIME_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("h:mma");
        }
    };

    public enum Type {
        STRING, NUMBER, TIME
    }

    interface Matchable {
        boolean doesMatch(final String first, final String second,
                final Type type);
    }

    public enum Operator {
        EQUALS("==", new Matchable() {
            public boolean doesMatch(final String first, final String second,
                    final Type type) {
                return first.equalsIgnoreCase(second);
            }
        }), CONTAINS("in", new Matchable() {
            public boolean doesMatch(final String first, final String second,
                    final Type type) {
                return first.toLowerCase().contains(second.toLowerCase());
            }
        }), LESS_THAN("<", new Matchable() {
            public boolean doesMatch(final String first, final String second,
                    final Type type) {
                if (isNumber(first) && isNumber(second)) {
                    return number(second) < number(first);
                }
                switch (type) {
                case TIME:
                    try {
                        Date firstDate = TIME_FORMAT.get().parse(first);
                        Date secondDate = TIME_FORMAT.get().parse(second);
                        return secondDate.compareTo(firstDate) < 0;
                    } catch (ParseException e) {
                        LOGGER.error("failed to parse " + first + " and "
                                + second, e);
                    }
                case STRING:
                    return second.compareTo(first) < 0;
                }

                return false;
            }
        }), LESS_OR_EQUALS("<=", new Matchable() {
            public boolean doesMatch(final String first, final String second,
                    final Type type) {
                if (isNumber(first) && isNumber(second)) {
                    return number(second) <= number(first);
                }
                switch (type) {
                case TIME:
                    try {
                        Date firstDate = TIME_FORMAT.get().parse(first);
                        Date secondDate = TIME_FORMAT.get().parse(second);
                        return secondDate.compareTo(firstDate) <= 0;
                    } catch (ParseException e) {
                        LOGGER.error("failed to parse " + first + " and "
                                + second, e);
                    }
                case STRING:
                    return second.compareTo(first) <= 0;
                }

                return false;
            }
        }), GREATER_THAN(">", new Matchable() {
            public boolean doesMatch(final String first, final String second,
                    final Type type) {
                if (isNumber(first) && isNumber(second)) {
                    return number(second) > number(first);
                }
                switch (type) {
                case TIME:
                    try {
                        Date firstDate = TIME_FORMAT.get().parse(first);
                        Date secondDate = TIME_FORMAT.get().parse(second);
                        return secondDate.compareTo(firstDate) > 0;
                    } catch (ParseException e) {
                        LOGGER.error("failed to parse " + first + " and "
                                + second, e);
                    }
                case STRING:
                    return second.compareTo(first) > 0;
                }

                return false;
            }
        }), GREATER_OR_EQUALS(">=", new Matchable() {
            public boolean doesMatch(final String first, final String second,
                    final Type type) {
                if (isNumber(first) && isNumber(second)) {
                    return number(second) >= number(first);
                }
                switch (type) {
                case TIME:
                    try {
                        Date firstDate = TIME_FORMAT.get().parse(first);
                        Date secondDate = TIME_FORMAT.get().parse(second);
                        return secondDate.compareTo(firstDate) >= 0;
                    } catch (ParseException e) {
                        LOGGER.error("failed to parse " + first + " and "
                                + second, e);
                    }
                case STRING:
                    return second.compareTo(first) >= 0;
                }

                return false;
            }
        }), NOT_EQUALS("!=", new Matchable() {
            public boolean doesMatch(final String first, final String second,
                    final Type type) {
                return !first.equalsIgnoreCase(second);
            }
        }), IN_RANGE("..", new Matchable() {
            public boolean doesMatch(final String first, final String second,
                    final Type type) {
                String[] firstParts = first.split("\\.\\.");
                if (firstParts.length != 2) {
                    throw new IllegalArgumentException(first
                            + " does not match range format "
                            + firstParts.length);
                }
                switch (type) {
                case NUMBER:
                    return number(second) >= number(firstParts[0])
                            && number(second) <= number(firstParts[1]);
                case TIME:
                    if (isNumber(second)) {
                        return number(second) >= number(firstParts[0])
                                && number(second) <= number(firstParts[1]);
                    } else {
                        try {
                            Date firstDate1 = TIME_FORMAT.get().parse(
                                    firstParts[0]);
                            Date firstDate2 = TIME_FORMAT.get().parse(
                                    firstParts[1]);
                            Date secondDate = TIME_FORMAT.get().parse(second);
                            return secondDate.compareTo(firstDate1) >= 0
                                    && secondDate.compareTo(firstDate2) <= 0;

                        } catch (ParseException e) {
                            LOGGER.error("failed to parse " + first + " and "
                                    + second, e);
                        }
                    }
                case STRING:
                    return second.compareTo(firstParts[0]) >= 0
                            && second.compareTo(firstParts[1]) <= 0;
                }

                return false;
            }
        });
        private final String symbol;
        private final Matchable matchable;

        Operator(final String symbol, final Matchable matchable) {
            this.symbol = symbol;
            this.matchable = matchable;
        }

        public boolean matches(final String first, final String second,
                final Type type) {
            return matchable.doesMatch(first, second, type);
        }

        public static Operator bySymbol(final String symbol) {
            for (Operator op : Operator.values()) {
                if (op.symbol.equals(symbol)) {
                    return op;
                }
            }
            return null;
        }

        public String toString() {
            return symbol;
        }
    }

    ContextProperty() {
    }

    public ContextProperty(String name, Type type, Operator operator,
            String value) {
        setName(name);
        setType(type);
        setOperator(operator);
        setValue(value);
    }

    private String name;
    private Type type;
    private Operator operator;
    private String value; // time value must be in millisSince1970 or HH:MM:ss

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (GenericValidator.isBlankOrNull(name)) {
            throw new IllegalArgumentException("name not specified");
        }
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type not specified");
        }

        this.type = type;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        if (operator == null) {
            throw new IllegalArgumentException("operator not specified");
        }

        this.operator = operator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value not specified");
        }

        this.value = value;
    }

    public boolean implies(final String value) {
        return operator.matches(this.value, value, this.type);
    }

    private static boolean isNumber(final String value) {
        return NUMBER_PATTERN.matcher(value).matches();
    }

    private static double number(final String value) {
        return new Double(value).doubleValue();
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ContextProperty)) {
            return false;
        }
        ContextProperty rhs = (ContextProperty) object;
        return new EqualsBuilder().append(this.name, rhs.name).isEquals();
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

    @Override
    public void validate() throws ValidationException {
        final Map<String, String> errorsByField = new HashMap<String, String>();
        if (GenericValidator.isBlankOrNull(name)) {
            errorsByField.put("name", "name is not specified");
        }
        if (GenericValidator.isBlankOrNull(value)) {
            errorsByField.put("value", "value is not specified");
        }
        if (type == null) {
            errorsByField.put("type", "type is not specified");
        }
        if (operator == null) {
            errorsByField.put("operator", "operator is not specified");
        }

        if (errorsByField.size() > 0) {
            throw new ValidationException(errorsByField);
        }
    }
}
