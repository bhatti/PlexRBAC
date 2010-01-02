package com.plexobject.rbac.eval.simple;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

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
            if (Type.isNumber(first) && Type.isNumber(second)) {
                return Type.number(second) < Type.number(first);
            }
            switch (type) {
            case TIME:
                try {
                    Date firstDate = TIME_FORMAT.get().parse(first);
                    Date secondDate = TIME_FORMAT.get().parse(second);
                    return secondDate.compareTo(firstDate) < 0;
                } catch (ParseException e) {
                    LOGGER.error("failed to parse " + first + " and " + second,
                            e);
                }
            case STRING:
                return second.compareTo(first) < 0;
            }

            return false;
        }
    }), LESS_OR_EQUALS("<=", new Matchable() {
        public boolean doesMatch(final String first, final String second,
                final Type type) {
            if (Type.isNumber(first) && Type.isNumber(second)) {
                return Type.number(second) <= Type.number(first);
            }
            switch (type) {
            case TIME:
                try {
                    Date firstDate = TIME_FORMAT.get().parse(first);
                    Date secondDate = TIME_FORMAT.get().parse(second);
                    return secondDate.compareTo(firstDate) <= 0;
                } catch (ParseException e) {
                    LOGGER.error("failed to parse " + first + " and " + second,
                            e);
                }
            case STRING:
                return second.compareTo(first) <= 0;
            }

            return false;
        }
    }), GREATER_THAN(">", new Matchable() {
        public boolean doesMatch(final String first, final String second,
                final Type type) {
            if (Type.isNumber(first) && Type.isNumber(second)) {
                return Type.number(second) > Type.number(first);
            }
            switch (type) {
            case TIME:
                try {
                    Date firstDate = TIME_FORMAT.get().parse(first);
                    Date secondDate = TIME_FORMAT.get().parse(second);
                    return secondDate.compareTo(firstDate) > 0;
                } catch (ParseException e) {
                    LOGGER.error("failed to parse " + first + " and " + second,
                            e);
                }
            case STRING:
                return second.compareTo(first) > 0;
            }

            return false;
        }
    }), GREATER_OR_EQUALS(">=", new Matchable() {
        public boolean doesMatch(final String first, final String second,
                final Type type) {
            if (Type.isNumber(first) && Type.isNumber(second)) {
                return Type.number(second) >= Type.number(first);
            }
            switch (type) {
            case TIME:
                try {
                    Date firstDate = TIME_FORMAT.get().parse(first);
                    Date secondDate = TIME_FORMAT.get().parse(second);
                    return secondDate.compareTo(firstDate) >= 0;
                } catch (ParseException e) {
                    LOGGER.error("failed to parse " + first + " and " + second,
                            e);
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
    }), IN_RANGE("between", new Matchable() {
        public boolean doesMatch(final String first, final String second,
                final Type type) {
            String[] firstParts = first.split("\\.\\.");
            if (firstParts.length != 2) {
                throw new IllegalArgumentException(first
                        + " does not match range format " + firstParts.length);
            }
            switch (type) {
            case NUMBER:
                return Type.number(second) >= Type.number(firstParts[0])
                        && Type.number(second) <= Type.number(firstParts[1]);
            case TIME:
                if (Type.isNumber(second)) {
                    return Type.number(second) >= Type.number(firstParts[0])
                            && Type.number(second) <= Type
                                    .number(firstParts[1]);
                } else {
                    try {
                        Date firstDate1 = TIME_FORMAT.get()
                                .parse(firstParts[0]);
                        Date firstDate2 = TIME_FORMAT.get()
                                .parse(firstParts[1]);
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
    private static final Logger LOGGER = Logger.getLogger(Operator.class);

    private static final ThreadLocal<SimpleDateFormat> TIME_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("h:mma");
        }
    };

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
