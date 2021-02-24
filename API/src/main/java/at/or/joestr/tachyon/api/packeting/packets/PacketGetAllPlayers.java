// 
// Private License
// 
// Copyright (c) 2019-2020 Joel Strasser
// 
// Only the owner is allowed to use this software.
// 
package at.or.joestr.tachyon.api.packeting.packets;

import java.util.Map;
import java.util.UUID;
import at.or.joestr.tachyon.api.packeting.Packets;


/**
 *
 * @author Joel
 */
public class PacketGetAllPlayers extends Packet {
	
	public Map<UUID, String> players;
	
	public PacketGetAllPlayers(Map<UUID, String> players) {
		super(Packets.Provided.GET_ALL_PLAYERS);
		
		this.players = players;
	}
	
	public Map<UUID, String> getPlayers() {
		return this.players;
	}
}