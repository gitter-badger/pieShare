/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieShareApp.model;

import org.pieShare.pieShareApp.model.message.FileChangedMessage;
import org.pieShare.pieShareApp.model.message.FileListRequestMessage;
import org.pieShare.pieShareApp.service.fileService.fileListenerService.ApacheFileWatcherService;
import org.pieShare.pieShareApp.springConfiguration.PieShareApp.PieShareAppModel;

/**
 *
 * @author Svetoslav
 */
public class PieShareAppBeanNames {

	public static String getLoginCommmandName() {
		return "loginCommand";
	}

	public static String getPieUser() {
		return "pieUser";
	}

	public static String getLoginActionServiceName() {
		return "loginActionService";
	}

	public static String getFileServiceName() {
		return "fileService";
	}

	public static String getPieFileName() {
		return "pieFile";
	}

	public static String getFileCreatedTaskName() {
		return "fileCreatedTask";
	}

	public static String getFileChangedTaskName() {
		return "localFileChangedTask";
	}

	public static String getPieShareAppConfigurationName() {
		return "pieShareAppConfiguraion";
	}

	public static String getFileRequestMessageName() {
		return "fileRequestMessage";
	}

	public static String getNewFileMessageName() {
		return "newFileMessage";
	}
	
	public static String getGUILoader() {
		return "fxmlLoader";
	}
	
	public static String getLocalFileCreatedTask() {
		return "localFileCreatedTask";
	}
	
	public static String getFileDeletedMessage() {
		return "fileDeletedMessage";
	}
	
	public static String getLocalFileDeletedTask() {
		return "localFileDeletedTask";
	}
	
	public static Class<FileChangedMessage> getFileChangedMessage() {
		return FileChangedMessage.class;
	}
	
	public static Class<FileListRequestMessage> getFileListRequestMessage() {
		return FileListRequestMessage.class;
	}
}
