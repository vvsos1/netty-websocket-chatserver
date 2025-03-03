package org.dimigo;

/**
 * <pre>
 * nettyInActionWebSocket
 * 	 |_ FileInfo
 *
 * 1. 개요 : 
 * 2. 작성일 : 2017. 11. 9.
 * <pre>
 *
 * @author : 박명규(로컬계정)
 * @version : 1.0
 */
public class FileInfo {
	private final String fileName;
	private final long fileSize;
	private final String fileUploader;
	
	public FileInfo(String fileName, long fileSize, String fileUploader) {
		super();
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.fileUploader = fileUploader;
	}

	public String getFileName() {
		return fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public String getFileUploader() {
		return fileUploader;
	}
	
	
}
