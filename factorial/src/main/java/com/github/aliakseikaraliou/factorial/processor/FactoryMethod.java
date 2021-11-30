package com.github.aliakseikaraliou.factorial.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import java.util.Collections;
import java.util.List;

class FactoryMethod {
    private final String root;
    private final List<? extends VariableElement> params;
    private final Element element;

    private FactoryMethod(Builder builder) {
        this.root = builder.root;
        this.params = Collections.unmodifiableList(builder.params);
        this.element = builder.element;
    }

    public String getRoot() {
        return root;
    }

    public List<? extends VariableElement> getParams() {
        return params;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String root;
        private List<? extends VariableElement> params;
        private Element element;

        public Builder setRoot(String root) {
            this.root = root;
            return this;
        }

        public Builder setParams(List<? extends VariableElement> params) {
            this.params = params;
            return this;
        }

        public Builder setElement(Element element) {
            this.element = element;
            return this;
        }

        public FactoryMethod build() {
            return new FactoryMethod(this);
        }
    }
}
