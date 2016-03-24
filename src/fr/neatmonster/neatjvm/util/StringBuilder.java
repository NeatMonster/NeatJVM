package fr.neatmonster.neatjvm.util;

public class StringBuilder {
    private final java.lang.StringBuilder builder     = new java.lang.StringBuilder();
    private int                           indentLevel = 0;
    private boolean                       addComma    = false;
    private boolean                       addIndent   = false;
    private boolean                       isEmpty     = false;

    public void append(final String s) {
        if (addIndent) {
            if (builder.length() > 0)
                builder.append(addComma ? ",\n" : "\n");
            for (int i = 0; i < indentLevel; ++i)
                builder.append(" ");
            addIndent = false;
        }
        builder.append(s);
        isEmpty = false;
    }

    public void appendln(final String s) {
        append(s);
        addComma = addIndent = true;
    }

    public void openObject(final Object obj) {
        appendln((obj == null ? "" : obj.getClass().getSimpleName()) + "{");
        addComma = false;
        indentLevel += 4;
        isEmpty = true;
    }

    public void closeObject() {
        indentLevel -= 4;
        addComma = false;
        addIndent = !isEmpty;
        appendln("}");
    }

    public void openArray() {
        appendln("[");
        addComma = false;
        indentLevel += 4;
        isEmpty = true;
    }

    public void closeArray() {
        indentLevel -= 4;
        addComma = false;
        addIndent = !isEmpty;
        appendln("]");
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}