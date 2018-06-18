package org.xhtmlrenderer.js.web_idl;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Taras Maslov
 * 6/1/2018
 */
public interface Attribute<T> {
    public T get();
    public void set(T t);
    
    static <T> AtBuilder<T> receive(Consumer<T> consumer){
        return new AtBuilder<T>(consumer);
    }
    
    static <T> AtBuilder<T> readOnly(){
        return new AtBuilder<T>(null);
    }
        
    static <T> Attribute<T> readOnly(T value){
        return new Attribute<T>() {
            @Override
            public T get() {
                return value;
            }

            @Override
            public void set(T t) {
            }
        };
    }
    
    public static class AtBuilder<T> {
        private Consumer<T> consumer;

        private AtBuilder(Consumer<T> consumer) {
            this.consumer = consumer;
        }

        public Attribute<T> give(Supplier<T> supplier){
            return new Attribute<T>() {
                @Override
                public T get() {
                    return supplier.get();
                }

                @Override
                public void set(T o) {
                    consumer.accept(o);
                }
            };
        };
    }
}