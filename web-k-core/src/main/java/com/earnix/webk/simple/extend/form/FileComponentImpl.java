package com.earnix.webk.simple.extend.form;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;

public class FileComponentImpl extends FileInputComponent
{
	private String path;
	private final JTextField textField;
	private final JButton select;
	private Consumer<String> onChangeConsumer;

	public FileComponentImpl()
	{
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		textField = new JTextField("No file chosen", 30);
		textField.setEditable(false);
		textField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

		select = new JButton("Choose File");
		select.addActionListener(l -> {
			String filePath = getFilePath();
			File currentFile = filePath == null ? null : Paths.get(filePath).toFile();

			final JFileChooser fc = new JFileChooser();
			if (currentFile != null && currentFile.exists())
				fc.setCurrentDirectory(currentFile);
			fc.setMultiSelectionEnabled(false);
			//In response to a button click:
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				setFilePath(fc.getSelectedFile().getAbsolutePath());
			}
		});
		textField.setPreferredSize(new Dimension(textField.getPreferredSize().width, select.getPreferredSize().height));
		this.add(select);
		this.add(textField);
	}

	@Override
	public String getFilePath()
	{
		return path;
	}

	@Override
	public void setFilePath(String stringPath)
	{
		String originalPath = path;
		String finalPathStr = stringPath;
		String fileName = null;

		if (stringPath != null)
		{
			Path filePath = Paths.get(stringPath);
			if (filePath.toFile().exists())
			{
				finalPathStr = filePath.toFile().getAbsolutePath();
				fileName = (filePath.getFileName().toString());
			}
		}

		this.path = finalPathStr;
		textField.setText(StringUtils.defaultString(fileName, "No file chosen"));
		textField.setToolTipText(StringUtils.defaultString(fileName));

		if (onChangeConsumer != null && !StringUtils.equals(originalPath, this.path))
			onChangeConsumer.accept(this.path);
	}

	@Override
	public void setOnChangeListener(Consumer<String> onChangeConsumer)
	{
		this.onChangeConsumer = onChangeConsumer;
	}
}
