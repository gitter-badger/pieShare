/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieShareApp.service.factoryService;

import org.pieShare.pieShareApp.model.message.FileListMessage;
import org.pieShare.pieShareApp.model.message.FileListRequestMessage;
import org.pieShare.pieShareApp.model.message.MetaMessage;
import org.pieShare.pieShareApp.model.message.MetaCommitMessage;
import org.pieShare.pieShareApp.model.message.api.IFileChangedMessage;
import org.pieShare.pieShareApp.model.message.api.IFileDeletedMessage;
import org.pieShare.pieShareApp.model.message.api.IFileListMessage;
import org.pieShare.pieShareApp.model.message.api.IFileListRequestMessage;
import org.pieShare.pieShareApp.model.message.api.IFileRequestMessage;
import org.pieShare.pieShareApp.model.message.api.IFileTransferCompleteMessage;
import org.pieShare.pieShareApp.model.message.api.IMetaMessage;
import org.pieShare.pieShareApp.model.message.api.IMetaCommitMessage;
import org.pieShare.pieShareApp.model.message.api.INewFileMessage;
import org.pieShare.pieShareApp.model.message.fileHistoryMessage.FileChangedMessage;
import org.pieShare.pieShareApp.model.message.fileHistoryMessage.FileDeletedMessage;
import org.pieShare.pieShareApp.model.message.fileMessageBase.FileRequestMessage;
import org.pieShare.pieShareApp.model.message.fileMessageBase.FileTransferCompleteMessage;
import org.pieShare.pieShareApp.model.message.fileMessageBase.NewFileMessage;
import org.pieShare.pieShareApp.model.pieFile.FileMeta;
import org.pieShare.pieTools.piePlate.model.IPieAddress;
import org.pieShare.pieTools.piePlate.model.message.api.IClusterMessage;
import org.pieShare.pieTools.piePlate.model.serializer.jacksonSerializer.JGroupsPieAddress;

/**
 *
 * @author Svetoslav
 */
public class MessageFactoryService implements IMessageFactoryService {
	
	protected <P extends IClusterMessage> P prepareMessage(P message) {
		IPieAddress ad = new JGroupsPieAddress();
		message.setAddress(ad);
		return message;
	}

	@Override
	public IFileChangedMessage getFileChangedMessage() {
		return this.prepareMessage(new FileChangedMessage());
	}

	@Override
	public IFileDeletedMessage getFileDeletedMessage() {
		return this.prepareMessage(new FileDeletedMessage());
	}

	@Override
	public IFileListMessage getFileListMessage() {
		return this.prepareMessage(new FileListMessage());
	}

	@Override
	public IFileListRequestMessage getFileListRequestMessage() {
		return this.prepareMessage(new FileListRequestMessage());
	}

	@Override
	public IFileRequestMessage getFileRequestMessage() {
		return this.prepareMessage(new FileRequestMessage());
	}

	@Override
	public IFileTransferCompleteMessage getFileTransferCompleteMessage() {
		return this.prepareMessage(new FileTransferCompleteMessage());
	}

	@Override
	public IMetaMessage getFileTransferMetaMessage() {
		IMetaMessage msg = this.prepareMessage(new MetaMessage());
		msg.setFileMeta(new FileMeta());
		return msg;
	}

	@Override
	public INewFileMessage getNewFileMessage() {
		return this.prepareMessage(new NewFileMessage());
	}

	@Override
	public IMetaCommitMessage getMetaCommitMessage() {
		return this.prepareMessage(new MetaCommitMessage());
	}
	
}
