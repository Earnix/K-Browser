package com.earnix.webk.simple.extend.form;

import java.awt.LayoutManager;
import java.util.function.Consumer;
import javax.swing.JPanel;

public abstract class FileInputComponent extends JPanel
{
    public FileInputComponent(LayoutManager layout)
    {
        super(layout);
    }

    public abstract String getFilePath();

    public abstract void setFilePath(String path);

    public abstract void setOnChangeListener(Consumer<String> onChangeConsumer);
}
