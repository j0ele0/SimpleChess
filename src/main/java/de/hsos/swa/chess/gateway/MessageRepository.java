package de.hsos.swa.chess.gateway;

import de.hsos.swa.chess.shared.PlayerMessage;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.jaxrs.OutboundSseEventImpl;
import org.jboss.resteasy.reactive.server.jaxrs.SseBroadcasterImpl;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import java.util.*;

@ApplicationScoped
public class MessageRepository {
    private static final Logger log = Logger.getLogger(MessageRepository.class);

    private static final OutboundSseEvent.Builder builder = new OutboundSseEventImpl.BuilderImpl();
    private Map<Long, SseBroadcaster> publisher = new HashMap<>();

    public void createBroadcast(Long publisherId) {
        publisher.put(publisherId, new SseBroadcasterImpl());
    }

    public boolean removeBroadcast(Long publisherId) {
        SseBroadcaster broadcast = publisher.remove(publisherId);
        if(broadcast == null) {
            log.info("cant remove broadcast(publisherId: "+publisherId+"): broadcast does not exist");
            return false;
        }
        log.info("broadcast(publisherId: "+publisherId+") removed");
        broadcast.close();
        return true;
    }

    public Set<Long> getPublisherIds() {
        return publisher.keySet();
    }

    public void removeOldBroadcasts(Set<Long> oldPublisherIds) {
        log.info("removing "+oldPublisherIds.size()+" old Broadcasts...");
        for(Long publisherId: oldPublisherIds) {
            publisher.remove(publisherId);
        }
    }

    public String publishMessage(Long publisherId, String message) {
        if(!publisher.containsKey(publisherId)) {
            log.error("create broadcast(publisherId: "+publisherId+")");
            publisher.put(publisherId, new SseBroadcasterImpl());
        }
        SseBroadcaster broadcast = publisher.get(publisherId);
        try {
            publisher.get(publisherId).broadcast(builder.data(message).build());
        }
        catch(IllegalStateException ex){
            log.debug("There is no active subscriber for this broadcast");
        }
        log.info("publisher with publisherId: "+publisherId+" published: "+ message);
        return message;
    }

    public boolean subscribeToBroadcaster(Long publisherId, SseEventSink sink) {
        if(!publisher.containsKey(publisherId)) {
            log.debug("cant subscribe to messages: publisher not available");
            log.debug("broadcaster size: "+publisher.size()+", publisherId: "+publisherId);
            return false;
        }
        publisher.get(publisherId).register(sink);
        log.info("subscribe broadcaster(publisherId: "+publisherId+"): "+publisher.get(publisherId));
        return true;
    }

}
