package org.pieShare.pieTools.piePlate.service.cluster.jgroupsCluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.Validate;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.pieShare.pieTools.piePlate.model.IPieAddress;
import org.pieShare.pieTools.piePlate.model.message.api.IClusterMessage;
import org.pieShare.pieTools.piePlate.model.serializer.jacksonSerializer.JGroupsPieAddress;
import org.pieShare.pieTools.piePlate.service.channel.api.IIncomingChannel;
import org.pieShare.pieTools.piePlate.service.channel.api.IOutgoingChannel;
import org.pieShare.pieTools.piePlate.service.channel.api.ITwoWayChannel;
import org.pieShare.pieTools.piePlate.service.cluster.api.IClusterService;
import org.pieShare.pieTools.piePlate.service.cluster.event.ClusterRemovedEvent;
import org.pieShare.pieTools.piePlate.service.cluster.event.IClusterRemovedListener;
import org.pieShare.pieTools.piePlate.service.cluster.exception.ClusterServiceException;
import org.pieShare.pieTools.piePlate.service.serializer.api.ISerializerService;
import org.pieShare.pieTools.pieUtilities.model.EncryptedPassword;
import org.pieShare.pieTools.pieUtilities.service.eventBase.IEventBase;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;
import org.pieShare.pieTools.pieUtilities.service.security.encodeService.api.IEncodeService;
import org.pieShare.pieTools.pieUtilities.service.shutDownService.api.IShutdownService;
import org.pieShare.pieTools.pieUtilities.service.shutDownService.api.IShutdownableService;

public class JGroupsClusterService implements IClusterService, IShutdownableService {

	//todo-sv: the model / service seperation is fuzzy in here: check it out!!!
	private List<IIncomingChannel> incomingChannels;
	private Map<String, IOutgoingChannel> outgoingChannels;

	private ObjectBasedReceiver receiver;
	private JChannel channel;
	private IEventBase<IClusterRemovedListener, ClusterRemovedEvent> clusterRemovedEventBase;
	private String id;

	public JGroupsClusterService() {
		//todo: bean service here?
		this.incomingChannels = new ArrayList<>();
		this.outgoingChannels = new HashMap<>();
	}

	@Override
	public IEventBase<IClusterRemovedListener, ClusterRemovedEvent> getClusterRemovedEventBase() {
		return this.clusterRemovedEventBase;
	}

	public void setClusterRemovedEventBase(IEventBase<IClusterRemovedListener, ClusterRemovedEvent> clusterRemovedEventBase) {
		this.clusterRemovedEventBase = clusterRemovedEventBase;
	}

	public void setChannel(JChannel channel) {
		this.channel = channel;
	}

	public void setReceiver(ObjectBasedReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void connect(String clusterName) throws ClusterServiceException {
		try {
			Validate.notNull(this.receiver);
			this.receiver.setClusterService(this);

			this.channel.setReceiver(this.receiver);
			this.channel.setDiscardOwnMessages(true);
			this.channel.connect(clusterName);

		}
		catch (NullPointerException e) {
			throw new ClusterServiceException("Receiver not set!");
		}
		catch (Exception e) {
			throw new ClusterServiceException(e);
		}
	}

	@Override
	public boolean isMaster() {
		if (this.channel.getView().getMembers().get(0).equals(this.channel.getAddress())) {
			return true;
		}
		return false;
	}

	@Override
	public void sendMessage(IClusterMessage msg) throws ClusterServiceException {
		Address ad = null;
		IPieAddress address = msg.getAddress();

		if (!this.outgoingChannels.containsKey(address.getChannelId())) {
			throw new ClusterServiceException(String.format("This outgoing channel doesn't exists: %s", address.getChannelId()));
		}

		if (msg.getAddress() instanceof JGroupsPieAddress) {
			ad = ((JGroupsPieAddress) msg.getAddress()).getAddress();
		}

		try {
			PieLogger.debug(this.getClass(), "Sending: {}", msg.getClass());
			byte[] message = this.outgoingChannels.get(msg.getAddress().getChannelId()).prepareMessage(msg);
			this.channel.send(ad, message);
		}
		catch (Exception e) {
			throw new ClusterServiceException(e);
		}
	}

	@Override
	public int getMembersCount() {
		return this.channel.getView().getMembers().size();
	}

	@Override
	public boolean isConnectedToCluster() {
		return this.channel.isConnected();
	}

	@Override
	public void disconnect() throws ClusterServiceException {
		//todo: this needs additional fixing: the true error is that this class doesn't get removed from the shutdown event listeners after manually shuting down
		//todo: all listeners need to proper handle event unsubscription
		if(this.channel == null) {
			return;
		}
		this.channel.setReceiver(null);
		
		this.channel.disconnect();
		PieLogger.trace(this.getClass(), "Channel {} disconnected!", this.id);
		this.channel.close();
		PieLogger.trace(this.getClass(), "Channel {} closed!", this.id);
		
		this.channel = null;
		clusterRemovedEventBase.fireEvent(new ClusterRemovedEvent(this));
	}

	@Override
	public List<IIncomingChannel> getIncomingChannels() {
		return this.incomingChannels;
	}

	@Override
	public void registerIncomingChannel(IIncomingChannel channel) {
		this.incomingChannels.add(channel);
	}

	@Override
	public void registerOutgoingChannel(IOutgoingChannel channel) {
		//todo-piePlate: throw exceptions if adding to map is not possible

		this.outgoingChannels.put(channel.getChannelId(), channel);
	}

	@Override
	public void shutdown() {
		try {
			this.disconnect();
		} catch (ClusterServiceException ex) {
			PieLogger.error(this.getClass(), "Could not disconnect!", ex);
		}
	}
}
