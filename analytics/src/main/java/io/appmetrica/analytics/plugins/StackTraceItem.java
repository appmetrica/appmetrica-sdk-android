package io.appmetrica.analytics.plugins;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Class describing the stacktrace in a common form
 * so that errors both from native and plugin code can be described via this object.
 *
 * @see PluginErrorDetails.Builder#withStacktrace(java.util.List)
 */
public class StackTraceItem {

    @Nullable
    private final String className;
    @Nullable
    private final String fileName;
    @Nullable
    private final Integer line;
    @Nullable
    private final Integer column;
    @Nullable
    private final String methodName;

    private StackTraceItem(
            @Nullable String className,
            @Nullable String fileName,
            @Nullable Integer line,
            @Nullable Integer column,
            @Nullable String methodName
    ) {
        this.className = className;
        this.fileName = fileName;
        this.line = line;
        this.column = column;
        this.methodName = methodName;
    }

    @Nullable
    public String getClassName() {
        return className;
    }

    @Nullable
    public String getFileName() {
        return fileName;
    }

    @Nullable
    public Integer getLine() {
        return line;
    }

    @Nullable
    public Integer getColumn() {
        return column;
    }

    @Nullable
    public String getMethodName() {
        return methodName;
    }

    public static class Builder {

        @Nullable
        private String className;
        @Nullable
        private String fileName;
        @Nullable
        private Integer line;
        @Nullable
        private Integer column;
        @Nullable
        private String methodName;

        /**
         * Sets class name.
         *
         * @param value name of the class/interface/symbol (depending on the plugin you are using)
         *              where the error occurred.
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withClassName(@Nullable String value) {
            className = value;
            return this;
        }

        /**
         * Sets file name.
         *
         * @param value name of the file where the error occurred.
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withFileName(@Nullable String value) {
            fileName = value;
            return this;
        }

        /**
         * Sets line.
         *
         * @param value line number in which the error occurred.
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withLine(@Nullable Integer value) {
            line = value;
            return this;
        }

        /**
         * Sets column.
         *
         * @param value column in which the error occurred.
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withColumn(@Nullable Integer value) {
            column = value;
            return this;
        }

        /**
         * Sets method name.
         *
         * @param value name of the method/function (depending on the plugin you are using) where the error occurred.
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withMethodName(@Nullable String value) {
            methodName = value;
            return this;
        }

        /**
         * Creates an instance of {@link StackTraceItem}.
         *
         * @return {@link StackTraceItem} object.
         */
        @NonNull
        public StackTraceItem build() {
            return new StackTraceItem(className, fileName, line, column, methodName);
        }

    }
}
