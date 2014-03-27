package org.pieShare.pieTools.pieCeption.service.core;

import javax.annotation.PostConstruct;
import org.pieShare.pieTools.pieCeption.service.core.api.IConnectorService;
import org.pieShare.pieTools.pieCeption.service.core.api.IPieCeptionService;
import org.pieShare.pieTools.pieCeption.service.core.exception.PieCeptionServiceException;
import org.pieShare.pieTools.pieCeption.service.core.exception.StartupServiceException;
import org.pieShare.pieTools.piePlate.model.message.api.IPieMessage;
import org.pieShare.pieTools.pieUtilities.service.commandParser.api.ICommandParserService;
import org.pieShare.pieTools.pieUtilities.service.commandParser.exception.CommandParserServiceException;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.IExecutorService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.IPieEvent;

/**
 * Created by Svetoslav on 29.12.13.
 */
public class PieCeptionService implements IPieCeptionService {
    private IConnectorService connectorService;
    private ICommandParserService commandParserService;
    private IExecutorService executorService;
    private boolean isMaster;

    public PieCeptionService(){
    }

    public void setConnectorService(IConnectorService connectorService) {
        this.connectorService = connectorService;
    }

    public void setCommandParserService(ICommandParserService commandParserService){
        this.commandParserService = commandParserService;
    }

    @PostConstruct
    public void start() throws PieCeptionServiceException {
        this.connectorService.connectToMaster("pieShare");
        this.isMaster = !this.connectorService.isPieShareRunning();
    }

    @Override
    public void handlePieMessage(IPieMessage message) {
        if(this.isMaster) {
            this.executorService.handlePieEvent(message);
            return;
        }
        
        //todo-sv: send to master via cluster
    }
}
