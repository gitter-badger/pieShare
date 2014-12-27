/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieShareApp.service.shareService;

import com.turn.ttorrent.client.SharedTorrent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.pieShare.pieShareApp.model.PieShareAppBeanNames;
import org.pieShare.pieShareApp.model.PieUser;
import org.pieShare.pieShareApp.model.message.api.IFileTransferCompleteMessage;
import org.pieShare.pieShareApp.model.message.api.IFileTransferMetaMessage;
import org.pieShare.pieShareApp.model.pieFile.PieFile;
import org.pieShare.pieShareApp.service.configurationService.api.IPieShareConfiguration;
import org.pieShare.pieShareApp.service.factoryService.IMessageFactoryService;
import org.pieShare.pieShareApp.service.fileService.api.IFileService;
import org.pieShare.pieShareApp.service.fileService.fileEncryptionService.IFileEncryptionService;
import org.pieShare.pieTools.piePlate.service.cluster.api.IClusterManagementService;
import org.pieShare.pieTools.piePlate.service.cluster.exception.ClusterManagmentServiceException;
import org.pieShare.pieTools.pieUtilities.service.base64Service.api.IBase64Service;
import org.pieShare.pieTools.pieUtilities.service.beanService.IBeanService;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;
import org.pieShare.pieTools.pieUtilities.service.tempFolderService.api.ITempFolderService;

/**
 *
 * @author Svetoslav
 */
public class ShareService implements IShareService{
	
	private IBitTorrentService bitTorrentService;
	
	private ITempFolderService tmpFolderService;
	private IClusterManagementService clusterManagementService;
	private IBeanService beanService;
	private IBase64Service base64Service;
	private IFileService fileService;
	private ConcurrentHashMap<PieFile, Integer> sharedFiles;
	
	
	private IPieShareConfiguration configuration;
	
	
	private IFileEncryptionService fileEncryptionService;
	private IMessageFactoryService messageFactoryService;

	public void init() {
		PieUser user = beanService.getBean(PieShareAppBeanNames.getPieUser());
		configuration = user.getPieShareConfiguration();
		this.sharedFiles = new ConcurrentHashMap<>();
	}

	public void setBitTorrentService(IBitTorrentService bitTorrentService) {
		this.bitTorrentService = bitTorrentService;
	}

	public void setMessageFactoryService(IMessageFactoryService messageFactoryService) {
		this.messageFactoryService = messageFactoryService;
	}

	public void setFileEncryptionService(IFileEncryptionService fileEncryptionService) {
		this.fileEncryptionService = fileEncryptionService;
	}

	public void setSharedFiles(ConcurrentHashMap<PieFile, Integer> sharedFiles) {
		this.sharedFiles = sharedFiles;
	}

	public void setBase64Service(IBase64Service base64Service) {
		this.base64Service = base64Service;
	}

	public void setBeanService(IBeanService beanService) {
		this.beanService = beanService;
	}

	public void setFileUtilsService(IFileService fileService) {
		this.fileService = fileService;
	}

	public void setClusterManagementService(IClusterManagementService clusterManagementService) {
		this.clusterManagementService = clusterManagementService;
	}

	public void setTmpFolderService(ITempFolderService tmpFolderService) {
		this.tmpFolderService = tmpFolderService;
	}

	@Override
	public void shareFile(PieFile file) {
		try {
			//PieFile tmpFile = this.fileService.getTmpPieFile(file);
			File localFile = this.fileService.getAbsolutePath(file).toFile();
			//File localTmpFile = this.fileService.getAbsoluteTmpPath(tmpFile).toFile();
			File localTmpFile = this.fileService.getAbsoluteTmpPath(file).toFile();
			
			this.fileEncryptionService.encryptFile(localFile, localTmpFile);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//this.bitTorrentService.shareTorrent(tmpFile, baos);
			this.bitTorrentService.shareTorrent(file, localTmpFile, baos);
			
			this.initPieFileState(file, 0);
			this.manipulatePieFileState(file, 1);

			IFileTransferMetaMessage metaMsg = this.messageFactoryService.getFileTransferMetaMessage();
			metaMsg.setMetaInfo(base64Service.encode(baos.toByteArray()));
			metaMsg.setPieFile(file);
			//todo: think about some kind o PieAdress factory
			PieUser user = beanService.getBean(PieShareAppBeanNames.getPieUser());
			metaMsg.getAddress().setChannelId(user.getUserName());
			metaMsg.getAddress().setClusterName(user.getCloudName());
			this.clusterManagementService.sendMessage(metaMsg);
			
			//todo: there is still some problem with the modification date!!! somewhere...
			//todo: ther is a bug when triing to share 0 byte files
			//todo: error handling when torrent null
			//todo: replace name by nodeName			
			//URI uri = tracker.getAnnounceUrl().toURI();
			//share torrent
			//PieFile pieFile = this.fileService.getPieFile(file);
			//todo: find out why ttorrent changes the date modified when sharing a file?!
			//this.fileService.setCorrectModificationDate(encTmpFile);
		} catch (ClusterManagmentServiceException ex) {
			PieLogger.error(this.getClass(), "Sharing error.", ex);
		}
	}

