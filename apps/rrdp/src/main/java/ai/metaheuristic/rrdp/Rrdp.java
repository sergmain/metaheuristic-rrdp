package ai.metaheuristic.rrdp;

import java.util.Iterator;
import java.util.function.Consumer;

import static ai.metaheuristic.rrdp.RrdpEnums.EntryState;
import static ai.metaheuristic.rrdp.RrdpEnums.NotificationEntryType;

/**
 * @author Serge
 * Date: 6/1/2022
 * Time: 4:39 AM
 */
public class Rrdp {

    private final RrdpConfig cfg;
    public String session;
    public int serial;
    public NotificationEntryType notificationEntryType;
    public boolean newEntries = false;

    public Rrdp(RrdpConfig cfg) {
        this.cfg = cfg;
        if (cfg.rfc8182) {
            if (!cfg.isFileContent) {
                throw new IllegalStateException("(!cfg.isFileContent)");
            }
        }
    }

    public void produce() {
        session = cfg.getSession.get();
        serial = cfg.currSerial.apply(session);
        notificationEntryType = serial == 1 ? NotificationEntryType.SNAPSHOT : cfg.produceType.get();
        if (notificationEntryType == NotificationEntryType.SNAPSHOT && serial!=1) {
            throw new IllegalStateException("(produce== RrdpEnums.NotificationEntryType.SNAPHOST && serial!=1)");
        }

        switch(notificationEntryType) {
            case SNAPSHOT:
                produceSnapshot(session);
                break;
            case DELTA:
                produceDelta(session, serial);
                break;
        }
    }

    public void produceNotification(UriAndHash uriAndHash, Consumer<String> persistNotification) {
        Notification n;
        if (serial>1) {
            n = cfg.currentNotification.get();
            if (!n.sessionId.equals(session)) {
                throw new IllegalStateException("(!n.sessionId.equals(session))");
            }
            if (n.serial+1!=serial) {
                throw new IllegalStateException("(n.serial+1!=serial)");
            }
        }
        else {
            n = new Notification(session, serial);
        }
        n.entries.add(new Notification.Entry(notificationEntryType, uriAndHash.uri, uriAndHash.hash, serial));

        persistNotification.accept(
                "<notification xmlns=\"http://www.ripe.net/rpki/rrdp\" version=\"1\" serial=\""+serial+"\" session_id=\""+session+"\">\n");
        for (Notification.Entry entry : n.entries) {
            if (entry.type== NotificationEntryType.SNAPSHOT) {
                persistNotification.accept(
                        " <snapshot uri=\""+entry.uri+"\" hash=\""+entry.hash+"\"/>\n");
            }
            else if (entry.type== NotificationEntryType.DELTA) {
                persistNotification.accept(
                        " <delta uri=\""+entry.uri+"\" hash=\""+entry.hash+"\" serial=\""+entry.serial+"\"/>\n");
            }
            else {
                throw new IllegalStateException("unknown type: " + entry.type);
            }
        }
        persistNotification.accept("</notification>\n");
    }

    private void produceSnapshot(String session) {
        cfg.persistNotificationEntry.accept(
                "<snapshot xmlns=\"http://www.ripe.net/rpki/rrdp\" version=\"1\" serial=\"1\" session_id=\""+session+"\">\n");
        Iterator<RrdpEntry> it = cfg.rrdpEntryIterator.get();
        while (it.hasNext()) {
            RrdpEntry entry = it.next();
            if (cfg.rfc8182 && !entry.state.rfc8182) {
                throw new IllegalStateException("Entry " + entry.uri.get() + " must be rfc8192 compatible.");
            }
            if (entry.state==EntryState.WITHDRAWAL) {
                throw new IllegalStateException("Entry " + entry.uri.get() + " can't be WITHDRAWAL while NotificationEntryType is SNAPSHOT.");
            }
            cfg.persistNotificationEntry.accept(
                    " <publish uri=\""+entry.uri.get()+"\"");
            if (!cfg.rfc8182) {
                cfg.persistNotificationEntry.accept(
                        " hash=\""+entry.hash.get()+"\"");
            }
            if (cfg.isFileContent) {
                if (entry.content==null) {
                    throw new IllegalStateException("(entry.content==null)");
                }
                cfg.persistNotificationEntry.accept(">\n"+entry.content.get()+"\n </publish>\n");
            }
            else {
                cfg.persistNotificationEntry.accept("/>\n");
            }
        }
        cfg.persistNotificationEntry.accept("</snapshot>\n");
    }

    private void produceDelta(String session, int serial) {
        cfg.persistNotificationEntry.accept(
                "<delta xmlns=\"http://www.ripe.net/rpki/rrdp\" version=\"1\" serial=\""+serial+"\" session_id=\""+session+"\">\n");
        Iterator<RrdpEntry> it = cfg.rrdpEntryIterator.get();
        while (it.hasNext()) {
            RrdpEntry entry = it.next();
            if (cfg.rfc8182 && !entry.state.rfc8182) {
                throw new IllegalStateException("Entry " + entry.uri.get() + " must be rfc8192 compatible.");
            }
            if (entry.state== EntryState.PUBLISHED ||entry.state== EntryState.UPDATED) {
                cfg.persistNotificationEntry.accept(
                        " <publish uri=\""+entry.uri.get()+"\" hash=\""+entry.hash.get()+"\"");

                if (cfg.isFileContent) {
                    if (entry.content==null) {
                        throw new IllegalStateException("(entry.content==null)");
                    }
                    cfg.persistNotificationEntry.accept(">\n"+entry.content.get()+"\n  </publish>\n");
                }
                else {
                    cfg.persistNotificationEntry.accept("/>\n");
                }
            }
            else {
                cfg.persistNotificationEntry.accept(
                        " <withdraw uri=\""+entry.uri.get()+"\" hash=\""+entry.hash.get()+"\"/>\n");
            }
        }
        cfg.persistNotificationEntry.accept("</delta>\n");
    }

}
