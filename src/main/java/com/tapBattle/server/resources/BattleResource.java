package com.tapBattle.server.resources;

import java.io.IOException;

import javax.ws.rs.Path;

import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Message;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.interceptor.CorsInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapBattle.server.BattleServer;
import com.tapBattle.server.core.MessageDecoder;
import com.tapBattle.server.core.UserMessage;

import io.swagger.annotations.Api;

@ManagedService(path="/battle/connect")
public class BattleResource{
	
	private final ObjectMapper mapper = new ObjectMapper();
	
	public BattleResource() {

	}
	
	@org.atmosphere.config.service.Message(decoders = {MessageDecoder.class})
	public void onMessage(final AtmosphereResource r, UserMessage message) throws IOException {
		if(message.getMessageType().equals("REGISTER")) {
			BattleServer.registerUser(message.getUser(), r);
		}else if(message.getMessageType().equals("BATTLE")) {
			BattleServer.startBattle(message.getUser());
		}else if(message.getMessageType().equals("TAP")) {
			BattleServer.addUserTapToBattle(message.getUser(), message.getBattle().getId());
		}
	}
	
	@Ready
    public void onReady(final AtmosphereResource r) {
        System.out.println("BattleResource: OnReady: Browser " + r.uuid() + " connected.");
    }

    @Disconnect
    public void onDisconnect(AtmosphereResourceEvent event) {
        if (event.isCancelled()) {
            System.out.println("BattleResource: OnDisconnect: Browser " + event.getResource().uuid() + " unexpectedly disconnected");
        } else if (event.isClosedByClient()) {
            System.out.println("BattleResource: OnDisconnect: Browser " + event.getResource().uuid() + " closed the connection");
        }
        BattleServer.disconnectResource(event.getResource());
    }

}
