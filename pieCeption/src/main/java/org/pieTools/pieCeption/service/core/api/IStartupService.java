package org.pieTools.pieCeption.service.core.api;

import org.pieTools.pieCeption.service.core.exception.StartupServiceException;

/**
 * Created by Svetoslav on 09.01.14.
 */
public interface IStartupService {
    public void startInstance() throws StartupServiceException;
}