	//todo: maybe merge this with handle activeshare
	@Override
	public void handleFile(PieFile file, byte[] metaInfo) {
		
		if(this.sharedFiles.containsKey(file)) {
			//allready handling this file
			return;
		}

		try {
			this.initPieFileState(file, 0);

			//File tmpDir = tmpFolderService.createTempFolder(file.getFileName(), configuration.getTmpDir());
			File localTmpFile = this.fileService.getAbsoluteTmpPath(file).toFile();
			//todo: does this belong into the fileService?
			if (!localTmpFile.getParentFile().exists()) {
				localTmpFile.getParentFile().mkdirs();
			}
			//SharedTorrent torrent = new SharedTorrent(base64Service.decode(metaInfo), tmpDir);
			SharedTorrent torrent = new SharedTorrent(base64Service.decode(metaInfo), localTmpFile.getParentFile());
			
			this.bitTorrentService.handleSharedTorrent(file, torrent);
		}
		catch (IOException ex) {
			PieLogger.error(this.getClass(), "Sharing error.", ex);
		}
		catch (Exception ex) {
			PieLogger.error(this.getClass(), "Sharing error.", ex);
		}
	}

	private synchronized void initPieFileState(PieFile file, Integer count) {
		if (!this.sharedFiles.containsKey(file)) {
			this.sharedFiles.put(file, count);
		}
	}

	private synchronized void removePieFileState(PieFile file) {
		this.sharedFiles.remove(file);
	}

	private synchronized void manipulatePieFileState(PieFile file, Integer value) {
		if (this.sharedFiles.containsKey(file)) {
			int newValue = this.sharedFiles.get(file) + value;
			
			if(newValue <= 0) {
				this.removePieFileState(file);
				return;
			}
			
			this.sharedFiles.put(file, newValue);
		}
	}

	@Override
	public void localFileTransferComplete(PieFile file, boolean source) {
		try {
			File localTmpFile = this.fileService.getAbsoluteTmpPath(file).toFile();
		
			if(!source) {
			
				File localFile = this.fileService.getAbsolutePath(file).toFile();
				
				//todo: does this belong into the fileService?
				if (!localFile.getParentFile().exists()) {
					localFile.getParentFile().mkdirs();
				}
				
				this.fileEncryptionService.decryptFile(localTmpFile, localFile);
				
				this.fileService.setCorrectModificationDate(file);
				
				IFileTransferCompleteMessage msgComplete = this.messageFactoryService.getFileTransferCompleteMessage();
				msgComplete.setPieFile(file);
				PieUser user = this.beanService.getBean(PieShareAppBeanNames.getPieUser());
				msgComplete.getAddress().setChannelId(user.getUserName());
				msgComplete.getAddress().setClusterName(user.getCloudName());
				this.clusterManagementService.sendMessage(msgComplete);
			
			}
		
			//localTmpFile.delete();
		
			this.manipulatePieFileState(file, -1);
		} catch (ClusterManagmentServiceException ex) {
			Logger.getLogger(ShareService.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	@Override
	public void remoteFileTransferComplete(PieFile file) {
		this.manipulatePieFileState(file, -1);
	}

	@Override
	public void handleRemoteRequestForActiveShare(PieFile pieFile) {
		try {
			PieFile tmpFile = this.fileService.getTmpPieFile(pieFile);
			this.manipulatePieFileState(tmpFile, 1);
		} catch (IOException ex) {
			Logger.getLogger(ShareService.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	@Override
	public boolean isShareActive(PieFile file) {
		if(!this.sharedFiles.containsKey(file)) {
			return false;
		}
		
		return (this.sharedFiles.get(file) > 0);
	}
}
