/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package xyz.joestr.tachyon.api.requests;

import java.util.UUID;
import java.util.logging.Logger;
import xyz.joestr.tachyon.api.request.Request;

/**
 *
 * @author Joel
 */
public class PlayerToPlayerTeleportRequest extends Request {
    
    private UUID sender;
    private UUID target;

    public PlayerToPlayerTeleportRequest() {
    }

    public PlayerToPlayerTeleportRequest(UUID sender, UUID target) {
        this.sender = sender;
        this.target = target;
    }

    public UUID getSender() {
        return sender;
    }

    public void setSender(UUID sender) {
        this.sender = sender;
    }

    public UUID getTarget() {
        return target;
    }

    public void setTarget(UUID target) {
        this.target = target;
    }
}