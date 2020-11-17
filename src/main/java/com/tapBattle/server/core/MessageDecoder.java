package com.tapBattle.server.core;

import java.io.IOException;

import org.atmosphere.config.managed.Decoder;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageDecoder implements Decoder<String, UserMessage>{

	private final ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public UserMessage decode(String s) {
		try {
            return mapper.readValue(s, UserMessage.class);
        } catch (IOException e) {
        	e.printStackTrace();
            throw new RuntimeException(e);
        }
	}

}
