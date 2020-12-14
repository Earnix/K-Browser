package com.earnix.webk.simple.extend.form;

import java.util.function.Consumer;

public interface FileInputComponent {
    String getFilePath();

    void setFilePath(String path);

    void setOnChangeListener(Consumer<String> onChangeConsumer);
}
