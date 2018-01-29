/*******************************************************************************
 * Copyright 2014 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.onrc.openvirtex.messages.ver13;


import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.OVXBadRequestErrorMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFBadRequestCode;
import org.projectfloodlight.openflow.protocol.ver13.OFBadRequestErrorMsgVer13;
import org.projectfloodlight.openflow.types.OFErrorCauseData;

public class OVXBadRequestErrorMsgVer13 extends OFBadRequestErrorMsgVer13 implements OVXBadRequestErrorMsg {

     public OVXBadRequestErrorMsgVer13(long xid, OFBadRequestCode code,
                                       OFErrorCauseData data) {
		super(xid, code, data);
		// TODO Auto-generated constructor stub
	}

	private final Logger log = LogManager.getLogger(OVXBadRequestErrorMsgVer13.class.getName());

     
    
    @Override
    public void devirtualize(final OVXSwitch sw) {
        // TODO Auto-generated method stub

    }

    @Override
    public void virtualize(final PhysicalSwitch sw) {
        /*
         * TODO: For now, just report the error. In the future parse them and
         * forward to controller if need be.
         */
        log.error(this.getCode());

    }



}
