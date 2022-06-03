package ai.metaheuristic.rrdp;

import java.util.Iterator;

import static ai.metaheuristic.rrdp.RrdpEnums.EntryState;
import static ai.metaheuristic.rrdp.RrdpEnums.ProduceType;

/**
 * @author Serge
 * Date: 6/1/2022
 * Time: 4:39 AM
 */
public class Rrdp {

    private final RrdpConfig cfg;

    public Rrdp(RrdpConfig cfg) {
        this.cfg = cfg;
        if (cfg.rfc8182) {
            if (!cfg.isFileContent) {
                throw new IllegalStateException("(!cfg.isFileContent)");
            }
        }
    }

    public void produce() {
        String session = cfg.getSession.get();
        int serial = cfg.currSerial.apply(session);
        final ProduceType produce = serial == 0 ? ProduceType.SNAPSHOT : cfg.produceType.get();
        serial = cfg.nextSerial.apply(session);
        if (produce== ProduceType.SNAPSHOT && serial!=1) {
            throw new IllegalStateException("(produce== RrdpEnums.ProduceType.SNAPHOST && serial!=1)");
        }

        switch(produce) {
            case SNAPSHOT:
                produceSnapshot(session);
                break;
            case DELTA:
                produceDelta(session, serial);
                break;
        }
        produceNotification(session, serial);
    }

    private void produceSnapshot(String session) {
        cfg.persistSnapshot.accept(
                "<snapshot xmlns=\"http://www.ripe.net/rpki/rrdp\" version=\"1\" serial=\"1\" session_id=\""+session+"\">\n");
        Iterator<RrdpEntry> it = cfg.rrdpEntryIteator.get();
        while (it.hasNext()) {
            RrdpEntry entry = it.next();
            if (cfg.rfc8182 && !entry.state.rfc8182) {
                throw new IllegalStateException("Entry " + entry.uri.get() + " must be rfc8192 compatible.");
            }
            if (entry.state==EntryState.WITHDRAWAL) {
                throw new IllegalStateException("Entry " + entry.uri.get() + " can't be WITHDRAWAL while ProduceType is SNAPSHOT.");
            }
            cfg.persistSnapshot.accept(
                    " <publish uri=\""+entry.uri.get()+"\"");
            if (!cfg.rfc8182) {
                cfg.persistSnapshot.accept(
                        " hash=\""+entry.hash.get()+"\"");
            }
            if (cfg.isFileContent) {
                cfg.persistSnapshot.accept(">\n"+entry.content.get()+"\n </publish>\n");
            }
            else {
                cfg.persistSnapshot.accept("/>\n");
            }
        }
        cfg.persistSnapshot.accept("</snapshot>\n");
    }

    private void produceDelta(String session, int serial) {
        cfg.persistDelta.accept(
                "<delta xmlns=\"http://www.ripe.net/rpki/rrdp\" version=\"1\" serial=\""+serial+"\" session_id=\""+session+"\">\n");
        Iterator<RrdpEntry> it = cfg.rrdpEntryIteator.get();
        while (it.hasNext()) {
            RrdpEntry entry = it.next();
            if (cfg.rfc8182 && !entry.state.rfc8182) {
                throw new IllegalStateException("Entry " + entry.uri.get() + " must be rfc8192 compatible.");
            }
            if (entry.state== EntryState.PUBLISHED ||entry.state== EntryState.UPDATED) {
                cfg.persistDelta.accept(
                        " <publish uri=\""+entry.uri.get()+"\" hash=\""+entry.hash.get()+"\"");

                if (cfg.isFileContent) {
                    cfg.persistDelta.accept(">\n"+entry.content.get()+"\n  </publish>\n");
                }
                else {
                    cfg.persistDelta.accept("/>\n");
                }
            }
            else {
                cfg.persistDelta.accept(
                        " <withdraw uri=\""+entry.uri.get()+"\" hash=\""+entry.hash.get()+"\"/>\n");
            }
        }
        cfg.persistDelta.accept("</delta>\n");
    }

    private void produceNotification(String session, int serial) {
        Notification n;
        RrdpEntry newEntry = cfg.entryForSerial.apply(session, serial);
        if (serial>1) {
            String xml = cfg.currentNotification.get();
            n = RrdpUtils.parseNotificationXml(xml);
            if (!n.sessionId.equals(session)) {
                throw new IllegalStateException("(!n.sessionId.equals(session))");
            }
            if (n.serial+1!=serial) {
                throw new IllegalStateException("(n.serial+1!=serial)");
            }
            n.entries.add(new Notification.Entry(ProduceType.DELTA, newEntry.uri.get(), newEntry.hash.get(), serial));
        }
        else {
            n = new Notification(session, serial);
            n.entries.add(new Notification.Entry(ProduceType.SNAPSHOT, newEntry.uri.get(), newEntry.hash.get(), 1));
        }

        cfg.persistNotification.accept(
                "<notification xmlns=\"http://www.ripe.net/rpki/rrdp\" version=\"1\" serial=\""+serial+"\" session_id=\""+session+"\">\n");
        for (Notification.Entry entry : n.entries) {
            if (entry.type==ProduceType.SNAPSHOT) {
                cfg.persistNotification.accept(
                        " <snapshot uri=\""+entry.uri+"\" hash=\""+entry.hash+"\"/>\n");
            }
            else if (entry.type==ProduceType.DELTA) {
                cfg.persistNotification.accept(
                        " <delta serial=\""+entry.serial+"\" uri=\""+entry.uri+"\" hash=\""+entry.hash+"\"/>\n");
            }
            else {
                throw new IllegalStateException("unknown type: " + entry.type);
            }
        }
        cfg.persistNotification.accept("</notification>\n");
    }

}
